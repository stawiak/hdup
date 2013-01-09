package com.outsmart.unit

import org.scalatest.{OneInstancePerTest, BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.outsmart.measurement.Measurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.outsmart.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
class TimeWindowInterpolatorActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest{

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val testTimeWindow = TestActorRef(new TimeWindowActor(expiredTimeWindow = 10000))
	testTimeWindow.underlyingActor.writeMaster = writeProbe.ref

	"time window" must {
		"send 4 expired messages to interpolator and get one value back" in {
			testTimeWindow !  new Measurement("", "", "", 119995,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 119997,3, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 120001,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 120002,6, 0, 0)

			writeProbe.expectMsg(new Measurement("", "", "", 120000,4, 0, 0))
		}

		"send 4 expired messages to interpolator and get 2 values back" in {
			testTimeWindow !  new Measurement("", "", "", 179995,5, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 179997,3, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 240001,60005, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 240002,60006, 0, 0)

			writeProbe.expectMsgAllOf(new Measurement("", "", "", 180000,4, 0, 0), new Measurement("", "", "", 240000,60004, 0, 0))
		}
	}

}
