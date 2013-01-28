package com.os.actor

import akka.actor.Props
import spray.can.server.SprayCanHttpServerApp

/**
 * @author Vadim Bobrov
 */

object Main extends App with SprayCanHttpServerApp {

	// the handler actor replies to incoming HttpRequests
	val handler = system.actorOf(Props[WebServiceActor])

	// create a new HttpServer using our handler and tell it where to bind to
	newHttpServer(handler) ! Bind(interface = "localhost", port = 8080)

}