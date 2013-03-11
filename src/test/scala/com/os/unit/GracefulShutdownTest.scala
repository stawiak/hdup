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
		Props(new MonitorActor)
	)), name = "top")

	override def afterAll() {
		system.shutdown()
	}

	// run with both saveStateOnShutdown = on and off
	"graceful shutdown" should "allow all measurements to be processed" in {
		val dataGenerator = new DataGenerator()
		//val timeWindow = system.actorFor("/user/top/timeWindow")
		//dataGenerator.dailyDataIterator(20, false) foreach (timeWindow ! _)
		var counter = 0
		dataGenerator.dailyDataIterator(20, false) foreach (x => counter +=1)
		println("" + counter)

		//top ! GracefulStop
		//receiveN(144000, 5 seconds)
		//receiveN(60000, 5 seconds)
	}

}
