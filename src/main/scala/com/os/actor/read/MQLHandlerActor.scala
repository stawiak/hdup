package com.os.actor.read

import akka.actor.{Props, PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParser
import com.os.actor.util.GracefulStop
import akka.routing.RoundRobinRouter

/**
  * @author Vadim Bobrov
  */
class MQLHandlerActor(val parserFactory: () => MQLParser) extends Actor with ActorLogging {

	import context._
	val router = actorOf(Props(new MQLWorkerActor(parserFactory())).withRouter(new RoundRobinRouter(3)))

	override def receive: Receive = {

		case mql: String =>
			router forward mql

		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}


 }
