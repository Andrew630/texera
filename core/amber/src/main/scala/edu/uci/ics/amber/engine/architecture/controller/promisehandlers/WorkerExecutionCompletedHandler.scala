package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowCompleted
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.{
  WorkerExecutionCompleted,
  getReadableTime
}
import edu.uci.ics.amber.engine.architecture.controller.{ControllerAsyncRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.engine.operators.SinkOpExecConfig

import scala.collection.mutable

object WorkerExecutionCompletedHandler {
  private def getReadableTime(nanos: Long): String = {
    val tempSec = nanos / (1000 * 1000 * 1000)
    val sec = tempSec % 60
    val min = (tempSec / 60) % 60
    val hour = (tempSec / (60 * 60)) % 24
    val day = (tempSec / (24 * 60 * 60)) % 24
    day.toString + ":" + hour.toString + ":" + min.toString + ":" + sec.toString
  }

  final case class WorkerExecutionCompleted() extends ControlCommand[Unit]
}

/** indicate a worker has completed its job
  * i.e. received and processed all data from upstreams
  * note that this doesn't mean all the output of this worker
  * has been received by the downstream workers.
  *
  * possible sender: worker
  */
trait WorkerExecutionCompletedHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: WorkerExecutionCompleted, sender) =>
    {
      assert(sender.isInstanceOf[ActorVirtualIdentity])
      operatorEndTime(workflow.getOperator(sender).id) = System.nanoTime()
      workerEndTime(sender) = System.nanoTime()

      // after worker execution is completed, query statistics immediately one last time
      // because the worker might be killed before the next query statistics interval
      // and the user sees the last update before completion
      val statsRequests = new mutable.MutableList[Future[Unit]]()
      statsRequests += execute(ControllerInitiateQueryStatistics(Option(List(sender))), CONTROLLER)

      Future
        .collect(statsRequests)
        .flatMap(_ => {
          // if entire workflow is completed, clean up
          if (workflow.isCompleted) {
            workflowEndTime = System.nanoTime()
            println(
              s"\tTOTAL EXECUTION TIME FOR WORKFLOW ${(workflowEndTime - workflowStartTime) / 1e9d}s from ${getReadableTime(workflowStartTime)} to ${getReadableTime(workflowEndTime)}"
            )

            operatorStartTime.keys.foreach(opID => {
              if (operatorEndTime.contains(opID)) {
                println(s"\tTOTAL EXECUTION FOR OPERATOR ${opID
                  .toString()} = ${(operatorEndTime(opID) - operatorStartTime(opID)) / 1e9d}s from ${getReadableTime(
                  operatorStartTime(opID)
                )} to ${getReadableTime(operatorEndTime(opID))}")
              }
              workflow
                .getOperator(opID)
                .getAllWorkers
                .foreach(worker => {
                  println(
                    s"\t\tSTATISTICS FOR ${worker.toString()}: ${workflow.getWorkerInfo(worker).stats.inputTupleCount}:${workflow.getWorkerInfo(worker).stats.outputTupleCount} in ${(workerEndTime(worker) - workerStartTime(
                      worker
                    )) / 1e9d}s from ${getReadableTime(workerStartTime(worker))} to ${getReadableTime(workerEndTime(worker))}"
                  )
                })
            })
            // after query result come back: send completed event, cleanup ,and kill workflow
            sendToClient(WorkflowCompleted())
            disableStatusUpdate()
            disableMonitoring()
            disableSkewHandling()
            Future.Done
          } else {
            Future.Done
          }
        })
    }
  }
}
