package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object BackpressureHandler {
  final case class Backpressure(enableBackpressure: Boolean) extends ControlCommand[Unit]
}

/** Get queue and other resource usage of this worker
  *
  * possible sender: controller(by ControllerInitiateMonitoring)
  */
trait BackpressureHandler {
  this: WorkerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: Backpressure, _) =>
    println(s"\t BACKPRESSURE received by ${dataProcessor.actorId} with ${msg.enableBackpressure}")
    if (msg.enableBackpressure) {
      dataProcessor.disableDataQueue()
      dataProcessor.backpressured = true
    } else {
      dataProcessor.enableDataQueue()
      dataProcessor.backpressured = false
    }
  }

}
