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

/**
 * @author Vadim Bobrov
*/
object FaultHandlingDocSpec{
	class TestWriterActor extends Actor {

		protected def receive: Receive = {
			case work: WriteWork => {
				throw new Exception
			}
		}

	}

}
class FaultHandlingDocSpec(_system: ActorSystem) extends TestKit(_system) with WordSpec with MustMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("prod", ConfigFactory.load().getConfig("prod")))

	override def afterAll {
		system.shutdown()
	}

	"A supervisor" must {

		"apply the chosen strategy for its child" in {
			val masterWriter = system.actorOf(Props(new WriteMasterActor(Props(new TestWriterActor()).withRouter(FromConfig()))), name = "master")
			masterWriter ! new Measurement("","","",0,0,0,0)
			masterWriter ! Flush
			//expectMsg(StopWriter)
		}
	}
}