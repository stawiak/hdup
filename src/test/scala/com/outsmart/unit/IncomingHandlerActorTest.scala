package com.outsmart.unit

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.outsmart.measurement.Measurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.outsmart.actor.service.IncomingHandlerActor
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
class IncomingHandlerActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	val writeProbe  = TestProbe()
	val timeWindowProbe = TestProbe()
	val testIncomingHandler = TestActorRef(new IncomingHandlerActor(writeProbe.ref, timeWindowProbe.ref))

	"incoming handler" must {
		"send all measurements to write master" in {
			testIncomingHandler !  new Measurement("", "", "", 0, 0, 0, 0)
			testIncomingHandler !  new Measurement("", "", "", 0, 0, 0, 0)

			writeProbe.receiveN(2)
			timeWindowProbe.expectNoMsg()
		}

		"also send all measurements less than 9.5 min old to time window" in {
			testIncomingHandler !  new Measurement("", "", "", 0, 0, 0, 0)
			testIncomingHandler !  new Measurement("", "", "", 0, 0, 0, 0)
			testIncomingHandler !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)

			writeProbe.receiveN(3)
			timeWindowProbe.receiveN(1)
		}

	}

}
