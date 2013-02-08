package com.os.actor

import akka.actor.{ActorRef, Props}
import read.ReadMasterActor
import service.TimeWindowActor
import util._
import write.WriteMasterActor
import concurrent.duration.Duration
import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.DeadLetter
import akka.actor.OneForOneStrategy

/**
 * Top actor
 * responsible for: global fault tolerance, startup and graceful shutdown of actor system
 * @author Vadim Bobrov
 */
case object GetWebService
class TopActor(   // props of top-level actors to start
				  val timeWindowProps: Props,
				  val readMasterProps: Props,
				  val writeMasterProps: Props,
				  val messageListenerProps: Props,
				  val webServiceProps: Props,
				  val deadLetterListenerProps: Props
				  )
	extends FinalCountDown with LastMohican with SettingsUse {

	import context._

	var timeWindow: ActorRef = _
	var readMaster: ActorRef = _
	var writeMaster: ActorRef = _
	var messageListener: ActorRef = _
	var webService: ActorRef = _
	var deadLetterListener: ActorRef = _


	override def preStart() {
		// start top level actors
		timeWindow = actorOf(timeWindowProps, name = "timeWindow")
		readMaster = actorOf(readMasterProps, name = "readMaster")
		writeMaster = actorOf(writeMasterProps, name = "writeMaster")
		messageListener = actorOf(messageListenerProps, name = "jmsListener")
		webService = actorOf(webServiceProps, name = "webService")
		deadLetterListener = actorOf(deadLetterListenerProps, name = "deadLetterListener")

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

