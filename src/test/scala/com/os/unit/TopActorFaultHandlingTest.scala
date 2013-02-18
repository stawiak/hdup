package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.{Measurement, EnergyMeasurement}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.os.actor.{WebServiceActor, MessageListenerActor, TopActor}
import com.os.actor.util.{DeadLetterListener, GracefulStop}
import com.os.Settings
import com.os.actor.read.{MQLHandlerActor, ReadMasterActor}
import com.os.actor.write.WriteMasterActor
import akka.pattern._
import akka.util.Timeout
import concurrent.duration._
import concurrent.Await
import com.os.mql.parser.MQLParser

/**
 * @author Vadim Bobrov
 */
class TopActorFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	implicit val timeout: Timeout = 10 seconds
	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val settings = Settings(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props(new MQLHandlerActor(MQLParser.apply)),
		Props(new TestTimeWindowActor()),
		Props[ReadMasterActor],
		Props(new WriteMasterActor),
		Props(new MessageListenerActor(settings.ActiveMQHost, settings.ActiveMQPort, settings.ActiveMQQueue)),
		Props[WebServiceActor],
		Props[DeadLetterListener]
	)), name = "top")


	override def afterAll() {
		top ! GracefulStop
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
