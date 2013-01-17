package com.outsmart.actor.read

import akka.actor.{ActorLogging, Actor}

/**
 * Actor aware of read master
 * @author Vadim Bobrov
*/
trait ReadMasterAwareActor extends Actor with ActorLogging {

	// made settable for testability
	var readMaster = context.system.actorFor("/user/readMaster")

}
