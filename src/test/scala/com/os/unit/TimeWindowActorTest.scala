package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.EnergyMeasurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import com.os.util.TimeSource

/**
 * @author Vadim Bobrov
 */
class TimeWindowActorTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("prod", ConfigFactory.load().getConfig("prod")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val testTimeWindow = TestActorRef(new TimeWindowActor(10 seconds, TestTimeSource))
	testTimeWindow.underlyingActor.aggregatorFactory =  TestAggregationFactory.get
	testTimeWindow.underlyingActor.writeMaster = writeProbe.ref

	"time window" should "send 4 expired messages to interpolator" in {
		testTimeWindow !  new EnergyMeasurement("", "", "", 119995,5)
		testTimeWindow !  new EnergyMeasurement("", "", "", 119997,3)
		testTimeWindow !  new EnergyMeasurement("", "", "", 120001,5)
		testTimeWindow !  new EnergyMeasurement("", "", "", 120002,6)

		TestAggregationFactory.aggregator.receiveN(4, 10 seconds)
	}


	object TestAggregationFactory {
		val aggregator = TestProbe()

		def get(customer : String, location : String) : ActorRef = {
			aggregator.ref
		}
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
