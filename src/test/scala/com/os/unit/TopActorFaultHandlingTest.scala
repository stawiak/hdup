package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.{Measurement, EnergyMeasurement}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.os.actor._
import com.os.actor.util.DeadLetterListener
import com.os.{TestActors, Settings}
import com.os.actor.read.{MQLHandlerActor, ReadMasterActor}
import akka.pattern._
import akka.util.Timeout
import concurrent.duration._
import concurrent.Await
import com.os.mql.parser.MQLParser
import com.os.util.Ping

/**
 * @author Vadim Bobrov
 */
class TopActorFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val settings = Settings.init(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props(new MQLHandlerActor(MQLParser.apply)),
		Props(new TestTimeWindowActor()),
		Props(new ReadMasterActor),
		Props(new ForwarderActor),
		Props(new MessageListenerActor(settings.ActiveMQHost, settings.ActiveMQPort, settings.ActiveMQQueue)),
		Props[WebServiceActor],
		Props[DeadLetterListener],
		Props(new MonitorActor(Props(new Crasher)))
	)), name = "top")


	override def afterAll() {
		system.awaitTermination()
	}

	"time window" should "retain state after crash" in {
		val testTimeWindow = system.actorFor("/user/top/timeWindow")

		testTimeWindow !  new EnergyMeasurement("", "", "", 119995,5)
		testTimeWindow !  new EnergyMeasurement("", "", "", 119997,3)
		testTimeWindow !  new EnergyMeasurement("", "", "", 120001,5)
		testTimeWindow !  new EnergyMeasurement("", "", "", 120002,6)

		Await.result((testTimeWindow ? Report).mapTo[Int], timeout.duration) should be (4)
		testTimeWindow ! Crash
		Await.result((testTimeWindow ? Report).mapTo[Int], timeout.duration) should be (4)
		top ! GracefulStop
	}

	"monitor worker" should "crash without taking system down" in {
		val testMonitor = system.actorFor("/user/top/monitor")
		val testWriteMaster = system.actorFor("/user/top/writeMaster")
		testMonitor ! Crash
		testWriteMaster ! Ping

		receiveN(1, 2 seconds)
		top ! GracefulStop
	}

	private case object Crash
	private case object Report
	private class TestException extends Exception
	private class TestTimeWindowActor extends Actor with ActorLogging {

		var msmts = List.empty[Measurement]


		override def preStart() {
			log.info("starting test time window")
		}

		override def receive: Receive = {

			case msmt: EnergyMeasurement =>
				log.info("receiving " + msmt)
				msmts = msmt :: msmts

			case Report =>
				sender ! msmts.size

			case Crash =>
				throw new TestException

			case GracefulStop => {
				self ! PoisonPill
			}
		}

	}

}
