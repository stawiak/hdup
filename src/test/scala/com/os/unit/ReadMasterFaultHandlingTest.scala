package com.os.unit

import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.typesafe.config.ConfigFactory
import scala._
import scala.Predef._
import com.os.actor.read.{MeasurementReadRequest, ReadMasterActor}
import org.joda.time.Interval
import concurrent.duration._
import com.os.util.{Ping, Pong}
import com.os.TestActors

/**
 * @author Vadim Bobrov
*/
class ReadMasterFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	var readMaster = TestActorRef(new TestReadMasterActor(), name = "readMaster")
	readMaster.underlyingActor.routerFactory = {(tableName : String) =>
		//TODO
		null
		//actorContext.actorOf(Props[SlowActor])
	}

	"A read master" should	"not block on its child worker" in {
		readMaster ! MeasurementReadRequest("", "", "", "", new Interval(0,1))
		readMaster ! Ping
		expectMsg(1 second, Pong)
	}

	class TestReadMasterActor extends ReadMasterActor {
		private final def pingPong: Receive = {	case Ping => sender ! Pong }
		override def receive = pingPong orElse super.receive
	}

}