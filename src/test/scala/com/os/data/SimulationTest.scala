package com.os.data

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import com.os.{TestActors, Settings, DataGenerator}
import com.os.util.Timing
import com.os.actor.util.{DeadLetterListener, Counter, Stats}
import com.typesafe.config.ConfigFactory
import com.os.actor.{GracefulStop, WebServiceActor, MessageListenerActor, TopActor}
import com.os.actor.read.{ReadMasterActor, MQLHandlerActor}
import com.os.actor.service.TimeWindowActor
import com.os.actor.write.WriteMasterActor
import com.os.mql.parser.MQLParser

/**
 * @author Vadim Bobrov
 */
class SimulationTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll with Timing {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))
	val settings = Settings.init(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props(new MQLHandlerActor(MQLParser.apply)),
		Props(new TimeWindowActor(settings.ExpiredTimeWindow)),
		Props(new ReadMasterActor),
		Props(new WriteMasterActor),
		Props(new MessageListenerActor(settings.ActiveMQHost, settings.ActiveMQPort, settings.ActiveMQQueue)),
		Props[WebServiceActor],
		Props[DeadLetterListener],
	    Props(new NoGoodnik)
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
		val dataGenerator = DataGenerator()
		Stats.sentWriteMaster = new Counter()
		Stats.receivedWriteWorker = new Counter()
		time { dataGenerator.dailyDataIterator(60, false) foreach  (timeWindow ! _) }

		//val scanner = Scanner(Settings.TableName, Settings(ConfigFactory.load().getConfig("chaos")))
		//println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + scanner.scan("customer1", "location1", "wireid1", new Interval(0, 1360693438444L)).size)

		top ! GracefulStop
	}

}
