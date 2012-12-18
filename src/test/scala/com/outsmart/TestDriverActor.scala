package com.outsmart

import actor.write._
import akka.actor.{Props, Actor}
import dao.{Writer}
import measurement.Measurement

/**
 * @author Vadim Bobrov
*/
class TestDriverActor extends Actor {

  //val master = context.actorOf(Props(new WriteMasterActor(() => new TestWriterImpl())), name = "master")
  val master = context.actorOf(Props(new WriteMasterActor(Writer.create)), name = "master")

  protected def receive: Receive = {

    case WorkDone => {
      println("writer called " + TestWriterImpl.counter + " times")
      // Stops this actor and all its supervised children
      context.stop(self)
      context.system.shutdown()
    }

    case msg : Any =>  {
      master ! msg
    }

  }
}
