package edu.uci.ics.amber.engine.common

import scala.concurrent.duration._

object Constants {
  val defaultBatchSize = 400
  val remoteHDFSPath = "hdfs://10.138.0.2:8020"
  val remoteHDFSIP = "10.138.0.2"
  var defaultNumWorkers = 0
  var dataset = 0
  var masterNodeAddr: String = null

  var dataVolumePerNode = 10
  var defaultTau: FiniteDuration = 10.milliseconds

  var numWorkerPerNode = 4
  // join-skew reserach related
  val gcpExp: Boolean = true
  val samplingResetFrequency: Int = 2000
  val startDetection: FiniteDuration = 100.milliseconds
  val detectionPeriod: FiniteDuration = 2.seconds
  val printResultsInConsole: Boolean = true

  // sort-skew research related
  val eachTransferredListSize: Int = 100000
  val lowerLimit: Float = 0f
  val upperLimit: Float = 600000f // 30gb

  val sortExperiment: Boolean = false
  val onlyDetectSkew: Boolean = false
  val threshold: Int = 100
  val singleIterationOnly: Boolean = false
  val freeSkewedThreshold: Int = 100
  val firstPhaseNum = 4
  val firstPhaseDen = 5

  type joinType = String
}
