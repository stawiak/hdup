package com.os.actor.read

import akka.actor.{PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParsers
import com.os.actor.util.GracefulStop
import concurrent.{Future}
import com.os.measurement.TimedValue
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe
import concurrent.duration._
import com.os.mql.executor.MQLExecutor

/**
  * @author Vadim Bobrov
  */
class MQLHandlerActor extends Actor with ActorLogging with ReadMasterAware {

	import context._
	implicit val timeout: Timeout = 60 seconds
	//TODO: singleton or multiple?
	val parser = new MQLParsers()


	override def receive: Receive = {

		case mql: String =>
			//TODO: error handling
			log.debug("mql received {}", mql)
			val query = parser.parseAll(parser.mql, mql)
			val commands = new MQLExecutor(query.get).generateExecutePlan

			Future.traverse(commands)(command => (readMaster ? command.readRequest).mapTo[Iterable[TimedValue]]  map(command.include(_)) map (command.enrich(_)) ).map(_.flatten) pipeTo sender

		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}


 }
