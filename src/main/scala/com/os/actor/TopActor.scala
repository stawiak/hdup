package com.os.actor

import akka.actor.{ActorRef, OneForOneStrategy, DeadLetter, Props}
import read.ReadMasterActor
import service.TimeWindowActor
import write.WriteMasterActor
import com.os.Settings
import concurrent.duration.Duration
import akka.actor.SupervisorStrategy.{Escalate, Resume}

/**
 * Top actor
 * responsible for: global fault tolerance, startup and graceful shutdown of actor system
 * @author Vadim Bobrov
 */
case object GetWebService
class TopActor  extends FinalCountDown with LastMohican {

	import context._

	var timeWindow: ActorRef = _
	var readMaster: ActorRef = _
	var writeMaster: ActorRef = _
	var messageListener: ActorRef = _
	var webService: ActorRef = _
	var deadLetterListener: ActorRef = _


	override def preStart() {
		// start top level actors
		timeWindow = actorOf(Props(new TimeWindowActor()), name = "timeWindow")
		readMaster = actorOf(Props[ReadMasterActor], name = "readMaster")
		writeMaster = actorOf(Props[WriteMasterActor], name = "writeMaster")
		messageListener = actorOf(Props(new MessageListenerActor(Settings.ActiveMQHost, Settings.ActiveMQPort, Settings.ActiveMQQueue)), name = "jmsListener")
		webService = actorOf(Props[WebServiceActor], name = "webService")
		deadLetterListener = actorOf(Props[DeadLetterListener], name = "deadLetterListener")

		system.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])
	}


	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	override def receive: Receive = {

		case GetWebService =>
			sender ! webService

		case GracefulStop =>
			log.debug("top received graceful stop - stopping top level actors")
			// time window must be flushed before stopping write master
			killChild(timeWindow, () => killChild(writeMaster))
			// do the rest
			children foreach (_ ! GracefulStop)
			waitAndDie()

	}

}

