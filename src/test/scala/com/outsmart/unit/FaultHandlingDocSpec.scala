package com.outsmart.unit

import akka.testkit.{TestProbe, TestActorRef, TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import com.outsmart.actor.write._
import com.typesafe.config.ConfigFactory
import com.outsmart.unit.FaultHandlingDocSpec.TestWriterActor
import com.outsmart.measurement.Measurement
import scala._
import akka.util.{Duration, Timeout}
import akka.util.duration._
import com.outsmart.{Settings, DataGenerator}
import scala.Predef._
import akka.routing.FromConfig
import com.outsmart.actor.LoggingActor
import akka.event.LoggingReceive
import com.outsmart.actor.service.TimeWindowActor

/**
 * @author Vadim Bobrov
*/
object FaultHandlingDocSpec{
	class TestWriterActor extends Actor with ActorLogging {

		var counter : Int = 0
		var received = List[Measurement]()

		protected def receive: Receive = {

			case msmt: Measurement => {
				counter += 1

				// fail every other measurement
				if (counter % 2 == 0) {
					//log.info("throwing exception instance hashcode # {}",	this.hashCode())
					throw new TestException
				} else {
					log.info("receiving " + msmt)
					received = msmt :: received
				}



			}

			case Flush => {
				//log.info("flush received")
				sender ! received
			}
		}

	}

	class TestException extends Exception
}

class FaultHandlingDocSpec(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))
	val dataGen = new DataGenerator

	override def afterAll() {
		system.shutdown()
	}

	var masterWriter = TestActorRef(new WriteMasterActor(), name = "writeMaster")
	masterWriter.underlyingActor.routerFactory = {(actorContext : ActorContext, tableName : String) =>
		actorContext.actorOf(Props(new LoggingActor(new TestWriterActor())), name = "workerRouter")
	}

	"A write master" must {

		"apply the chosen strategy for its child writers in case of intermittent failure" in {

			for (i <- 1 to 3)
				masterWriter ! new Measurement("", "", "", i, i, i, i)

			masterWriter ! Flush
			// it is not testKit it is masterWriter that receives this message
			//expectMsg(5 seconds, List(new Measurement("", "", "", 1, 1, 1, 1), new Measurement("", "", "", 2, 2, 2, 2), new Measurement("", "", "", 3, 3, 3, 3)))
		}
	}

}