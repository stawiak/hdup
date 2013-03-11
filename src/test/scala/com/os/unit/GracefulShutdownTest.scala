package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.os.actor._
import com.os.actor.util.DeadLetterListener
import com.os.{DataGenerator, TestActors, Settings}
import com.os.actor.read.ReadMasterActor
import akka.util.Timeout
import concurrent.duration._
import service.TimeWindowActor
import com.os.measurement.EnergyMeasurement

/**
 * @author Vadim Bobrov
 */
class GracefulShutdownTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

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
		Props(new NoGoodnik)
	)), name = "top")

	override def afterAll() {
		system.awaitTermination()
	}

	// run with both saveStateOnShutdown = on and off
	"graceful shutdown" should "allow all measurements to be processed" in {
		val timeWindow = system.actorFor("/user/top/timeWindow")

		timeWindow !  new EnergyMeasurement("", "", "", 119995,5)
		timeWindow !  new EnergyMeasurement("", "", "", 119997,3)
		timeWindow !  new EnergyMeasurement("", "", "", 120001,5)
		timeWindow !  new EnergyMeasurement("", "", "", 120002,6)


		top ! GracefulStop
		// 4 original messages are not sent - they are handled around time window
		// 1 interpolated
		// 1 rollup
		// 1 SaveState
		// 1 TimeWindowState
		receiveN(4, 5 seconds)
	}

}
