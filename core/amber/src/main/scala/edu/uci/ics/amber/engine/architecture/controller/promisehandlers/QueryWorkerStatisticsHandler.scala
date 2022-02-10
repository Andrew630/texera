package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.Calendar

object QueryWorkerStatisticsHandler {

  final case class ControllerInitiateQueryStatistics(
      filterByWorkers: Option[List[ActorVirtualIdentity]] = None
  ) extends ControlCommand[Unit]

}

/** Get statistics from all the workers
  *
  * possible sender: controller(by statusUpdateAskHandle)
  */
trait QueryWorkerStatisticsHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler((msg: ControllerInitiateQueryStatistics, sender) => {
    // send to specified workers (or all workers by default)
    val workers = msg.filterByWorkers.getOrElse(workflow.getAllWorkers).toList

    // send QueryStatistics message
    val requests = workers.map(worker =>
      // must immediately update worker state and stats after reply
      send(QueryStatistics(), worker).map(res => {
        if (workerStatisticsTime.contains(worker)) {
          val oldInputCount = workflow.getOperator(worker).getWorker(worker).stats.inputTupleCount
          val newInputCount = res.inputTupleCount
          val oldOutputCount = workflow.getOperator(worker).getWorker(worker).stats.outputTupleCount
          val newOutputCount = res.outputTupleCount

          val now = Calendar.getInstance()
          val hh = now.get(Calendar.HOUR)
          val mm = now.get(Calendar.MINUTE)
          val ss = now.get(Calendar.SECOND)

          println(
            s"\t THROUGHPUT for ${worker.toString()} at ${hh}:${mm}:${ss}: Input ${(newInputCount - oldInputCount) / (res.timeSpentNanoSec / 1e9d)}," +
              s" Output ${(newOutputCount - oldOutputCount) / ((res.timeSpentNanoSec) / 1e9d)}"
          )
        }
        workflow.getOperator(worker).getWorker(worker).state = res.workerState
        workflow.getOperator(worker).getWorker(worker).stats = res
        workerStatisticsTime(worker) = System.nanoTime()
      })
    )

    // wait for all workers to reply before notifying frontend
    Future
      .collect(requests)
      .map(_ => sendToClient(WorkflowStatusUpdate(workflow.getWorkflowStatus)))
  })
}
