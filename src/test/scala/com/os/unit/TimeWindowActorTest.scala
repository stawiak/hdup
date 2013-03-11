package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.EnergyMeasurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import com.os.util.{ActorCache, TimeSource}
import com.os.{Settings, TestActors}

/**
 * @author Vadim Bobrov
 */
class TimeWindowActorTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", Settings.init(ConfigFactory.load().getConfig("chaos")).config))

	override def afterAll() {
		system.shutdown()
	}

	val testAggregatorFactory: ActorCache[(String, String)] = new ActorCache[(String, String)] {
		def values: Traversable[ActorRef] = Nil
		def keys: Traversable[(String, String)] = Nil
		def apply(t: (String, String))(implicit context: ActorContext): ActorRef = context.actorOf(Props(new ForwarderActor))
	}

	val writeProbe  = TestProbe()
	val testTimeWindow = TestActorRef(new TimeWindowActor(10 seconds, TestTimeSource, Some(testAggregatorFactory)))

	"time window" should "send 4 expired messages to interpolator" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", 119995,5)
		testTimeWindow !  new EnergyMeasurement("", "", "", 119997,3)
		testTimeWindow !  new EnergyMeasurement("", "", "", 120001,5)
		testTimeWindow !  new EnergyMeasurement("", "", "", 120002,6)

		receiveN(4, 10 seconds)
	}

	object TestTimeSource extends TimeSource {
		private val times = List(120000, 120000, 120000, 120000, 180000, 240000)
		var i = 0

		override def now(): Long = {
			i += 1
			times(i - 1)
		}
	}

}
