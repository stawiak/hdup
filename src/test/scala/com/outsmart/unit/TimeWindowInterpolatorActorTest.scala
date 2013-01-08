package com.outsmart.unit

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.outsmart.measurement.{Measurement}
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.outsmart.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import akka.util.Duration

/**
 * @author Vadim Bobrov
 */
class TimeWindowInterpolatorActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val testTimeWindow = TestActorRef(new TimeWindowActor(writeProbe.ref, null, expiredTimeWindow = 10000))

	"time window" must {
		"send 4 expired messages to interpolator and get one value back" in {
			testTimeWindow !  new Measurement("", "", "", 119995,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 119997,3, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 120001,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 120002,6, 0, 0)

			writeProbe.expectMsg(Duration.Inf, new Measurement("", "", "", 120000,4, 0, 0))
		}


	}

	object TestInterpolatorFactory {
		val interpolator = TestProbe()

		def get(customer : String, location : String, wireid : String) : ActorRef = {
			interpolator.ref
		}
	}


}
