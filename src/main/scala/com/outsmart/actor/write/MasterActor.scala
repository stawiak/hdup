package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.Writer
import akka.routing.RoundRobinRouter


/**
 * @author Vadim Bobrov
 */
case object Flush
case object Stop
case class WorkDone(val count: Int)
case class WriteWork(val measurements: Seq[Measurement])

class MasterActor(val numberOfWorkers : Int, val listener: ActorRef) extends Actor {

  var count: Int = 0
  var measurements = List[Measurement]()
  //val workerRouter = context.actorOf(Props(new WriterActor(Writer.create())).withRouter(RoundRobinRouter(numberOfWorkers)), name = "workerRouter")
  val workerRouter = context.actorOf(Props(new WriterActor(Writer.create())))

  protected def receive: Receive = {

    case msmt : Measurement => {

      if(measurements.length == Settings.BatchSize) {
        println("sending batch to write")
        workerRouter ! WriteWork(measurements)
      } else
        measurements = msmt :: measurements
    }

    case workDone: WorkDone => {
      count += workDone.count
    }

    case Flush =>  {
      println("flushing the rest " + measurements.length)
      workerRouter ! WriteWork(measurements)
    }


    case Stop => {
      println("received stop")
      // Stops this actor and all its supervised children
      context.stop(self)
    }

  }

}
