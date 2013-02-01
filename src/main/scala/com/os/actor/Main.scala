package com.os.actor

import akka.actor.{ActorSystem, Props}
import spray.can.server.SprayCanHttpServerApp
import com.os.Settings

/**
 * @author Vadim Bobrov
 */

object Main extends App with SprayCanHttpServerApp {

	override lazy val system = ActorSystem("prod", Settings.config)
	val master = system.actorOf(Props[TopActor], name = "top")
	val webService = system.actorFor("/user/top/webService")

	// create a new HttpServer using our handler and tell it where to bind to
	newHttpServer(webService) ! Bind(interface = Settings.HttpHost, port = Settings.HttpPort)

}