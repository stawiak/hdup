package com.os.unit

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.typesafe.config.ConfigFactory
import scala._
import com.os.actor.read.{ReadRequest, ReadMasterActor}
import org.joda.time.Interval
import concurrent.duration._
import com.os.util._
import com.os.{Settings, TestActors}
import com.os.actor.read.MeasurementReadRequest
import scala.Some
import com.os.dao.read.ReaderFactory

/**
 * @author Vadim Bobrov
*/
class ReadMasterFaultHandlingTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", Settings.init(ConfigFactory.load().getConfig("chaos")).config))

	override def afterAll() {
		system.shutdown()
	}

	val testRouterFactory = new MappableActorCache[ReadRequest, ReaderFactory] {
		def values: Traversable[ActorRef] = Nil
		def keys: Traversable[Int] = Nil
		def apply(r: ReadRequest)(implicit context: ActorContext) : ActorRef = context.actorOf(Props(new SlowActor()))
	}

	var readMaster = system.actorOf(Props(new TestReadMasterActor(Some(testRouterFactory))))

	"A read master" should	"not block on its child worker" in {
		readMaster ! MeasurementReadRequest("", "", "", "", new Interval(0,1))
		readMaster ! Ping
		expectMsg(1 second, Pong)
	}

	class TestReadMasterActor(mockFactory: Option[MappableActorCache[ReadRequest, ReaderFactory]]) extends ReadMasterActor(mockFactory) {
		private final def pingPong: Receive = {	case Ping => sender ! Pong }
		override def receive = pingPong orElse super.receive
	}

}