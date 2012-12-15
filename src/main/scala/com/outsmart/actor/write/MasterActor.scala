package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.WriterImpl

/**
 * @author Vadim Bobrov
 */
case class WorkDone()
case class WriteWork(val measurements: Seq[Measurement])

class MasterActor extends Actor {

  var measurements = List[Measurement]()
  var worker = context.actorOf(Props[WriterActor]) // new WriterImpl()

  protected def receive: Receive = {

    case msmt : Measurement => {
      if(measurements.length == Settings.BatchSize)
        worker ! WriteWork(measurements) //TODO: remove WriterImpl
      else
        measurements = msmt :: measurements
    }

    case workDone: WorkDone => {}


  }

}
