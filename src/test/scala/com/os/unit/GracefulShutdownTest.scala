package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.{Measurement, EnergyMeasurement}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.os.actor._
import com.os.actor.util.DeadLetterListener
import com.os.{DataGenerator, TestActors, Settings}
import com.os.actor.read.{MQLHandlerActor, ReadMasterActor}
import akka.pattern._
import akka.util.Timeout
import concurrent.duration._
import concurrent.Await
import com.os.mql.parser.MQLParser
import com.os.util.Ping
import service.TimeWindowActor
import write.WriteMasterActor

/**
 * @author Vadim Bobrov
 */
class GracefulShutdownTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val settings = Settings.init(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props(new NoGoodnik),
		Props(new TimeWindowActor(settings.ExpiredTimeWindow)),
		Props(new ReadMasterActor),
		Props(new ForwarderActor),
		Props(new NoGoodnik),
		Props(new NoGoodnik),
		Props[DeadLetterListener],
		Props(new MonitorActor)
	)), name = "top")

	override def afterAll() {
		system.awaitTermination()
	}

	// run with both saveStateOnShutdown = on and off
	"graceful shutdown" should "allow all measurements to be processed" in {
		val dataGenerator = new DataGenerator()
		val timeWindow = system.actorFor("/user/top/timeWindow")
		dataGenerator.dailyDataIterator(20, false) foreach (timeWindow ! _)

		top ! GracefulStop
		//receiveN(144000, 5 seconds)
		receiveN(60000, 5 seconds)
	}

}
