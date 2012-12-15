package com.outsmart

import actor.write.MasterActor
import dao.WriterImpl
import org.scalatest.FunSuite
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import akka.actor.{Props, ActorSystem}

/**
 * @author Vadim Bobrov
*/
class MasterActorTest extends FunSuite {

  test("even fill") {
    val start = System.currentTimeMillis()
    // Create an Akka system
    val system = ActorSystem("DataFillSystem")

    // create the result listener, which will print the result and
    // shutdown the system
    //val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    val master = system.actorOf(Props(new MasterActor(10)))

    // start the calculation
    //master ! Calculate
    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start) + " min")
  }


}
