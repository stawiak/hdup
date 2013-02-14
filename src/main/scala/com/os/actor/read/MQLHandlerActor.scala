package com.os.actor.read

import akka.actor.{Props, PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParser
import com.os.actor.util.GracefulStop
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe
import concurrent.duration._
import akka.routing.RoundRobinRouter

/**
  * @author Vadim Bobrov
  */
class MQLHandlerActor(val parserFactory: () => MQLParser) extends Actor with ActorLogging with ReadMasterAware {

	import context._
	implicit val timeout: Timeout = 60 seconds
	val router = actorOf(Props(new MQLWorkerActor(parserFactory())).withRouter(new RoundRobinRouter(3)))

	override def receive: Receive = {

		case mql: String =>
			(router ? mql).mapTo[Iterable[Map[String, Any]]] pipeTo sender

		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}


 }
