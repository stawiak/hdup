package com.outsmart.actor

import akka.actor._
import akka.actor.Terminated
import akka.routing.Broadcast

/**
 * @author Vadim Bobrov
*/
trait LastMohican
trait DoctorGoebbels extends Actor with ActorLogging {
	import context._

	def onBlackMark() {
		children foreach watch
		// send everyone a poison pill and wait for them to die, then kick the bucket
		children foreach (_ ! Broadcast(PoisonPill))
		children foreach (_ ! PoisonPill)
		// from now on receive only bad news
		become(lastMoments)
	}

	final def lastMoments : Receive = {

		case Terminated(ref) =>
			log.debug("another one bites the dust {}" + ref.hashCode())

			if (children.isEmpty)
				// all children done - safe to commit suicide
				if (isInstanceOf[LastMohican])
					context.system.shutdown()
				else
					stop(self)

	}
}
