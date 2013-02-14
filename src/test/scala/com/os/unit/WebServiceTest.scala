package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.{Measurement, EnergyMeasurement}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.os.actor._
import com.os.actor.util.{DeadLetterListener, GracefulStop}
import com.os.Settings
import com.os.actor.read.{MQLHandlerActor, ReadMasterActor}
import com.os.actor.write.WriteMasterActor
import akka.pattern._
import akka.util.Timeout
import concurrent.duration._
import concurrent.Await
import com.os.mql.parser.MQLParser
import com.os.actor.service.TimeWindowActor
import spray.testkit.ScalatestRouteTest

/**
 * @author Vadim Bobrov
 */
/*
class WebServiceTest(_system: ActorSystem) extends TestKit(_system) with ScalatestRouteTest with FlatSpec with ShouldMatchers with ImplicitSender with OneInstancePerTest {
	def actorRefFactory = system

	//implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val settings = Settings(system.settings.config)
	val testtop = system.actorOf(Props(new TopActor(
		Props(new MQLHandlerActor(MQLParser.apply)),
		Props(new TimeWindowActor(settings.ExpiredTimeWindow)),
		Props[ReadMasterActor],
		Props(new WriteMasterActor),
		Props(new MessageListenerActor(settings.ActiveMQHost, settings.ActiveMQPort, settings.ActiveMQQueue)),
		Props[WebServiceActor],
		Props[DeadLetterListener]
	)), name = "top")


/*
	override def afterAll() {
		testtop ! GracefulStop
		system.awaitTermination()
	}
*/

	"time window" should "retain state after crash" in {
		//Get() ~> route ~> check {
		//	entityAs[String] should contain("Say hello")
		//}
	}

}
*/
