package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import com.os.measurement.EnergyMeasurement
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import com.os.actor.service.TimeWindowActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
 * @author Vadim Bobrov
 */
class SettingsTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	"tests" should "load test configuration" in {
		system.settings.config.getInt("testMarker") should be (239)
	}

}
