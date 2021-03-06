package com.os.actor

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.pattern.ask
import read.{MQLHandlerActor, ReadMasterActor}
import service.TimeWindowActor
import spray.can.server.SprayCanHttpServerApp
import com.os.Settings
import akka.util.Timeout
import concurrent.duration._
import scala.util.{Failure, Success}
import com.typesafe.config.ConfigFactory
import util.DeadLetterListener
import write.WriteMasterActor
import com.os.mql.parser.MQLParser

/**
 * @author Vadim Bobrov
 */

object Main extends App with SprayCanHttpServerApp {

	override lazy val system = ActorSystem("chaos", ConfigFactory.load().getConfig("chaos"))
	val settings = Settings.init(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
			Props(new MQLHandlerActor(MQLParser.apply)),
			Props(new TimeWindowActor(settings.ExpiredTimeWindow)),
			Props(new ReadMasterActor),
			Props(new WriteMasterActor),
			Props(new MessageListenerActor(settings.ActiveMQHost, settings.ActiveMQPort, settings.ActiveMQQueue)),
			Props(new WebServiceActor),
			//Props(new HttpServiceActor),
			Props(new DeadLetterListener),
			Props(new MonitorActor)
		)), name = "top")

	implicit val timeout: Timeout = 60 seconds
	implicit val dispatcher = system.dispatcher

	val webService = (top ? GetWebService).mapTo[ActorRef]

	webService onComplete {
		case Success(result) => newHttpServer(result) ! Bind(interface = settings.HttpHost, port = settings.HttpPort)
		case Failure(failure) => system.shutdown()
	}

}