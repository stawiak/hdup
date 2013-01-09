package com.outsmart.unit

import org.scalatest.FunSuite
import com.outsmart.dao.Writer
import org.scalamock.{MockFactoryBase, ProxyMockFactory}
import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import com.outsmart.DataGenerator
import com.outsmart.actor.write.WriteMasterActor

/**
 * @author Vadim Bobrov
 */
class WriterActorTest extends FunSuite with MockFactoryBase with ProxyMockFactory {
	//TODO: why just ProxyMockFactory doesn't work

	val mockWriter = mock[Writer]

	test("") {
		val config = ConfigFactory.load()
		val system = ActorSystem("test", config.getConfig("test"))

		/*
			val slaveProps = Props(
				new WriterActor(
					() => mockWriter
				).withRouter(FromConfig())
			)
		*/
		val writeMaster = system.actorOf(Props(new WriteMasterActor()), name = "master")
		writeMaster ! DataGenerator.getRandomMeasurement

		mockWriter expects 'write withArgs (*) anyNumberOfTimes
	}

}
