package com.outsmart.unit

import org.scalatest.{FunSuite, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.outsmart.dao.Writer
import org.scalamock.{MockFactoryBase, GeneratedMockFactoryBase, ProxyMockFactory}
import org.scalamock.scalatest.MockFactory
import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import com.outsmart.{DataGenerator, DataFiller, TestDriverActor}
import org.joda.time.DateTime
import com.outsmart.actor.write.{WriteMasterActor, Flush}
import com.outsmart.measurement.Measurement

/**
 * @author Vadim Bobrov
*/
class WriterActorTest extends FunSuite with MockFactoryBase with ProxyMockFactory {
  //TODO: why just ProxyMockFactory doesn't work

  val mockWriter = mock[Writer]
  val dataGen = new DataGenerator

  test("") {
    val config = ConfigFactory.load()
    val system = ActorSystem("test", config.getConfig("test"))

    val writeMaster = system.actorOf(Props(new WriteMasterActor(() => mockWriter)), name = "master")
    writeMaster ! dataGen.getRandomMeasurement

    mockWriter expects 'write withArgs (*) anyNumberOfTimes
  }

}
