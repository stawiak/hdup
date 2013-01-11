package com.outsmart.actor.service

import akka.actor.Props
import com.outsmart.measurement.Measurement
import com.outsmart.actor.write.{WriterMasterAwareActor, GracefulStop}
import com.outsmart.Settings
import com.outsmart.actor.{LastMohican, FinalCountDown}


/**
  * @author Vadim Bobrov
  */
class IncomingHandlerActor extends WriterMasterAwareActor with FinalCountDown with LastMohican {

	import context._
	var timeWindowManager = actorOf(Props(new TimeWindowActor()), name = "timeWindow")

	protected def receive: Receive = {

		case msmt : Measurement => {
			writeMaster ! msmt

			// if less than 9.5 minutes old - send to time window manager
			if (System.currentTimeMillis() - msmt.timestamp < Settings.ExpiredTimeWindow)
				timeWindowManager ! msmt

		}

		case GracefulStop =>
			log.debug("incoming handler received graceful stop")
			// allow time window to stop write master when it's done
			//writeMaster ! GracefulStop
			timeWindowManager ! GracefulStop
		    onBlackSpot()
	}

 }
