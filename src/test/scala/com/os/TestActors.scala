package com.os

import akka.actor.{ActorLogging, Actor}
import akka.testkit.TestKit

/**
 * @author Vadim Bobrov
 */
trait TestActors {
	this:TestKit =>

	class TestActorForwarder extends Actor {
		override def receive: Receive = { case x => testActor ! x}
	}

	class NoGoodnik extends Actor with ActorLogging {
		override def receive: Receive = { case _ => }
	}

	class SlowActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case _ =>
				log.debug("starting my long work")
				Thread.sleep(60000)
				log.debug("long work done")
		}
	}


}
