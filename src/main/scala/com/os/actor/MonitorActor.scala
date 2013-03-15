package com.os.actor

import akka.actor._
import read.LoadState
import util.{Tick, TimedActor}
import javax.management.ObjectName
import com.os.util.JMXActorBean
import akka.actor.SupervisorStrategy.Stop
import java.util.UUID


/**
 * @author Vadim Bobrov
 */
class MonitorActor(workerProps: Props = Props(new MonitorChildActor())) extends Actor with ActorLogging {

	import context._
	var worker = watch(context.actorOf(workerProps, name = "worker"))

	override def receive: Receive = { case m => worker forward m	}

	// stop monitor on any exceptions
	override val supervisorStrategy = OneForOneStrategy() {	case _ => Stop }
}

trait MonitorChildActorMBean {
	def stop: Unit
	def startMessageListener: Unit
	def stopMessageListener: Unit
	//def saveState: Unit
	//def loadState: Unit

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
case object Done
case class Disable(id: UUID = UUID.randomUUID())
case class Disabled(id: UUID = UUID.randomUUID())
case object Enable
case object StartMessageListener
case object StopMessageListener

class MonitorChildActor extends JMXActorBean with Actor with ActorLogging with TimedActor with TopAware with MonitorChildActorMBean {

	type Monitoring = Map[String, Any]
	override val jmxName = new ObjectName("com.os.chaos:type=Monitor,name=monitor")

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

			// grandpa						dad 					 self
			(sender.path.parent.parent.name, sender.path.parent.name, sender.path.name) match {
				// aggregator
				case ("timeWindow", "worker", _) =>
					rollups += m("rollups").asInstanceOf[Long]
					interpolators += m("interpolators").asInstanceOf[Long]

				// time window
				case (_, "timeWindow", "worker") =>
					timeWindowSize = m("length").asInstanceOf[Int]
					aggregators = m("aggregators").asInstanceOf[Int]
					aggregatorNames = (m("aggregatorNames").asInstanceOf[Traversable[(String, String)]] map ( x => x._1 + "@" + x._2)).toArray

				// message listener
				case (_, "messageProcessor", "worker") =>
					sentMsmt = m("msmt").asInstanceOf[Long]
					receivedBatches = m("batch").asInstanceOf[Long]

				case x =>
					log.debug("monitor received from {}", x)
			}

		case Tick =>
			// zero out because those have to be summed up across multiple messages
			rollups = 0
			interpolators = 0
			top ! Monitor


		case Disable(id) =>
			log.debug("received Disable")
			cancelSchedule()
			sender ! Disabled(id)
	}

}
