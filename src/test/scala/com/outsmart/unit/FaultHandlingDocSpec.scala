package com.outsmart.unit

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import com.outsmart.actor.write._
import com.typesafe.config.ConfigFactory
import com.outsmart.unit.FaultHandlingDocSpec.TestWriterActor
import com.outsmart.measurement.Measurement
import scala._
import akka.util.Timeout
import akka.util.duration._
import com.outsmart.DataGenerator
import scala.Predef._

/**
 * @author Vadim Bobrov
*/
object FaultHandlingDocSpec{
	class TestWriterActor extends Actor with ActorLogging {

		var counter : Int = 0

		override def preRestart(reason: Throwable, message: Option[Any]) {
			// retry - forward is necessary to retain the master as sender
			// http://letitcrash.com/post/23532935686/watch-the-routees
			//log.info("retrying " + message.get.asInstanceOf[WriteWork].measurements.mkString)
			//message foreach {self forward _ }
			log.info("restarting WorkerActor instance hashcode # {}", this.hashCode())
		}

		protected def receive: Receive = {

			case msmt: Measurement => {
				counter += 1

				// fail every fifth measurement
				if (counter % 5 == 0) {
					log.info("throwing exception instance hashcode # {}",	this.hashCode())
					throw new TestException
				} else {
					//Thread.sleep(1000)
					log.info("processed " + msmt)
				}

			}

			case Flush => {}
		}

	}

	class TestException extends Exception
}

class FaultHandlingDocSpec(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("test", ConfigFactory.load().getConfig("test")))
	val dataGen = new DataGenerator

	override def afterAll() {
		system.awaitTermination()
	}

	"A supervisor" must {

		"apply the chosen strategy for its child" in {
			val masterWriter = system.actorOf(Props(new WriteMasterActor(String => Props[TestWriterActor])), name = "master")

			for (i <- 1 to 32)
				masterWriter ! new Measurement("" + i, "" + i, "" + i, i, i, i, i)

			masterWriter ! Flush


			implicit val timeout = Timeout(20 seconds)
			//Await.ready(masterWriter ? StopWriter, timeout.duration)
			//expectMsg(StopWriter)

		}
	}
}