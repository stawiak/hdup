package com.os.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import spray.can.server.SprayCanHttpServerApp
import com.os.Settings
import akka.util.Timeout
import concurrent.duration._
import scala.util.{Failure, Success}

/**
 * @author Vadim Bobrov
 */

object Main extends App with SprayCanHttpServerApp {

	override lazy val system = ActorSystem("prod", Settings.config)
	val top = system.actorOf(Props[TopActor], name = "top")
	implicit val timeout: Timeout = 60 seconds
	implicit val dispatcher = system.dispatcher

	val webService = (top ? GetWebService).mapTo[ActorRef]

	webService onComplete {
		case Success(result) => newHttpServer(result) ! Bind(interface = Settings.HttpHost, port = Settings.HttpPort)
		case Failure(failure) => system.shutdown()
	}


}