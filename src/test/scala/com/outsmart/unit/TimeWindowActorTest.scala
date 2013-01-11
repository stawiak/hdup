package com.outsmart.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.outsmart.measurement.Measurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.outsmart.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import akka.util.duration._

/**
 * @author Vadim Bobrov
 */
class TimeWindowActorTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	val testTimeWindow = TestActorRef(new TimeWindowActor(10000))
	testTimeWindow.underlyingActor.aggregatorFactory =  TestAggregationFactory.get

	"time window" should "send nothing before expiration window expire" in {
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
		testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)

		TestAggregationFactory.aggregator.expectNoMsg
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

	it should "send 4 expired messages to interpolator and get one value back" in {
		testTimeWindow !  new Measurement("", "", "", 119995,5, 0, 0)
		testTimeWindow !  new Measurement("", "", "", 119997,3, 0, 0)
		testTimeWindow !  new Measurement("", "", "", 120001,5, 0, 0)
		testTimeWindow !  new Measurement("", "", "", 120002,6, 0, 0)

		TestAggregationFactory.aggregator.receiveN(4)
	}




	object TestAggregationFactory {
		val aggregator = TestProbe()

		def get(customer : String, location : String) : ActorRef = {
			aggregator.ref
		}
	}

}
