package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{
  CONTROL_QUEUE,
  ControlElement,
  DATA_QUEUE,
  EndMarker,
  EndOfAllMarker,
  InputTuple,
  InternalQueueElement
}
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataFrame, EndOfUpstream}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}
import lbmq.LinkedBlockingMultiQueue

import scala.collection.mutable

object WorkerInternalQueue {
  final val DATA_QUEUE = 1
  final val CONTROL_QUEUE = 0

  // 4 kinds of elements can be accepted by internal queue
  sealed trait InternalQueueElement

  case class InputTuple(from: ActorVirtualIdentity, tuple: ITuple) extends InternalQueueElement

  case class SenderChangeMarker(newUpstreamLink: LinkIdentity) extends InternalQueueElement

  case class ControlElement(payload: ControlPayload, from: ActorVirtualIdentity)
      extends InternalQueueElement

  case object EndMarker extends InternalQueueElement

  case object EndOfAllMarker extends InternalQueueElement

}

/** Inspired by the mailbox-ed thread, the internal queue should
  * be a part of DP thread.
  */
trait WorkerInternalQueue {

  private val lbmq = new LinkedBlockingMultiQueue[Int, InternalQueueElement]()

  lbmq.addSubQueue(DATA_QUEUE, DATA_QUEUE)
  lbmq.addSubQueue(CONTROL_QUEUE, CONTROL_QUEUE)

  private val dataQueue = lbmq.getSubQueue(DATA_QUEUE)

  private val controlQueue = lbmq.getSubQueue(CONTROL_QUEUE)

  // the credits in the `inputToCredits` map are in tuples (not batches)
  private var inputTuplesPutIn = new mutable.HashMap[ActorVirtualIdentity, Long]()
  @volatile private var inputTuplesTakenOut = new mutable.HashMap[ActorVirtualIdentity, Long]()

  def getSenderCredits(sender: ActorVirtualIdentity): Int = {
    val x =
      inputTuplesPutIn.getOrElseUpdate(sender, 0L) - inputTuplesTakenOut.getOrElseUpdate(sender, 0L)
    if (x < 0) {
      println(s"\t\t IT's STILL HAPPENING ${x}")
    }

    if (sender.toString().contains("Scan")) {
      (Constants.unprocessedBatchesCreditLimitPerSender * Constants.defaultBatchSize - (inputTuplesPutIn
        .getOrElseUpdate(sender, 0L) - inputTuplesTakenOut.getOrElseUpdate(
        sender,
        0L
      )).toInt) / Constants.defaultBatchSize

    } else {
      (Constants.unprocessedBatchesCreditLimitPerSender * Constants.defaultBatchSize - (inputTuplesPutIn
        .getOrElseUpdate(sender, 0L) - inputTuplesTakenOut.getOrElseUpdate(
        sender,
        0L
      )).toInt) / Constants.defaultBatchSize
    }
  }

  def appendElement(elem: InternalQueueElement): Unit = {
    if (Constants.flowControlEnabled) {
      elem match {
        case InputTuple(from, _) =>
          if (from.toString().contains("Scan")) {
            inputTuplesPutIn(from) = inputTuplesPutIn.getOrElseUpdate(from, 0L) + 1
          } else {
            inputTuplesPutIn(from) = inputTuplesPutIn.getOrElseUpdate(from, 0L) + 1
          }

        case _ =>
        // do nothing
      }
    }
    dataQueue.add(elem)
  }

  def enqueueCommand(payload: ControlPayload, from: ActorVirtualIdentity): Unit = {
    controlQueue.add(ControlElement(payload, from))
  }

  def getElement: InternalQueueElement = {
    val elem = lbmq.take()
    if (Constants.flowControlEnabled) {
      elem match {
        case InputTuple(from, _) =>
          if (from.toString().contains("Scan")) {
            inputTuplesTakenOut(from) = inputTuplesTakenOut.getOrElseUpdate(from, 0L) + 1
          } else {
            inputTuplesTakenOut(from) = inputTuplesTakenOut.getOrElseUpdate(from, 0L) + 1
          }
        case _ =>
        // do nothing

      }
    }
    elem
  }

  def disableDataQueue(): Unit = dataQueue.enable(false)

  def enableDataQueue(): Unit = dataQueue.enable(true)

  def getDataQueueLength: Int = dataQueue.size()

  def getControlQueueLength: Int = controlQueue.size()

  def isControlQueueEmpty: Boolean = controlQueue.isEmpty

}
