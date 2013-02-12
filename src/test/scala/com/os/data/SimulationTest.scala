package com.os.data

import org.scalatest.{FlatSpec, OneInstancePerTest, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.os.{Settings, DataGenerator}
import com.os.util.Timing
import com.os.actor.util.{DeadLetterListener, GracefulStop, Counter, Stats}
import com.typesafe.config.ConfigFactory
import com.os.actor.{WebServiceActor, MessageListenerActor, TopActor}
import com.os.actor.read.{ReadMasterActor, MQLHandlerActor}
import com.os.actor.service.TimeWindowActor
import com.os.actor.write.WriteMasterActor

/**
 * @author Vadim Bobrov
 */
class SimulationTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with OneInstancePerTest with Timing {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))
	val settings = Settings(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props[MQLHandlerActor],
		Props(new TimeWindowActor(settings.ExpiredTimeWindow)),
		Props[ReadMasterActor],
		Props[WriteMasterActor],
		Props(new MessageListenerActor(settings.ActiveMQHost, settings.ActiveMQPort, settings.ActiveMQQueue)),
		Props[WebServiceActor],
		Props[DeadLetterListener]
	)), name = "top")

	lazy val timeWindow = system.actorFor("/user/top/timeWindow")

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
		time { DataGenerator.dailyDataIterator(60, false) foreach  (timeWindow ! _) }

		//val scanner = Scanner(Settings.TableName, Settings(ConfigFactory.load().getConfig("chaos")))
		//println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + scanner.scan("customer1", "location1", "wireid1", new Interval(0, 1360693438444L)).size)

		top ! GracefulStop
	}

}
