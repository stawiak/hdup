package com.os.unit

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import com.os.{TestActors, Settings}
import com.os.actor.TopActor
import com.os.actor.util.DeadLetterListener

/**
 * @author Vadim Bobrov
 */
class ActorSystemTest(_system: ActorSystem) extends TestKit(_system) with TestActors with FlatSpec with ShouldMatchers with ImplicitSender with BeforeAndAfterAll {

	def this() = this(ActorSystem("chaos", ConfigFactory.load().getConfig("chaos")))

	val settings = Settings.init(system.settings.config)
	val top = system.actorOf(Props(new TopActor(
		Props(new BasicTestActor),
		Props(new BasicTestActor),
		Props(new BasicTestActor),
		Props(new BasicTestActor),
		Props(new BasicTestActor),
		Props(new BasicTestActor),
		Props[DeadLetterListener],
		Props(new BasicTestActor)
	)), name = "top")

	"top actors" should "load ok" in {
		//writeMaster ! Ping
	}

	it should "after all" in {
		system.shutdown()
	}

	private class BasicTestActor() extends Actor with ActorLogging {
		override def receive: Receive = {
			case x => log.debug("received " + x)
		}
	}


}
