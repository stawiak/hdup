package com.os.actor.read

import akka.actor.{Props, PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParser
import akka.routing.RoundRobinRouter
import com.os.actor.GracefulStop
import management.ManagementFactory
import javax.management.ObjectName

/**
  * @author Vadim Bobrov
  */
trait MQLHandlerActorMBean
class MQLHandlerActor(val parserFactory: () => MQLParser) extends Actor with ActorLogging with MQLHandlerActorMBean {

	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("com.os.chaos:type=MQLHandler,name=mqlHandler"))
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
