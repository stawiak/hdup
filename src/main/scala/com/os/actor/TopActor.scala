package com.os.actor

import akka.actor._
import read.LoadState
import util._
import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.DeadLetter
import akka.actor.OneForOneStrategy
import concurrent.duration._
import akka.util.Timeout
import com.os.Settings
import concurrent.{Future, Await}
import akka.pattern.gracefulStop

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
				  val deadLetterListenerProps: Props,
				  val monitorProps: Props
				  )
	extends FinalCountDown with LastMohican {

	import context._
	implicit val timeout: Timeout = 10 seconds

	var mqlHandler: ActorRef = _
	var timeWindow: ActorRef = _
	var readMaster: ActorRef = _
	var writeMaster: ActorRef = _
	var messageListener: Option[ActorRef] = None
	var webService: ActorRef = _
	var deadLetterListener: ActorRef = _
	var monitor: ActorRef = _


	override def preStart() {
		// start top level actors
		mqlHandler = actorOf(mqlHandlerProps, name = "mqlHandler")
		timeWindow = actorOf(timeWindowProps, name = "timeWindow")
		readMaster = actorOf(readMasterProps, name = "readMaster")
		writeMaster = actorOf(writeMasterProps, name = "writeMaster")
		// not started by default
		//messageListener = Some(actorOf(messageListenerProps, name = "messageListener"))
		webService = actorOf(webServiceProps, name = "webService")
		deadLetterListener = actorOf(deadLetterListenerProps, name = "deadLetterListener")
		monitor = actorOf(monitorProps, name = "monitor")

		system.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])

		super.preStart()
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Resume
			case _: Throwable                	⇒ Escalate
		}


	override def receive: Receive = {

		case Monitor =>
			timeWindow forward Monitor
			if (messageListener.isDefined && !messageListener.get.isTerminated)
				messageListener.get forward Monitor
			writeMaster forward Monitor

		case GetWebService =>
			sender ! webService

		case StartMessageListener =>
			if (!messageListener.isDefined || messageListener.get.isTerminated)
				messageListener = Some(actorOf(messageListenerProps, name = "messageListener"))

		case StopMessageListener =>
			if (messageListener.isDefined && !messageListener.get.isTerminated)
				messageListener.get ! GracefulStop

		case SaveState =>
			log.debug("top received SaveState from " + sender.path)
			children foreach (_ ! SaveState)

		case LoadState =>
			timeWindow ! LoadState

		case GracefulStop =>
			log.debug("top received GracefulStop - stopping top level actors")

			// shut down message listener then have everyone save state
			if (messageListener.isDefined && !messageListener.get.isTerminated)
				syncKill(messageListener.get)

			//TODO: is this synchronous? can we proceed?
			if (Settings().SaveStateOnShutdown) {
				log.debug("saving state on shutdown")
				children foreach (_ ! SaveState)
			}

			// time window must be flushed before stopping write master
			log.debug("killing timeWindow")
			syncKill(timeWindow)
			log.debug("killing writeMaster")
			syncKill(writeMaster)

			// do the rest
			log.debug("killing rest")
			children foreach (_ ! GracefulStop)
			waitAndDie()

	}

}

