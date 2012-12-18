package com.outsmart

import actor.write._
import akka.actor.{Props, Actor}
import dao.{Writer, TestWriterImpl}
import measurement.Measurement

/**
 * @author Vadim Bobrov
*/
class TestDriverActor extends Actor {

  val master = context.actorOf(Props(new WriteMasterActor(() => new TestWriterImpl())), name = "master")

  protected def receive: Receive = {

    case msmt : Measurement => {
      master ! msmt
    }

    case WorkDone => {
      // Stops this actor and all its supervised children
      println("received WorkDone from master")
      println("writer called " + TestWriterImpl.counter + " times")
      context.stop(self)
      context.system.shutdown()
    }

    case Flush =>  {
      master ! Flush
    }


  }


}
