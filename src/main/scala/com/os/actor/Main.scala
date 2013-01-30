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

	val config = ConfigFactory.load().getConfig("prod")
	val ACTIVEMQ_HOST = config.getString("activemq.host")
	val HTTP_PORT = config.getInt("port")
	val HTTP_HOST = config.getString("host")

	override lazy val system = ActorSystem("prod", config)


	val incomingHandler = system.actorOf(Props[IncomingHandlerActor], name = "incomingHandler")
	val readMaster = system.actorOf(Props[ReadMasterActor], name = "readMaster")
	val messageListener = system.actorOf(Props(new MessageListenerActor(ACTIVEMQ_HOST, "msmt")), name = "jmsListener")


	// the handler actor replies to incoming HttpRequests
	val handler = system.actorOf(Props[WebServiceActor], name = "webService")

	// create a new HttpServer using our handler and tell it where to bind to
	newHttpServer(handler) ! Bind(interface = HTTP_HOST, port = HTTP_PORT)

}