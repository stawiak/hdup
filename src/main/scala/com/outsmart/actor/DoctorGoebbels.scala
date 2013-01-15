package com.outsmart.actor

import akka.routing.Broadcast
import akka.actor.PoisonPill

/**
 * A marker trait to indicate that this actor should send
 * a PoisonPill to its children before its last moments
 * @author Vadim Bobrov
*/
trait DoctorGoebbels extends FinalCountDown {

	import context._

	override def onBlackSpot(depressionMode: Boolean = true) {
		super.onBlackSpot(depressionMode)
		// send everyone a poison pill and wait for them to die, then kick the bucket
		children foreach (_ ! Broadcast(PoisonPill))
		children foreach (_ ! PoisonPill)
	}

}
