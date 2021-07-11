package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.messaginglayer.{ControlOutputPort, DataOutputPort}
import edu.uci.ics.amber.engine.architecture.worker.promisehandler2.WorkerStateInfo
import edu.uci.ics.amber.engine.common.ambermessage.{DataFrame, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambermessage2.{DataHeader, WorkflowControlMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import kotlin.NotImplementedError
import org.apache.arrow.flight._
import org.apache.arrow.flight.example.InMemoryStore
import org.apache.arrow.memory.{BufferAllocator, RootAllocator}
import org.apache.arrow.util.AutoCloseables

import scala.collection.mutable

private class AmberProducer(
    allocator: BufferAllocator,
    location: Location,
    controlOutputPort: ControlOutputPort,
    dataOutputPort: DataOutputPort
) extends InMemoryStore(allocator, location) {

  override def doAction(
      context: FlightProducer.CallContext,
      action: Action,
      listener: FlightProducer.StreamListener[Result]
  ): Unit = {
    action.getType match {
      case "control" =>
        val workflowControlMessage = WorkflowControlMessage.parseFrom(action.getBody)

        workflowControlMessage.payload match {
          case returnPayload: edu.uci.ics.amber.engine.common.ambermessage2.ReturnPayload =>
            val k = returnPayload.returnValue match {
              case workerStateInfo: WorkerStateInfo =>
                workerStateInfo.workerState
              case a => a
            }
            controlOutputPort.sendTo(
              to = workflowControlMessage.from,
              payload = ReturnPayload(
                originalCommandID = returnPayload.originalCommandID,
                returnValue = k
              )
            )

          case controlInvocation: edu.uci.ics.amber.engine.common.ambermessage2.ControlInvocation =>
            controlInvocation.command match {
              case _: edu.uci.ics.amber.engine.architecture.worker.promisehandler2.WorkerExecutionCompleted =>
                controlOutputPort.sendTo(
                  to = CONTROLLER,
                  payload = ControlInvocation(
                    controlInvocation.commandID,
                    command = WorkerExecutionCompleted()
                  )
                )
            }

        }

        listener.onNext(new Result("ack".getBytes))
        listener.onCompleted()
      case _ => throw new NotImplementedError()
    }

  }

  override def acceptPut(
      context: FlightProducer.CallContext,
      flightStream: FlightStream,
      ackStream: FlightProducer.StreamListener[PutResult]
  ): Runnable = { () =>
    {

      val dataHeader: DataHeader = DataHeader
        .parseFrom(flightStream.getDescriptor.getCommand)
      val to: ActorVirtualIdentity = dataHeader.from
      val isEnd: Boolean = dataHeader.isEnd

      val root = flightStream.getRoot
      try {
        // consume all data in the stream, it will store on the root vectors.
        while (flightStream.next) {
          ackStream.onNext(PutResult.metadata(flightStream.getLatestMetadata))
        }
        // closing the stream will release the dictionaries
        flightStream.takeDictionaryOwnership

        if (isEnd) {
          // EndOfUpstream
          dataOutputPort.sendTo(to, EndOfUpstream())
        } else {
          // normal data batches
          val queue = mutable.Queue[Tuple]()
          for (i <- 0 until root.getRowCount)
            queue.enqueue(ArrowUtils.getTexeraTuple(i, root))
          dataOutputPort.sendTo(to, DataFrame(queue.toArray))
        }

      } catch {
        case e: Exception =>
          e.printStackTrace()
      }

    }

  }

}

class PythonProxyServer(
    portNumber: Int,
    controlOutputPort: ControlOutputPort,
    dataOutputPort: DataOutputPort
) extends Runnable
    with AutoCloseable {

  val allocator: BufferAllocator =
    new RootAllocator().newChildAllocator("flight-server", 0, Long.MaxValue);
  val location: Location = Location.forGrpcInsecure("localhost", portNumber)
  val mem: InMemoryStore = new AmberProducer(allocator, location, controlOutputPort, dataOutputPort)
  val server: FlightServer = FlightServer.builder(allocator, location, mem).build()

  override def run(): Unit = {
    server.start()
  }

  @throws[Exception]
  override def close(): Unit = {
    AutoCloseables.close(mem, server, allocator)
  }

}