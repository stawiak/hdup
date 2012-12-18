package com.outsmart

import actor.write.{WorkDone, Stop, WriteMasterActor}
import measurement.Measurement
import org.scalatest.FunSuite
import java.util.concurrent.TimeUnit
import akka.actor.{Actor, Props, ActorSystem}

/**
 * @author Vadim Bobrov
*/
class MasterActorTest extends FunSuite {

  // to attach to master actor
  class Listener extends Actor {
    def receive = {
      case WorkDone =>
        //println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format(pi, duration))
        context.system.shutdown()
    }


  }
}
