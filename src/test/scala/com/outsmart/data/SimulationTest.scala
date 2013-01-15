package com.outsmart.data

import org.scalatest.{FlatSpec, OneInstancePerTest, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.outsmart.actor.service.IncomingHandlerActor
import com.typesafe.config.ConfigFactory
import com.outsmart.DataGenerator
import com.outsmart.actor.{GracefulStop, LastMohican, FinalCountDown}
import com.outsmart.util.{Timing, Loggable}

/**
 * @author Vadim Bobrov
 */
class SimulationTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest with Timing{

	def this() = this(ActorSystem("prod", ConfigFactory.load().getConfig("prod")))
	val incomingHandler = system.actorOf(Props[IncomingHandlerActor with FinalCountDown with LastMohican], name = "incomingHandler")

	override def afterAll() {
		system.awaitTermination()
	}

	"incoming handler" should "be able to handle continuous message flow" in {
		time {
			for (i <- 1 to 10000) {
				incomingHandler ! DataGenerator.getRandomMeasurementSingleId
				Thread.sleep(5)
			}
		}

		incomingHandler ! GracefulStop
	}


	"incoming handler" should "be able to correctly process daily data" in {
		time { DataGenerator.dailyDataIterator foreach  (incomingHandler ! _) }
		incomingHandler ! GracefulStop
	}

}
