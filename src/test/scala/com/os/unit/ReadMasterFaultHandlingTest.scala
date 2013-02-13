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
import com.os.SlowActor

/**
 * @author Vadim Bobrov
*/
class ReadMasterFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	var readMaster = TestActorRef(new TestReadMasterActor(), name = "readMaster")
	readMaster.underlyingActor.routerFactory = {(actorContext : ActorContext, tableName : String) =>
		actorContext.actorOf(Props[SlowActor])
	}

	"A read master" should	"not block on its child worker" in {
		readMaster ! MeasurementReadRequest("", "", "", "", List[Interval](new Interval(0,1)))
		readMaster ! Ping
		expectMsg(1 second, Pong)
	}

	private case class Ping()
	private case class Pong()

	class TestReadMasterActor extends ReadMasterActor {
		private final def pingPong: Receive = {	case Ping => sender ! Pong }
		override def receive = pingPong orElse super.receive
	}

}