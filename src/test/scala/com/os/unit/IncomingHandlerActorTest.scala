package com.os.unit

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.os.measurement.Measurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.TimeWindowActor
import com.os.Settings

/**
 * @author Vadim Bobrov
 */
class IncomingHandlerActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", Settings.config))

	val writeProbe  = TestProbe()

	val testTimeWindow = TestActorRef(new TimeWindowActor())
	testTimeWindow.underlyingActor.writeMaster = writeProbe.ref


	"time window" must {
		"send all measurements to write master" in {
			testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)

			writeProbe.receiveN(2)
		}

		"also send all measurements less than 9.5 min old to time window" in {
			testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", 0, 0, 0, 0)
			testTimeWindow !  new Measurement("", "", "", System.currentTimeMillis, 0, 0, 0)

			writeProbe.receiveN(3)
		}

	}

}
