package com.os.unit

import org.scalatest.{OneInstancePerTest, BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import akka.actor._
import com.os.measurement.EnergyMeasurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.AggregatorActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
 * @author Vadim Bobrov
 */
class AggregatorInterpolatorActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest{

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	val writeProbe  = TestProbe()
	val underTest = TestActorRef(new AggregatorActor("", "", timeWindow = 10 seconds))
	underTest.underlyingActor.writeMaster = writeProbe.ref

	"write muster" must {
		"receive one value from interpolator and one from aggregator when sent 4 expired messages to aggregator" in {
			underTest !  new EnergyMeasurement("", "", "", 119995,5)
			underTest !  new EnergyMeasurement("", "", "", 119997,3)
			underTest !  new EnergyMeasurement("", "", "", 120001,5)
			underTest !  new EnergyMeasurement("", "", "", 120002,6)

			writeProbe.expectMsgAllOf(new EnergyMeasurement("", "", "", 120000,4), new EnergyMeasurement("", "", "", 120000,4))
		}

		"receive 2 values from interpolator and 2 from aggregator when sent 4 expired messages to aggregator" in {
			underTest !  new EnergyMeasurement("", "", "", 179995,5)
			underTest !  new EnergyMeasurement("", "", "", 179997,3)
			underTest !  new EnergyMeasurement("", "", "", 240001,60005)
			underTest !  new EnergyMeasurement("", "", "", 240002,60006)

			writeProbe.expectMsgAllOf(
				new EnergyMeasurement("", "", "", 180000,4),
				new EnergyMeasurement("", "", "", 240000,60004),
				new EnergyMeasurement("", "", "", 180000,4),
				new EnergyMeasurement("", "", "", 240000,60004)
			)
		}

		"receive 2 values from interpolators and one from aggregator when sent 4 expired messages to 2 wires in aggregator" in {
			underTest !  new EnergyMeasurement("", "", "1", 119995,5)
			underTest !  new EnergyMeasurement("", "", "1", 119997,3)
			underTest !  new EnergyMeasurement("", "", "1", 120001,5)
			underTest !  new EnergyMeasurement("", "", "1", 120002,6)

			underTest !  new EnergyMeasurement("", "", "2", 119995,5)
			underTest !  new EnergyMeasurement("", "", "2", 119997,3)
			underTest !  new EnergyMeasurement("", "", "2", 120001,5)
			underTest !  new EnergyMeasurement("", "", "2", 120002,6)

			writeProbe.expectMsgAllOf(new EnergyMeasurement("", "", "1", 120000,4), new EnergyMeasurement("", "", "2", 120000,4), new EnergyMeasurement("", "", "", 120000,8))
		}

	}

}
