package com.os.unit

import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.typesafe.config.ConfigFactory
import scala._
import scala.Predef._
import com.os.actor.read.{ReadRequest, ReadWorkerActor, MeasurementReadRequest, ReadMasterActor}
import org.joda.time.Interval
import concurrent.duration._
import com.os.util.{ActorCache, CachingActorFactory, Ping, Pong}
import com.os.TestActors
import akka.routing.RoundRobinRouter
import com.os.actor.read.MeasurementReadRequest
import scala.Some
import com.os.actor.write.WriteMasterActor

/**
 * @author Vadim Bobrov
*/
class ReadMasterFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	val testRouterFactory = new ActorCache[ReadRequest] {
		def values: Traversable[ActorRef] = Nil
		def keys: Traversable[ReadRequest] = Nil
		def apply(r: ReadRequest)(implicit context: ActorContext) : ActorRef = context.actorOf(Props(new SlowActor()))
	}

	var readMaster = system.actorOf(Props(new TestReadMasterActor(Some(testRouterFactory))))

	"A read master" should	"not block on its child worker" in {
		readMaster ! MeasurementReadRequest("", "", "", "", new Interval(0,1))
		readMaster ! Ping
		expectMsg(1 second, Pong)
	}

	class TestReadMasterActor(mockFactory: Option[ActorCache[ReadRequest]]) extends ReadMasterActor(mockFactory) {
		private final def pingPong: Receive = {	case Ping => sender ! Pong }
		override def receive = pingPong orElse super.receive
	}

}