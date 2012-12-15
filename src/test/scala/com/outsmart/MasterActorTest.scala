package com.outsmart

import actor.write.{Stop, MasterActor}
import org.scalatest.FunSuite
import java.util.concurrent.TimeUnit
import akka.actor.{Props, ActorSystem}

/**
 * @author Vadim Bobrov
*/
class MasterActorTest extends FunSuite {

  test("actor fill") {
    val start = System.currentTimeMillis()

    // Create an Akka system
    val system = ActorSystem("DataFillSystem")

    val master = system.actorOf(Props(new MasterActor(10)), name = "master")
    //master ! Calculate

    master ! Stop
    system.shutdown()

    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start) + " min")
  }


}
