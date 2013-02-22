package com.os.unit

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.os.actor.write._
import com.typesafe.config.ConfigFactory
import com.os.measurement.{EnergyMeasurement, Measurement}
import concurrent.duration._
import com.os.util.ActorCache

/**
 * @author Vadim Bobrov
*/
class WriteMasterFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}


	val testRouterFactory = new ActorCache[(String, Int)] {
		def getAll: Traversable[ActorRef] = Nil
		def apply(tb: (String, Int))(implicit context: ActorContext) : ActorRef = context.actorOf(Props(new TestWriterActor()))
	}

	var writeMaster = system.actorOf(Props(new WriteMasterActor(Some(testRouterFactory))))


	"A write master" should "apply the chosen strategy for its child writers in case of intermittent failure" in {
		writeMaster ! new EnergyMeasurement("", "", "", 1, 1)
		writeMaster ! new EnergyMeasurement("boom", "", "", 2, 2)
		writeMaster ! new EnergyMeasurement("", "", "", 3, 3)

		expectMsg(3 seconds, 1)
		expectMsg(3 seconds, 2)
		expectMsg(3 seconds, 3)
	}

	private class TestException extends Exception
	private class TestWriterActor extends Actor with ActorLogging {

		var received = List.empty[Measurement]

		override def preRestart(reason: Throwable, message: Option[Any]) {
			log.debug("this should not be called!!!!!")
			testActor ! received.size
		}

		override def receive: Receive = {

			case msmt: Measurement =>
				received = msmt :: received

				testActor ! received.size
				if (msmt.customer == "boom")
					throw new TestException

		}

	}

}