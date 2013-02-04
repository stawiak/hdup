package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.Measurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import com.os.util.TimeSource

/**
 * @author Vadim Bobrov
 */
class TimeWindowActorRealTimeTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("prod", ConfigFactory.load().getConfig("prod")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val testTimeWindow = TestActorRef(new TimeWindowActor(10 seconds))
	testTimeWindow.underlyingActor.aggregatorFactory =  TestAggregationFactory.get
	testTimeWindow.underlyingActor.writeMaster = writeProbe.ref

	"time window" should "send nothing before expiration window expire" in {
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)

		TestAggregationFactory.aggregator.expectNoMsg
	}

	it should "send expired measurements to write master" in {
		testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)

		writeProbe.receiveN(2)
	}

	it should "only store non-expired measurements" in {
		testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)

		writeProbe.receiveN(2)
		testTimeWindow.underlyingActor.measurements.length should be (1)
	}


	it should "send old to interpolator after expiration window expire" in {
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		TestAggregationFactory.aggregator.expectNoMsg(1 seconds)
		Thread.sleep(11000)
		TestAggregationFactory.aggregator.receiveN(2)
	}

	it should "not send new to interpolator after expiration window expire" in {
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		TestAggregationFactory.aggregator.expectNoMsg(10 milliseconds)
		Thread.sleep(11000)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		TestAggregationFactory.aggregator.receiveN(3)
	}

	object TestAggregationFactory {
		val aggregator = TestProbe()

		def get(customer : String, location : String) : ActorRef = {
			aggregator.ref
		}
	}

}
