package com.os.actor

import akka.actor.{PoisonPill, ActorLogging, Actor}
import read.LoadState
import util.{Tick, TimedActor}
import management.ManagementFactory
import javax.management.ObjectName


/**
 * @author Vadim Bobrov
 */
trait MonitorActorMBean {
	def stop: Unit
	def startMessageListener: Unit
	def stopMessageListener: Unit
	def saveState: Unit
	def loadState: Unit

	def getTimeWindowSize:Long
	def getAggregators:Long
	def getSentMsmt:Long
	def getReceivedBatches:Long
	def getRollups:Long
	def getInterpolators:Long
	def getAggregatorNames: Array[String]
}

case object Monitor
case object GracefulStop
case object SaveState
case object StartMessageListener
case object StopMessageListener

class MonitorActor extends Actor with ActorLogging with TimedActor with TopAware with MonitorActorMBean {

	type Monitoring = Map[String, Any]
	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("com.os.chaos:type=Monitor,name=monitor"))

	@scala.beans.BeanProperty
	var timeWindowSize:Long = 0
	@scala.beans.BeanProperty
	var aggregators:Long = 0
	@scala.beans.BeanProperty
	var sentMsmt:Long = 0
	@scala.beans.BeanProperty
	var receivedBatches:Long = 0

	@scala.beans.BeanProperty
	var rollups:Long = 0
	@scala.beans.BeanProperty
	var interpolators:Long = 0

	@scala.beans.BeanProperty
	var aggregatorNames:Array[String] = Array.empty[String]

	def stop { top ! GracefulStop }

	def startMessageListener { top ! StartMessageListener }
	def stopMessageListener { top ! StopMessageListener }

	def saveState { top ! SaveState }
	def loadState { top ! LoadState }

	override def receive: Receive = {


		case m: Monitoring =>

			if (sender.path.parent.name == "timeWindow") {
				rollups += m("rollups").asInstanceOf[Long]
				interpolators += m("interpolators").asInstanceOf[Long]
			} else
				sender.path.name match {
					case "timeWindow" =>
						timeWindowSize = m("length").asInstanceOf[Int]
						aggregators = m("aggregators").asInstanceOf[Int]
						aggregatorNames = (m("aggregatorNames").asInstanceOf[Traversable[(String, String)]] map ( x => x._1 + "@//" + x._2)).toArray

					case "messageProcessor" =>
						sentMsmt = m("msmt").asInstanceOf[Long]
						receivedBatches = m("batch").asInstanceOf[Long]

					case s =>
						log.debug("monitor received from {}", s)
				}


		case Tick =>
			// zero out because those have to be summed up across multiple messages
			rollups = 0
			interpolators = 0
			top ! Monitor


		case GracefulStop =>
			self ! PoisonPill
	}

}
