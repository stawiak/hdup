package com.outsmart.actor.service

import akka.actor.{ActorRef, Props}
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.{WriteMasterActor, WriterMasterAwareActor}
import com.outsmart.Settings
import com.outsmart.actor.{GracefulStop, FinalCountDown}


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
		writeMaster = context.system.actorOf(Props[WriteMasterActor], name = "writeMaster")
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
