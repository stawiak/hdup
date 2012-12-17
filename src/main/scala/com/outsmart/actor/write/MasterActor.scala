package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.Writer
import akka.routing.RoundRobinRouter


/**
 * @author Vadim Bobrov
 */
case object Stop
case object WorkDone
case class WriteWork(val measurements: Seq[Measurement])

class MasterActor(val numberOfWorkers : Int) extends Actor {

  var measurements = List[Measurement]()
  //TODO: remove WriterImpl
  val workerRouter = context.actorOf(Props(new WriterActor(Writer.create())).withRouter(RoundRobinRouter(numberOfWorkers)), name = "workerRouter")

  protected def receive: Receive = {

    case msmt : Measurement => {
      // ←
      if(measurements.length == Settings.BatchSize)
        workerRouter ! WriteWork(measurements)
      else {
        println("sending batch to write")
        measurements = msmt :: measurements
      }
    }

    case WorkDone => {}

    case Stop =>
      // Stops this actor and all its supervised children
      context.stop(self)

  }

}
