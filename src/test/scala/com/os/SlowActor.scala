package com.os

import akka.actor.{ActorLogging, Actor}

/**
 * @author Vadim Bobrov
 */
class SlowActor extends Actor with ActorLogging {
	override def receive: Receive = {
		case _ =>
			log.debug("starting my long work")
			Thread.sleep(60000)
			log.debug("long work done")
	}
}
