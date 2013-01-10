package com.outsmart.actor

import akka.actor._
import collection.mutable.ArrayBuffer
import akka.actor.Terminated
import akka.routing.Broadcast

/**
 * @author Vadim Bobrov
*/
trait DoctorGoebbels extends Actor with ActorLogging {

	import context._
	val watchedRouters = ArrayBuffer.empty[ActorRef]

	def addRouter(ref : ActorRef) = watchedRouters += watch(ref)

	def onBlackMark() {
		// send everyone a poison pill and wait for them to die, then kick the bucket
		watchedRouters foreach (_ ! Broadcast(PoisonPill))
		become(lastMoments)
	}

	final def lastMoments : Receive = {

		case Terminated(ref) =>
			log.info("another one bites the dust {}" + ref.hashCode())

			watchedRouters -= ref
			if (watchedRouters.isEmpty)
				// all children done - safe to commit suicide
				stop(self)

	}
}
