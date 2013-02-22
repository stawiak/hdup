package com.os.actor

import akka.actor.{PoisonPill, ActorLogging, Actor}
import util.{GracefulStop, Tick, TimedActor}
import management.ManagementFactory
import javax.management.ObjectName


/**
 * @author Vadim Bobrov
 */
trait MonitorActorMBean {
	def getTimeWindowSize:Int
	def getAggregators: Int
}
case object Monitor
class MonitorActor extends Actor with ActorLogging with TimedActor with TopAware with MonitorActorMBean {
	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("Dynamic:name=Data"))

	@scala.beans.BeanProperty
	var timeWindowSize = 0

	@scala.beans.BeanProperty
	var aggregators = 0

	override def receive: Receive = {

		case m: Map[String, Int] =>
			sender.path.name match {
				case "timeWindow" =>
					timeWindowSize = m("length")
					aggregators = m("aggregators")

				case s =>
					log.debug("monitor received from {}", s)
			}


		case Tick =>
			top ! Monitor


		case GracefulStop =>
			self ! PoisonPill
	}
}
