package com.outsmart.unit

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import com.outsmart.actor.write._
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig
import com.outsmart.unit.FaultHandlingDocSpec.TestWriterActor
import com.outsmart.measurement.Measurement
import scala._
import com.outsmart.actor.write.WriteWork
import akka.dispatch.Await
import akka.util.{Timeout, Duration}
import akka.util.duration._
import akka.pattern.ask
import com.outsmart.DataGenerator

/**
 * @author Vadim Bobrov
*/
object FaultHandlingDocSpec{
	class TestWriterActor extends Actor with ActorLogging {

		var counter : Int = 0

		override def preStart() {
			log.debug("Starting WorkerActor instance hashcode # {}", this.hashCode())
		}

		override def postStop() {
			log.debug("Stopping WorkerActor instance hashcode # {}",	this.hashCode())
		}

		protected def receive: Receive = {
			case work: WriteWork => {
				counter += 1

				// fail every fifth write job
				if (counter % 5 == 0) {
					log.info("throwing exception instance hashcode # {}",	this.hashCode())
					throw new TestException
				} else {
					log.info("writing to database " + counter)
					Thread.sleep(5000)
				}
			}
		}

	}

	class TestException extends Exception
}

class FaultHandlingDocSpec(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("prod", ConfigFactory.load().getConfig("prod")))
	val dataGen = new DataGenerator

	override def afterAll() {
		//system.awaitTermination()
	}

	"A supervisor" must {

		"apply the chosen strategy for its child" in {
			val masterWriter = system.actorOf(Props(new WriteMasterActor(Props(new TestWriterActor()), 3)), name = "master")

			for (i <- 1 to 32)
				masterWriter ! dataGen.getRandomMeasurement

			masterWriter ! Flush


			implicit val timeout = Timeout(60 seconds)
			Await.ready(masterWriter ? StopWriter, timeout.duration)
			//expectMsg(StopWriter)
		}
	}
}