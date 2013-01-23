package com.os.data

import org.scalatest.{FlatSpec, OneInstancePerTest, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.os.actor.service.IncomingHandlerActor
import com.typesafe.config.ConfigFactory
import com.os.DataGenerator
import com.os.actor.{GracefulStop, LastMohican, FinalCountDown}
import com.os.util.{Timing, Loggable}
import akka.agent.Agent
import com.os.actor.util.{Counter, Stats}

/**
 * @author Vadim Bobrov
 */
class SimulationTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest with Timing{

	def this() = this(ActorSystem("prod", ConfigFactory.load().getConfig("prod")))
	val incomingHandler = system.actorOf(Props[IncomingHandlerActor with LastMohican], name = "incomingHandler")

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


	"incoming handler" should "be able to correctly process daily data" in {
		Stats.sentWriteMaster = new Counter()
		Stats.receivedWriteWorker = new Counter()
		time { DataGenerator.dailyDataIterator(20) foreach  (incomingHandler ! _) }
		incomingHandler ! GracefulStop
	}

}
