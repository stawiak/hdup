package com.outsmart.actor.write

import akka.actor.Actor
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.WriterImpl

/**
 * @author Vadim Bobrov
 */
class MasterActor extends Actor {

  var measurements = List[Measurement]()

  protected def receive: Receive = {
    case msmt : Measurement => {
      if(measurements.length == Settings.BatchSize)
        new WriterActor(new WriterImpl()) ! new WriteWork(measurements) //TODO: remove WriterImpl
      else
        measurements = msmt :: measurements
    }

  }

}
