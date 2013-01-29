package com.os.actor

import akka.actor.{ActorSystem, Props}
import read.ReadMasterActor
import service.IncomingHandlerActor
import spray.can.server.SprayCanHttpServerApp
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */

object Main extends App with SprayCanHttpServerApp {

	override lazy val system = ActorSystem("prod", ConfigFactory.load().getConfig("prod"))
	val incomingHandler = system.actorOf(Props[IncomingHandlerActor], name = "incomingHandler")
	val readMaster = system.actorOf(Props[ReadMasterActor], name = "readMaster")

	// the handler actor replies to incoming HttpRequests
	val handler = system.actorOf(Props[WebServiceActor])

	// create a new HttpServer using our handler and tell it where to bind to
	newHttpServer(handler) ! Bind(interface = "localhost", port = 8080)

}