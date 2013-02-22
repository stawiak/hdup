package com.os.actor

import akka.actor.{PoisonPill, ActorLogging, Actor}
import util.{GracefulStop, Tick, TimedActor}
import management.ManagementFactory
import javax.management.ObjectName


/**
 * @author Vadim Bobrov
 */
trait MonitorActorMBean {
	def getTimeWindowSize:Long
	def getAggregators:Long
	def getSentMsmt:Long
	def getReceivedBatches:Long
}

case object Monitor

class MonitorActor extends Actor with ActorLogging with TimedActor with TopAware with MonitorActorMBean {
	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("Dynamic:name=Data"))

	@scala.beans.BeanProperty
	var timeWindowSize:Long = 0
	@scala.beans.BeanProperty
	var aggregators:Long = 0
	@scala.beans.BeanProperty
	var sentMsmt:Long = 0
	@scala.beans.BeanProperty
	var receivedBatches:Long = 0

	override def receive: Receive = {

		case m: Map[String, Long] =>
			sender.path.name match {
				case "timeWindow" =>
					timeWindowSize = m("length")
					aggregators = m("aggregators")

				case "messageProcessor" =>
					sentMsmt = m("msmt")
					receivedBatches = m("batch")

				case s =>
					log.debug("monitor received from {}", s)
			}


		case Tick =>
			top ! Monitor


		case GracefulStop =>
			self ! PoisonPill
	}
}
