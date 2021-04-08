package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{ActorRef, Address, Cancellable, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.softwaremill.macwire.wire
import com.twitter.util.Future
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.clustering.ClusterRuntimeInfo
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{ErrorOccurred, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LinkWorkflowHandler.LinkWorkflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, RegisterActorRef}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnPayload}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager.Ready
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.recovery.RecoveryManager.RecoveryMessage
import edu.uci.ics.amber.engine.recovery.{ControlLogManager, LogStorage, RecoveryManager}
import edu.uci.ics.amber.error.WorkflowRuntimeError

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object Controller {

  def props(
      id: WorkflowIdentity,
      workflow: Workflow,
      eventListener: ControllerEventListener,
      statusUpdateInterval: Long,
      controlLogStorage: LogStorage[WorkflowControlMessage] =
        RecoveryManager.defaultControlLogStorage(ActorVirtualIdentity.Controller),
      parentNetworkCommunicationActorRef: ActorRef = null
  ): Props =
    Props(
      new Controller(
        id,
        workflow,
        eventListener,
        Option.apply(statusUpdateInterval),
        controlLogStorage,
        parentNetworkCommunicationActorRef
      )
    )
}

class Controller(
    val id: WorkflowIdentity,
    val workflow: Workflow,
    val eventListener: ControllerEventListener = ControllerEventListener(),
    val statisticsUpdateIntervalMs: Option[Long],
    logStorage: LogStorage[WorkflowControlMessage],
    parentNetworkCommunicationActorRef: ActorRef
) extends WorkflowActor(
      ActorVirtualIdentity.Controller,
      parentNetworkCommunicationActorRef
    ) {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // register controller itself
  networkCommunicationActor ! RegisterActorRef(ActorVirtualIdentity.Controller, self)

  // build whole workflow
  workflow.build(availableNodes, networkCommunicationActor, context)

  ClusterRuntimeInfo.controllers.add(self)
  val startTime = System.nanoTime()

  val rpcHandlerInitializer = new ControllerAsyncRPCHandlerInitializer(this)
  val controlLogManager: ControlLogManager = wire[ControlLogManager]
  val recoveryManager = wire[RecoveryManager]

  private def errorLogAction(err: WorkflowRuntimeError): Unit = {
    eventListener.workflowExecutionErrorListener.apply(ErrorOccurred(err))
  }

  logger.setErrorLogAction(errorLogAction)

  var statusUpdateAskHandle: Cancellable = _

  controlLogManager.onComplete(()=>{
    // activate all links
    controlOutputPort.sendTo(ActorVirtualIdentity.Self, ControlInvocation(-1, LinkWorkflow()))
  })

  def availableNodes: Array[Address] =
    Await
      .result(context.actorSelection("/user/cluster-info") ? GetAvailableNodeAddresses, 5.seconds)
      .asInstanceOf[Array[Address]]

  override def receive: Receive = initializing

  def initializing: Receive = {
    processRecoveryMessages orElse {
      case NetworkMessage(
            id,
            cmd @ WorkflowControlMessage(from, sequenceNumber, payload: ReturnPayload)
          ) =>
        //process reply messages
        controlLogManager.persistControlMessage(cmd)
        sender ! NetworkAck(id)
        handleControlMessageWithTryCatch(cmd)
      case NetworkMessage(
            id,
            cmd @ WorkflowControlMessage(ActorVirtualIdentity.Controller, sequenceNumber, payload)
          ) =>
        //process control messages from self
        controlLogManager.persistControlMessage(cmd)
        sender ! NetworkAck(id)
        handleControlMessageWithTryCatch(cmd)
      case msg =>
        stash() //prevent other messages to be executed until initialized
    }
  }

  def running: Receive = {
    acceptDirectInvocations orElse
      processControlMessages orElse
      processRecoveryMessages orElse {
      case other =>
        logger.logInfo(s"unhandled message: $other")
    }
  }

  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      controlLogManager.persistControlMessage(WorkflowControlMessage(ActorVirtualIdentity.Client,-1,invocation))
      asyncRPCServer.receive(invocation, ActorVirtualIdentity.Client)
  }

  override def postStop(): Unit = {
    if (statusUpdateAskHandle != null) {
      statusUpdateAskHandle.cancel()
    }
    workflow.cleanupResults()
    ClusterRuntimeInfo.controllers.remove(self)
    val timeSpent = (System.nanoTime()-startTime).asInstanceOf[Double]/1000000000
    logger.logInfo("workflow finished in "+timeSpent+" seconds")
    super.postStop()
  }

  def processRecoveryMessages: Receive = {
    case NetworkMessage(id, msg: RecoveryMessage) =>
      sender ! NetworkAck(id)
      msg match {
        case RecoveryManager.TriggerRecovery(addr) =>
          val targetNode = availableNodes.head
          recoveryManager.recoverWorkerOnNode(addr, targetNode)
        case RecoveryManager.RecoveryCompleted(id) =>
          recoveryManager.setRecoverCompleted(id)
      }
  }

}
