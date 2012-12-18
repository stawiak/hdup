package com.outsmart

import actor.write._
import akka.actor.{Props, Actor}
import measurement.Measurement

/**
 * @author Vadim Bobrov
*/
class TestDriverActor extends Actor {

  val master = context.actorOf(Props(new MasterActor(10, null)), name = "master")

  protected def receive: Receive = {

    case msmt : Measurement => {
      master ! msmt
    }

    case WorkDone => {
      // Stops this actor and all its supervised children
      println("received WorkDone from master")
      context.stop(self)
      context.system.shutdown()
    }

    case Flush =>  {
      master ! Flush
    }


  }


}
