package com.os.actor.read

import akka.actor.{PoisonPill, ActorLogging, Actor}
import com.os.mql.parser.MQLParsers
import com.os.actor.util.GracefulStop
import com.os.mql.model.MQLExecutor
import concurrent.{Future}
import com.os.measurement.TimedValue
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe
import concurrent.duration._

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
			//TODO: where is condition like value > _ handled?
			//TODO: where are string literals and expressions added?
			log.debug("mql received {}", mql)
			val query = parser.parseAll(parser.mql, mql)
			val executor = new MQLExecutor(query.get)

			Future.traverse(executor.generate)(req => (readMaster ? req).mapTo[Iterable[TimedValue]]).map(_.flatten) pipeTo sender



		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}


 }
