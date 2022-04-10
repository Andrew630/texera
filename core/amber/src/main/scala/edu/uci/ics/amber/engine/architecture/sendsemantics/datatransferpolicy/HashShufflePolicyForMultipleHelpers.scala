package edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy

import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.{DataFrame, DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

class HashShufflePolicyForMultipleHelpers(
    policyTag: LinkIdentity,
    batchSize: Int,
    val hashFunc: ITuple => Int,
    val shuffleKey: ITuple => String,
    receivers: Array[ActorVirtualIdentity]
) extends DataSendingPolicy(policyTag, batchSize, receivers) {

  val numBuckets = receivers.length
  // buckets once decided will remain same because we are not changing the number of workers in Join
  var bucketsToReceivers = new mutable.HashMap[Int, ArrayBuffer[ActorVirtualIdentity]]()
  var bucketsToRedirectRatio =
    new mutable.HashMap[Int, (Long, ArrayBuffer[Long], Long)]() // bucket to (tuples idx, numerator, denominator)
  var bucketsToSharingEnabled = new mutable.HashMap[Int, Boolean]()
  var originalReceiverToHistory = new mutable.HashMap[ActorVirtualIdentity, ArrayBuffer[Long]]()
  var tupleIndexForHistory = 0
  var receiverToBatch = new mutable.HashMap[ActorVirtualIdentity, Array[ITuple]]()
  var receiverToCurrBatchSize = new mutable.HashMap[ActorVirtualIdentity, Int]()
  var receiverToTotalSent = new mutable.HashMap[ActorVirtualIdentity, Long]()

  initializeInternalState(receivers)

  var fluxChangeReceived: Boolean = false

  override def fluxExpMsgReceived(): Unit = {
    fluxChangeReceived = true
  }

  def selectBatchingIndex(tuple: ITuple): Int = {
    (hashFunc(tuple) % numBuckets + numBuckets) % numBuckets
  }

  override def noMore(): Array[(ActorVirtualIdentity, DataPayload)] = {
    println(
      s"No more received for worker. Already sent out ${receiverToTotalSent.mkString("\n\t")}"
    )
    val receiversAndBatches = new ArrayBuffer[(ActorVirtualIdentity, DataPayload)]
    for ((receiver, currSize) <- receiverToCurrBatchSize) {
      if (currSize > 0) {
        receiversAndBatches.append(
          (receiver, DataFrame(receiverToBatch(receiver).slice(0, currSize)))
        )
      }
      receiversAndBatches.append((receiver, EndOfUpstream()))
    }
    receiversAndBatches.toArray
  }

  override def getTotalSentCount(): mutable.HashMap[ActorVirtualIdentity, Long] = {
    receiverToTotalSent
  }

  // for non-heavy hitters get the default receiver
  def getDefaultReceiverForBucket(bucket: Int): ActorVirtualIdentity =
    bucketsToReceivers(bucket)(0)

  // to be called for heavy-hitter
  def getAndIncrementReceiverForBucket(bucket: Int): ActorVirtualIdentity = {
    var receiver: ActorVirtualIdentity = null

    // logic below is written in this way to avoid race condition on bucketsToReceivers map
    val receivers = bucketsToReceivers(bucket)
    var redirectRatio: (Long, ArrayBuffer[Long], Long) = bucketsToRedirectRatio.getOrElse(bucket, (0L, ArrayBuffer[Long](0L), 0L))

    if (receivers.size > 1 && bucketsToRedirectRatio.contains(bucket)) {
      breakable {
        for (i <- 0 to redirectRatio._2.size - 1) {
          if (redirectRatio._1 <= redirectRatio._2(i)) {
            receiver = receivers(i + 1)
            break
          }
        }
      }
    }

    // logic below is written in this way to avoid race condition on bucketsToRedirectRatio map
    if (redirectRatio._1 + 1L > redirectRatio._3) {
      bucketsToRedirectRatio(bucket) = (1L, redirectRatio._2, redirectRatio._3)
    } else {
      bucketsToRedirectRatio(bucket) = (redirectRatio._1 + 1L, redirectRatio._2, redirectRatio._3)
    }

    receiver
  }

  override def addReceiverToBucket(
      defaultRecId: ActorVirtualIdentity,
      newRecId: ArrayBuffer[ActorVirtualIdentity],
      tuplesToRedirectNumerator: ArrayBuffer[Long],
      tuplesToRedirectDenominator: Long
  ): Unit = {
    var defaultBucket: Int = -1
    bucketsToReceivers.keys.foreach(b => {
      if (bucketsToReceivers(b)(0) == defaultRecId) {
        defaultBucket = b
      }
    })
    assert(defaultBucket != -1)
    if (bucketsToReceivers(defaultBucket).size == 1) {
      bucketsToReceivers(defaultBucket).appendAll(newRecId)
    }
    bucketsToRedirectRatio(defaultBucket) = (1, tuplesToRedirectNumerator, tuplesToRedirectDenominator)
    println(s"Adding receivers to bucket ${bucketsToReceivers(defaultBucket).mkString(",")} with ${bucketsToRedirectRatio(defaultBucket)._2.mkString(",")}")
    bucketsToSharingEnabled(defaultBucket) = true

  }

  override def removeReceiverFromBucket(
      defaultRecId: ActorVirtualIdentity,
      recIdToRemove: ActorVirtualIdentity
  ): Unit = {
    var defaultBucket: Int = -1
    bucketsToReceivers.keys.foreach(b => {
      if (bucketsToReceivers(b)(0) == defaultRecId) {
        defaultBucket = b
      }
    })
    assert(defaultBucket != -1)
    bucketsToSharingEnabled(defaultBucket) = false
  }

  override def getWorkloadHistory(): mutable.HashMap[ActorVirtualIdentity, ArrayBuffer[Long]] = {
    val ret = new mutable.HashMap[ActorVirtualIdentity, ArrayBuffer[Long]]()
    originalReceiverToHistory.keys.foreach(rec => {
      ret(rec) = new ArrayBuffer[Long]()
      // copy all but last element because the last element is still forming
      for (i <- 0 to originalReceiverToHistory(rec).size - 2) {
        ret(rec).append(originalReceiverToHistory(rec)(i))
      }
      val mostRecentHistory =
        originalReceiverToHistory(rec)(originalReceiverToHistory(rec).size - 1)
      originalReceiverToHistory(rec) = ArrayBuffer[Long](mostRecentHistory)
    })
    ret
  }

  override def addTupleToBatch(
      tuple: ITuple
  ): Option[(ActorVirtualIdentity, DataPayload)] = {
    val index = selectBatchingIndex(tuple)
    if (recordHistory) {
      var hist = originalReceiverToHistory(bucketsToReceivers(index)(0))
      hist(hist.size - 1) = hist(hist.size - 1) + 1
      tupleIndexForHistory += 1
      if (tupleIndexForHistory % Constants.samplingResetFrequency == 0) {
        originalReceiverToHistory.keys.foreach(rec => {
          originalReceiverToHistory(rec).append(0)
        })
        tupleIndexForHistory = 0
      }
    }

    var receiver: ActorVirtualIdentity = null
    if (bucketsToSharingEnabled(index)) {
      // choose one of the receivers in round robin manner
      // println("GOING ROUND ROBIN")
      receiver = getAndIncrementReceiverForBucket(index)
    } else {
      receiver = getDefaultReceiverForBucket(index)
    }
    receiverToBatch(receiver)(receiverToCurrBatchSize(receiver)) = tuple
    receiverToTotalSent(receiver) = receiverToTotalSent.getOrElse(receiver, 0L) + 1
    receiverToCurrBatchSize(receiver) += 1
    if (receiverToCurrBatchSize(receiver) == batchSize) {
      receiverToCurrBatchSize(receiver) = 0
      val retBatch = receiverToBatch(receiver)
      receiverToBatch(receiver) = new Array[ITuple](batchSize)
      return Some((receiver, DataFrame(retBatch)))
    }
    None
  }

  override def reset(): Unit = {
    initializeInternalState(receivers)
  }

  private[this] def initializeInternalState(_receivers: Array[ActorVirtualIdentity]): Unit = {
    for (i <- 0 until numBuckets) {
      bucketsToReceivers(i) = ArrayBuffer[ActorVirtualIdentity](receivers(i))
      bucketsToSharingEnabled(i) = false
      originalReceiverToHistory(_receivers(i)) = ArrayBuffer[Long](0)
      receiverToBatch(_receivers(i)) = new Array[ITuple](batchSize)
      receiverToCurrBatchSize(_receivers(i)) = 0
    }
  }

}
