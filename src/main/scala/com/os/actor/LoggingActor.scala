package com.os.actor

import akka.event.LoggingReceive
import akka.actor.{Actor, Props}

/**
 * Wrapper class to add logging of messages
 * use: new LoggingActor(new MyActor))
 * @author Vadim Bobrov
*/
class LoggingActor(fac: => Actor) extends Actor {

	val underlying = context.system.actorOf(Props(fac))

	def receive = {
		LoggingReceive {
			case x => underlying.tell(x, sender)
		}
	}
}