package com.outsmart.actor.service

import akka.actor.{Terminated, ActorRef, Props}
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.{WriteMasterActor, WriterMasterAwareActor, GracefulStop}
import com.outsmart.Settings
import com.outsmart.actor.{LastMohican, FinalCountDown}


/**
  * @author Vadim Bobrov
  */
class IncomingHandlerActor extends WriterMasterAwareActor with FinalCountDown {

	import context._
	var timeWindowManager = actorOf(Props(new TimeWindowActor()), name = "timeWindow")


	override def preStart() {
		super.preStart()

		// start write master as top level actor
		context.system.actorOf(Props[WriteMasterActor])
	}

	protected def receive: Receive = {

		case msmt : Measurement => {
			writeMaster ! msmt

			// if less than 9.5 minutes old - send to time window manager
			if (System.currentTimeMillis() - msmt.timestamp < Settings.ExpiredTimeWindow)
				timeWindowManager ! msmt

		}

		case GracefulStop =>
			log.debug("incoming handler received graceful stop")
			killChild(timeWindowManager, () => {writeMaster ! GracefulStop} )
			system.shutdown()
	}



}
