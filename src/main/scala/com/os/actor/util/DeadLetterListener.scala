package com.os.actor.util

import akka.actor._
import com.os.measurement.Measurement
import akka.actor.Terminated
import akka.actor.DeadLetter
import com.os.actor.GracefulStop

/**
 * @author Vadim Bobrov
 */
class DeadLetterListener extends Actor with ActorLogging {

	var lostMsmt = 0

	def receive = {
		case Terminated => ;
		case DeadLetter(msmt: Measurement, sender: ActorRef, recipient: ActorRef) =>  {

			lostMsmt += 1
			log.debug("lost " + lostMsmt )
			log.debug("dead letter: " + sender + " " + recipient + " " + msmt)
		}

		case GracefulStop =>
			self ! PoisonPill
	}

}
