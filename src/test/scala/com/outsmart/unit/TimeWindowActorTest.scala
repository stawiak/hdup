package com.outsmart.unit

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.outsmart.measurement.{TimedValue, Measurement}
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.outsmart.actor.service.{InterpolatorActor, TimeWindowActor, IncomingHandlerActor}
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
class TimeWindowActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val testTimeWindow = TestActorRef(new TimeWindowActor(writeProbe.ref, TestInterpolatorFactory.get, 10000))

	"time window" must {
		"send nothing before expiration window expire" in {
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)

			writeProbe.expectNoMsg
			TestInterpolatorFactory.interpolator.expectNoMsg
		}


		"send old to interpolator after expiration window expire" in {
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			Thread.sleep(11000)
			writeProbe.expectNoMsg
			TestInterpolatorFactory.interpolator.receiveN(2)
		}

		"not send new to interpolator after expiration window expire" in {
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			Thread.sleep(11000)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)
			writeProbe.expectNoMsg
			TestInterpolatorFactory.interpolator.receiveN(3)
		}

		"send 4 expired messages to interpolator and get one value back" in {
			testTimeWindow !  new Measurement("", "", "", 119995,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 119997,3, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 120001,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 120002,6, 0, 0)

			writeProbe.expectNoMsg
			TestInterpolatorFactory.interpolator.receiveN(4)
		}


	}

	object TestInterpolatorFactory {
		val interpolator = TestProbe()

		def get(customer : String, location : String, wireid : String) : ActorRef = {
			interpolator.ref
		}
	}


}
