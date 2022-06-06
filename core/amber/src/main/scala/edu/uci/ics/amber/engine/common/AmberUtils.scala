package edu.uci.ics.amber.engine.common

import akka.actor.{ActorSystem, Address, DeadLetter, Props}
import com.typesafe.config.{Config, ConfigFactory}
import edu.uci.ics.amber.clustering.ClusterListener
import edu.uci.ics.amber.engine.architecture.messaginglayer.DeadLetterMonitorActor
import org.apache.commons.jcs3.access.exception.InvalidArgumentException

import java.io.{BufferedReader, InputStreamReader}

object AmberUtils {

  def reverseMultimap[T1, T2](map: Map[T1, Set[T2]]): Map[T2, Set[T1]] =
    map.toSeq
      .flatMap { case (k, vs) => vs.map((_, k)) }
      .groupBy(_._1)
      .mapValues(_.map(_._2).toSet)

  def startActorMaster(mainNodeAddress: Option[String]): ActorSystem = {
    val masterIp = mainNodeAddress.getOrElse("localhost")
    println(s"starting texera master in ${if(mainNodeAddress.isDefined)"cluster" else "local"} mode , ip addr = "+masterIp)
    val masterConfig = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.port = 2552
        akka.remote.artery.canonical.hostname = $masterIp
        akka.cluster.seed-nodes = [ "akka://Amber@$masterIp:2552" ]
        """)
      .withFallback(akkaConfig)
    Constants.masterNodeAddr = createMasterAddress(masterIp)
    createAmberSystem(masterConfig)
  }

  def akkaConfig: Config = ConfigFactory.load("cluster").withFallback(amberConfig)

  def amberConfig: Config = ConfigFactory.load()

  def createMasterAddress(addr: String): Address = Address("akka", "Amber", addr, 2552)

  def startActorWorker(mainNodeAddress: Option[String], workerNodeAddress: Option[String]): ActorSystem = {
    val masterIp = mainNodeAddress.getOrElse("localhost")
    var workerIp = workerNodeAddress.getOrElse("localhost")
    if(mainNodeAddress.isDefined != workerNodeAddress.isDefined){
      throw new InvalidArgumentException("cluster mode require both main node and worker ip addresses")
    }
    val workerConfig = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = $workerIp
        akka.remote.artery.canonical.port = 0
        akka.cluster.seed-nodes = [ "akka://Amber@$masterIp:2552" ]
        """)
      .withFallback(akkaConfig)
    Constants.masterNodeAddr = createMasterAddress(masterIp)
    createAmberSystem(workerConfig)
  }

  def createAmberSystem(actorSystemConf: Config): ActorSystem = {
    val system = ActorSystem("Amber", actorSystemConf)
    system.actorOf(Props[ClusterListener], "cluster-info")
    val deadLetterMonitorActor =
      system.actorOf(Props[DeadLetterMonitorActor], name = "dead-letter-monitor-actor")
    system.eventStream.subscribe(deadLetterMonitorActor, classOf[DeadLetter])
    system
  }
}
