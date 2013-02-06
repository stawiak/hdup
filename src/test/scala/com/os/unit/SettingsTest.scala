package com.os.unit

import org.scalatest.{OneInstancePerTest, FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
class SettingsTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with BeforeAndAfterAll with OneInstancePerTest {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	"tests" should "load test configuration" in {
    import system._

		settings.config.getInt("testMarker") should be (239)
		settings.config.getBoolean("interpolation") should be (true)

	}

}
