package com.os.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import spray.can.server.SprayCanHttpServerApp
import com.os.Settings
import akka.util.Timeout
import concurrent.duration._
import scala.util.{Failure, Success}
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */

object Main extends App with SprayCanHttpServerApp {

	override lazy val system = ActorSystem("chaos", ConfigFactory.load().getConfig("chaos"))
	val settings = Settings(system.settings.config)
	val top = system.actorOf(Props[TopActor], name = "top")
	implicit val timeout: Timeout = 60 seconds
	implicit val dispatcher = system.dispatcher

	val webService = (top ? GetWebService).mapTo[ActorRef]

	webService onComplete {
		case Success(result) => newHttpServer(result) ! Bind(interface = settings.HttpHost, port = settings.HttpPort)
		case Failure(failure) => system.shutdown()
	}


}