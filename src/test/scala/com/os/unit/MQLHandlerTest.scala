package com.os.unit

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import com.typesafe.config.ConfigFactory
import scala._
import scala.Predef._
import com.os.actor.read.MQLHandlerActor
import concurrent.duration._
import com.os.mql.parser.MQLParser
import com.os.mql.executor.MQLCommand

/**
 * @author Vadim Bobrov
*/
class MQLHandlerTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	override def afterAll() {
		system.shutdown()
	}

	var mqlHandler = system.actorOf(Props(new TestMQLHandlerActor()))

	"MQL handler" should "not block on parsing" in {
		mqlHandler ! "select nothing from nowhere where nowhere is everywhere"
		mqlHandler ! Ping
		expectMsg(1 second, Pong)
	}

	private case class Ping()
	private case class Pong()

	class TestMQLHandlerActor extends MQLHandlerActor(TestMQLParser.apply) {
		private final def pingPong: Receive = {	case Ping => sender ! Pong }
		override def receive = pingPong orElse super.receive
	}

	object TestMQLParser{
		def apply():MQLParser = new TestMQLParsersImpl

		private class TestMQLParsersImpl extends MQLParser {
			def parse(mql: String): Traversable[MQLCommand] = {
				Thread.sleep(60000)
				List.empty[MQLCommand]
			}
		}
	}

}