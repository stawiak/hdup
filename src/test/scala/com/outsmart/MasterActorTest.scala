package com.outsmart

import actor.write.{WorkDone, Stop, MasterActor}
import measurement.Measurement
import org.scalatest.FunSuite
import java.util.concurrent.TimeUnit
import akka.actor.{Actor, Props, ActorSystem}

/**
 * @author Vadim Bobrov
*/
class MasterActorTest extends FunSuite {

  test("actor fill") {
    val start = System.currentTimeMillis()

    // Create an Akka system
    val system = ActorSystem("test")
    val listener = system.actorOf(Props(new Listener()), name = "listener")

    val master = system.actorOf(Props(new MasterActor(10, listener)), name = "master")

    for(i <- 0 until 20)
      master ! new Measurement("","","", 1,1)


    //master ! Stop
    //system.shutdown()

    println("filled in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start) + " min")
  }

  // to attach to master actor
  class Listener extends Actor {
    def receive = {
      case WorkDone =>
        //println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format(pi, duration))
        context.system.shutdown()
    }


  }
}
