package com.os.actor

import akka.actor.Actor

/**
 * Actor aware of top actor
 * @author Vadim Bobrov
*/
trait TopAware {

	this: Actor =>
	// made settable for testability
	var top = context.system.actorFor("/user/top")

}
