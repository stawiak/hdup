package com.os.actor

import akka.actor._
import util._
import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.DeadLetter
import akka.actor.OneForOneStrategy
import concurrent.duration._
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

/**
 * Top actor
 * responsible for: global fault tolerance, startup and graceful shutdown of actor system
 * @author Vadim Bobrov
 */
case object GetWebService
class TopActor(   // props of top-level actors to start
				  val mqlHandlerProps: Props,
				  val timeWindowProps: Props,
				  val readMasterProps: Props,
				  val writeMasterProps: Props,
				  val messageListenerProps: Props,
				  val webServiceProps: Props,
				  val deadLetterListenerProps: Props
				  )
	extends FinalCountDown with LastMohican with SettingsUse {

	import context._
	implicit val timeout: Timeout = 10 seconds

	var mqlHandler: ActorRef = _
	var timeWindow: ActorRef = _
	var readMaster: ActorRef = _
	var writeMaster: ActorRef = _
	var messageListener: ActorRef = _
	var webService: ActorRef = _
	var deadLetterListener: ActorRef = _

	var monitor: ActorRef = _


	override def preStart() {
		// start top level actors
		mqlHandler = actorOf(mqlHandlerProps, name = "mqlHandler")
		timeWindow = actorOf(timeWindowProps, name = "timeWindow")
		readMaster = actorOf(readMasterProps, name = "readMaster")
		writeMaster = actorOf(writeMasterProps, name = "writeMaster")
		messageListener = actorOf(messageListenerProps, name = "jmsListener")
		webService = actorOf(webServiceProps, name = "webService")
		deadLetterListener = actorOf(deadLetterListenerProps, name = "deadLetterListener")

		monitor = actorOf(Props[MonitorActor], name = "monitor")

		system.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	override def receive: Receive = {

		case Monitor =>
			timeWindow forward Monitor
			messageListener forward Monitor
			writeMaster forward Monitor

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

