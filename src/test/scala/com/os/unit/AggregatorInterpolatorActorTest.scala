package com.os.unit

import org.scalatest.{OneInstancePerTest, BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.os.measurement.Measurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.AggregatorActor
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
class AggregatorInterpolatorActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest{

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val underTest = TestActorRef(new AggregatorActor("", "", timeWindow = 10000))
	underTest.underlyingActor.writeMaster = writeProbe.ref

	"write muster" must {
		"receive one value from interpolator and one from aggregator when sent 4 expired messages to aggregator" in {
			underTest !  new Measurement("", "", "", 119995,5, 0, 0)
			underTest !  new Measurement("", "", "", 119997,3, 0, 0)
			underTest !  new Measurement("", "", "", 120001,5, 0, 0)
			underTest !  new Measurement("", "", "", 120002,6, 0, 0)

			writeProbe.expectMsgAllOf(new Measurement("", "", "", 120000,4, 0, 0), new Measurement("", "", "", 120000,4, 0, 0))
		}

		"receive 2 values from interpolator and 2 from aggregator when sent 4 expired messages to aggregator" in {
			underTest !  new Measurement("", "", "", 179995,5, 0, 0)
			underTest !  new Measurement("", "", "", 179997,3, 0, 0)
			underTest !  new Measurement("", "", "", 240001,60005, 0, 0)
			underTest !  new Measurement("", "", "", 240002,60006, 0, 0)

			writeProbe.expectMsgAllOf(
				new Measurement("", "", "", 180000,4, 0, 0),
				new Measurement("", "", "", 240000,60004, 0, 0),
				new Measurement("", "", "", 180000,4, 0, 0),
				new Measurement("", "", "", 240000,60004, 0, 0)
			)
		}

		"receive 2 values from interpolators and one from aggregator when sent 4 expired messages to 2 wires in aggregator" in {
			underTest !  new Measurement("", "", "1", 119995,5, 0, 0)
			underTest !  new Measurement("", "", "1", 119997,3, 0, 0)
			underTest !  new Measurement("", "", "1", 120001,5, 0, 0)
			underTest !  new Measurement("", "", "1", 120002,6, 0, 0)

			underTest !  new Measurement("", "", "2", 119995,5, 0, 0)
			underTest !  new Measurement("", "", "2", 119997,3, 0, 0)
			underTest !  new Measurement("", "", "2", 120001,5, 0, 0)
			underTest !  new Measurement("", "", "2", 120002,6, 0, 0)

			writeProbe.expectMsgAllOf(new Measurement("", "", "1", 120000,4, 0, 0), new Measurement("", "", "2", 120000,4, 0, 0), new Measurement("", "", "", 120000,8, 0, 0))
		}

	}

}
