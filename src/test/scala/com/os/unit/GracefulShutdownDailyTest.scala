package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.os.actor._
import com.os.{DataGenerator, TestActors, Settings}
import concurrent.duration._
import service.TimeWindowActor
import com.os.util.Loggable

/**
 * @author Vadim Bobrov
 */
class GracefulShutdownDailyTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with Loggable {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val settings = Settings.init(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props(new NoGoodnik),
		Props(new TimeWindowActor(settings.ExpiredTimeWindow)),
		Props(new NoGoodnik),
		Props(new ForwarderActor),
		Props(new NoGoodnik),
		Props(new NoGoodnik),
		Props(new NoGoodnik),
		Props(new NoGoodnik)
	)), name = "top")

	override def afterAll() {
		system.awaitTermination()
	}

	// run with both saveStateOnShutdown = on and off
	"graceful shutdown" should "allow all measurements to be processed" in {
		val dataGenerator = new DataGenerator(20,2,30)
		val timeWindow = system.actorFor("/user/top/timeWindow")

		dataGenerator.dailyDataIterator(20, false) foreach (timeWindow ! _)


		// original messages are not sent - they are handled around time window
		// 20 * 2 * 30 = 1200 wires
		// 5 * 1200 interpolated
		// 5 * 40 rollup (why 205?)
		// 1 Disable
		// 1 TimeWindowState
		top ! GracefulStop
		receiveN(6207, 30 seconds)

	}

}
