package com.os.actor

import akka.actor._
import read.LoadState
import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.DeadLetter
import akka.actor.OneForOneStrategy
import concurrent.duration._
import akka.util.Timeout
import com.os.Settings
import akka.pattern.ask
import concurrent.Await

/**
 * Top actor
 * responsible for: global fault tolerance, startup and graceful shutdown of actor system
 * @author Vadim Bobrov
 */
case object PostStart
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
	extends Actor with ActorLogging {

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

		self ! PostStart
		super.preStart()
	}

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 20, withinTimeRange = Duration.Inf) {
			case _: Exception     				=> Resume
			case _: Throwable                	=> Escalate
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

		case PostStart =>
			if(Settings().LoadStateOnStartup)
				timeWindow ! LoadState

		case SaveState =>
			log.debug("top received SaveState from " + sender.path)
			children foreach (_ ! SaveState)

		case LoadState =>
			timeWindow ! LoadState

		case GracefulStop =>
			log.debug("top received GracefulStop - stopping top level actors")

			// shut down message listener then have everyone save state
			if (messageListener.isDefined && !messageListener.get.isTerminated)
				Await.ready(messageListener.get ? Disable(), 5 minutes)

			// time window must be flushed before stopping write master
			log.debug("disabling timeWindow")

			Await.ready(timeWindow ? Disable(), 5 minutes) //.onFailure( { case _ => log.debug("failed disabling time window") } )

			if (Settings().SaveStateOnShutdown) {
				log.debug("saving state on shutdown")
				Await.ready(timeWindow ? SaveState, 10 minutes)
			}

			log.debug("disabling writeMaster")
			Await.ready(writeMaster ? Disable(), 10 minutes) //.onFailure( { case _ => log.debug("failed disabling write master") } )

			log.debug("shutting down system")
			system.shutdown()
	}

}

