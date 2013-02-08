package com.os.actor.read

import akka.actor.{PoisonPill, ActorLogging, Actor}
import com.os.dao.Scanner
import com.os.Settings
import com.os.mql.parser.MQLParsers
import com.os.util.Timing
import com.os.actor.util.GracefulStop

/**
  * @author Vadim Bobrov
  */
class MQLHandlerActor extends Actor with ActorLogging {

	//TODO: singleton or multiple?
	val parser = new MQLParsers()


	override def receive: Receive = {

		case mql: String =>
			log.debug("mql received {}", mql)
			val res = parser.parseAll(parser.mql, mql)

		case GracefulStop =>
			log.debug("mqlHandler received graceful stop")
			self ! PoisonPill

	}

 }
