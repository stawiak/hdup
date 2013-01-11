package com.outsmart.actor.write

import akka.actor.{ActorLogging, Actor}

/**
 * Actor aware of write master
 * @author Vadim Bobrov
*/
trait WriterMasterAwareActor extends Actor with ActorLogging {

	// made settable for testability
	var writeMaster = context.system.actorFor("/user/writeMaster")

}
