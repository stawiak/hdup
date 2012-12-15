package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import akka.routing.RoundRobinRouter
import com.outsmart.dao.WriterImpl

/**
 * @author Vadim Bobrov
 */
case class WorkDone()
case class WriteWork(val measurements: Seq[Measurement])

class MasterActor(val numberOfWorkers : Int) extends Actor {

  var measurements = List[Measurement]()
  val workerRouter = Props(new WriterActor(new WriterImpl())).withRouter(RoundRobinRouter(numberOfWorkers))

  protected def receive: Receive = {

    case msmt : Measurement => {
      if(measurements.length == Settings.BatchSize)
        workerRouter ! WriteWork(measurements) //TODO: remove WriterImpl
      else
        measurements = msmt :: measurements
    }

    case workDone: WorkDone => {}
      // Stops this actor and all its supervised children
      //context.stop(self)


  }

}
