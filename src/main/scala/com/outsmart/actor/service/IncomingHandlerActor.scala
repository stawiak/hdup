package com.outsmart.actor.service

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.{WriteMasterActor, WriterMasterAwareActor}
import com.outsmart.Settings
import com.outsmart.actor.{GracefulStop, FinalCountDown}
import akka.actor.DeadLetter
import akka.dispatch.Terminate


/**
  * @author Vadim Bobrov
  */
class IncomingHandlerActor extends FinalCountDown {

	import context._
	var timeWindowManager = actorOf(Props(new TimeWindowActor()), name = "timeWindow")
	// cannot be taken from trait as it won't be created yet
	var writeMaster: ActorRef = _

	override def preStart() {
		// start write master as top level actor
		writeMaster = system.actorOf(Props[WriteMasterActor], name = "writeMaster")

		val listener = system.actorOf(Props[DeadLetterListener])
		system.eventStream.subscribe(listener, classOf[DeadLetter])
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			writeMaster ! msmt

			// if less than 9.5 minutes old - send to time window manager
			if (System.currentTimeMillis() - msmt.timestamp < Settings.ExpiredTimeWindow)
				timeWindowManager ! msmt

		}

		case GracefulStop =>
			log.debug("incoming handler received graceful stop - stopping time window and then write master")
			killChild(timeWindowManager, () => killChild(writeMaster, () => system.shutdown()) )
	}

}
class DeadLetterListener extends Actor with ActorLogging {

	var lostMsmt = 0

	def receive = {
		case Terminate => ;
		case Terminated => ;
		case DeadLetter(msmt: Measurement, sender: ActorRef, recipient: ActorRef) =>  {

			lostMsmt += 1
			log.debug("lost " + lostMsmt )
			log.debug("dead letter: " + sender + " " + recipient)
		}
	}

}
