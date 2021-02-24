package edu.uci.ics.amber.engine.architecture.deploysemantics.layer

import edu.uci.ics.amber.engine.architecture.deploysemantics.deploystrategy.DeployStrategy
import edu.uci.ics.amber.engine.architecture.deploysemantics.deploymentfilter.DeploymentFilter
import edu.uci.ics.amber.engine.operators.OpExecConfig
import akka.actor.{ActorContext, ActorRef, Address, Deploy, PoisonPill}
import akka.remote.RemoteScope
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.RegisterActorRef
import edu.uci.ics.amber.engine.architecture.worker.{WorkerStatistics, WorkflowWorker}
import edu.uci.ics.amber.engine.common.IOperatorExecutor
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager.{
  Uninitialized,
  WorkerState
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity.WorkerActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  LayerIdentity,
  LinkIdentity
}
import edu.uci.ics.amber.engine.recovery.empty.EmptyMainLogStorage
import edu.uci.ics.amber.engine.recovery.{MainLogStorage, RecoveryManager, SecondaryLogStorage}

import scala.collection.mutable

class WorkerLayer(
    val id: LayerIdentity,
    var metadata: Int => IOperatorExecutor,
    var numWorkers: Int,
    val deploymentFilter: DeploymentFilter,
    val deployStrategy: DeployStrategy
) extends Serializable {

  private val workers = mutable.HashMap[ActorVirtualIdentity, WorkerInfo]()
  private val workerRefs = mutable.HashMap[ActorVirtualIdentity, (Int, ActorRef)]()
  private val inLinks = mutable.HashSet[LinkIdentity]()

  def addInputLink(link: LinkIdentity): Unit = {
    inLinks.add(link)
  }

  def removeInputLink(link: LinkIdentity): Unit = {
    inLinks.remove(link)
  }

  def getAllInputLinks: Iterable[LinkIdentity] = inLinks

  private val startDependencies = mutable.HashSet[LinkIdentity]()

  def startAfter(link: LinkIdentity): Unit = {
    startDependencies.add(link)
  }

  def resolveDependency(link: LinkIdentity): Unit = {
    startDependencies.remove(link)
  }

  def hasDependency(link: LinkIdentity): Boolean = startDependencies.contains(link)

  def canStart: Boolean = startDependencies.isEmpty

  def isBuilt: Boolean = workers != null

  def identifiers: Array[ActorVirtualIdentity] = workers.values.map(_.id).toArray

  def states: Array[WorkerState] = workers.values.map(_.state).toArray

  def statistics: Array[WorkerStatistics] = workers.values.map(_.stats).toArray

  def getWorkerInfo(id: ActorVirtualIdentity): WorkerInfo = workers(id)

  def build(
      prev: Array[(OpExecConfig, WorkerLayer)],
      all: Array[Address],
      parentNetworkCommunicationActorRef: ActorRef,
      context: ActorContext,
      workerToLayer: mutable.HashMap[ActorVirtualIdentity, WorkerLayer]
  ): Unit = {
    deployStrategy.initialize(deploymentFilter.filter(prev, all, context.self.path.address))
    (0 until numWorkers).foreach { i =>
      val workerID = WorkerActorVirtualIdentity(id.toString + s"[$i]")
      workerToLayer(workerID) = this
      val d = deployStrategy.next()
      spawnWorker(
        workerID,
        i,
        d,
        context,
        parentNetworkCommunicationActorRef,
        RecoveryManager.defaultMainLogStorage(workerID),
        RecoveryManager.defaultSecondLogStorage(workerID)
      )
    }
  }

  private[this] def spawnWorker(
      workerID: ActorVirtualIdentity,
      index: Int,
      onNode: Address,
      context: ActorContext,
      parentNetworkCommunicationActorRef: ActorRef,
      mainLogStorage: MainLogStorage,
      secondaryLogStorage: SecondaryLogStorage
  ): Unit = {
    val ref = context.actorOf(
      WorkflowWorker
        .props(
          workerID,
          metadata(index),
          parentNetworkCommunicationActorRef,
          mainLogStorage,
          secondaryLogStorage
        )
        .withDeploy(Deploy(scope = RemoteScope(onNode)))
    )
    parentNetworkCommunicationActorRef ! RegisterActorRef(workerID, ref)
    workers(workerID) = WorkerInfo(workerID, Uninitialized, WorkerStatistics(Uninitialized, 0, 0))
    workerRefs(workerID) = (index, ref)
  }

  def killAndReBuild(
      id: ActorVirtualIdentity,
      onNode: Address,
      context: ActorContext,
      parentNetworkCommunicationActorRef: ActorRef,
      mainLogStorage: MainLogStorage,
      secondaryLogStorage: SecondaryLogStorage
  ): Unit = {
    val (index, ref) = workerRefs(id)
    ref ! PoisonPill
    spawnWorker(
      id,
      index,
      onNode,
      context,
      parentNetworkCommunicationActorRef,
      mainLogStorage,
      secondaryLogStorage
    )
  }

}
