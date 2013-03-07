package com.os.actor.read

import akka.actor.{Props, PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParser
import akka.routing.RoundRobinRouter
import com.os.actor.GracefulStop
import javax.management.ObjectName
import com.os.util.JMXActorBean

/**
  * @author Vadim Bobrov
  */
trait MQLHandlerActorMBean
class MQLHandlerActor(val parserFactory: () => MQLParser) extends Actor with ActorLogging with MQLHandlerActorMBean with JMXActorBean {

	import context._
	override val jmxName = new ObjectName("com.os.chaos:type=MQLHandler,name=mqlHandler")
	val router = actorOf(Props(new MQLWorkerActor(parserFactory())).withRouter(new RoundRobinRouter(3)))

	override def receive: Receive = {

		case mql: String =>
			router forward mql

		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}


 }
