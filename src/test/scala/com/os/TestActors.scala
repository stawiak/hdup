package com.os

import actor.GracefulStop
import akka.actor.{PoisonPill, ActorLogging, Actor}
import akka.testkit.TestKit
import util.{Pong, Ping, CounterMap}

/**
 * @author Vadim Bobrov
 */
trait TestActors {
	this:TestKit =>

	class ForwarderActor extends Actor with ActorLogging {

		var counterMap = new CounterMap[String]()

		override def postStop() {
			println("forwarder log")
			println(counterMap.toString)
		}

		override def receive: Receive = {
			case x =>
				//log.debug("forwarding {}", x)
				counterMap incr x.getClass.getName
				testActor ! x
		}
	}

	class NoGoodnik extends Actor with ActorLogging {
		override def receive: Receive = {
			case _ =>
		}
	}

	class PingPonger extends Actor with ActorLogging {
		override def receive: Receive = {
			case Ping =>
				sender ! Pong
			case _ =>
		}
	}

	class Crasher extends Actor with ActorLogging {
		override def receive: Receive = {
			case _ => throw new Exception
		}
	}

	class SlowActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case _ =>
				log.debug("starting my long work")
				Thread.sleep(60000)
				log.debug("long work done")
		}
	}

	class SlowDieActor extends Actor with ActorLogging {
		override def receive: Receive = {
			case GracefulStop =>
				log.debug("received graceful stop")
				Thread.sleep(5000)
				self ! PoisonPill
		}
	}


}
