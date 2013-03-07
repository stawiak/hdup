package com.os

import actor.GracefulStop
import akka.actor.{PoisonPill, ActorLogging, Actor}
import akka.testkit.TestKit

/**
 * @author Vadim Bobrov
 */
trait TestActors {
	this:TestKit =>

	class ForwarderActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case GracefulStop =>
				log.debug("received graceful stop")
				self ! PoisonPill

			case x =>
				log.debug("forwarding {}", x)
				testActor ! x
		}
	}

	class NoGoodnik extends Actor with ActorLogging {
		override def receive: Receive = {

			case GracefulStop =>
				log.debug("received graceful stop")
				self ! PoisonPill

			case _ =>
		}
	}

	class Crasher extends Actor with ActorLogging {
		override def receive: Receive = {
			case GracefulStop =>
				log.debug("received graceful stop")
				self ! PoisonPill

			case _ => throw new Exception
		}
	}

	class SlowActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case GracefulStop =>
				log.debug("received graceful stop")
				self ! PoisonPill

			case _ =>
				log.debug("starting my long work")
				Thread.sleep(60000)
				log.debug("long work done")
		}
	}


}
