package com.os.data

import org.scalatest.{FlatSpec, OneInstancePerTest, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.os.DataGenerator
import com.os.util.Timing
import com.os.actor.util.{GracefulStop, Counter, Stats}
import com.typesafe.config.ConfigFactory

/**
 * @author Vadim Bobrov
 */
class SimulationTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest with Timing{

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))
	val master = system.actorFor("/user/top")
	val timeWindow = system.actorFor("/user/top/timeWindow")

	override def afterAll() {
		system.awaitTermination()
	}

/*
	"incoming handler" should "be able to handle continuous message flow" in {
		time {
			for (i <- 1 to 10000) {
				incomingHandler ! DataGenerator.getRandomMeasurementSingleId
				Thread.sleep(5)
			}
		}

		incomingHandler ! GracefulStop
	}
*/


	"system" should "be able to correctly process daily data" in {
		Stats.sentWriteMaster = new Counter()
		Stats.receivedWriteWorker = new Counter()
		time { DataGenerator.dailyDataIterator(60 * 5, false) foreach  (timeWindow ! _) }
		master ! GracefulStop
	}

}
