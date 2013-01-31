package com.os.actor.service

import akka.actor.Actor

/**
 * Actor aware of time window
 * @author Vadim Bobrov
*/
trait TimeWindowAware {

	this: Actor =>
	// made settable for testability
	var timeWindow = context.system.actorFor("/user/incomingHandler/timeWindow")

}
