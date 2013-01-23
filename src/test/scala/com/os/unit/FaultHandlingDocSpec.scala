package com.os.unit

import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import com.os.actor.write._
import com.typesafe.config.ConfigFactory
import com.os.unit.FaultHandlingDocSpec.TestWriterActor
import com.os.measurement.Measurement
import scala._
import scala.Predef._
import com.os.actor.{GracefulStop, LoggingActor}

/**
 * @author Vadim Bobrov
*/
object FaultHandlingDocSpec{
	class TestWriterActor extends Actor with ActorLogging {

		var counter : Int = 0
		var received = List[Measurement]()

		override def receive: Receive = {

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

			case GracefulStop => {
				//log.info("flush received")
				sender ! received
			}
		}

	}

	class TestException extends Exception
}

class FaultHandlingDocSpec(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))

	override def afterAll() {
		system.shutdown()
	}

	var masterWriter = TestActorRef(new WriteMasterActor(), name = "writeMaster")
	masterWriter.underlyingActor.routerFactory = {(actorContext : ActorContext, tableName : String, batchSize : Int) =>
		actorContext.actorOf(Props(new LoggingActor(new TestWriterActor())), name = "workerRouter")
	}

	"A write master" must {

		"apply the chosen strategy for its child writers in case of intermittent failure" in {

			for (i <- 1 to 3)
				masterWriter ! new Measurement("", "", "", i, i, i, i)

			masterWriter ! GracefulStop
			// it is not testKit it is masterWriter that receives this message
			//expectMsg(5 seconds, List(new Measurement("", "", "", 1, 1, 1, 1), new Measurement("", "", "", 2, 2, 2, 2), new Measurement("", "", "", 3, 3, 3, 3)))
		}
	}

}