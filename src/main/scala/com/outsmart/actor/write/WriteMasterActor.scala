package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.Writer
import akka.routing.{FromConfig, RoundRobinRouter}


/**
 * @author Vadim Bobrov
 */
case object Flush
case object Stop
case object WorkDone
case class WriteWork(val measurements: Seq[Measurement])

class WriteMasterActor(writerCreator : () => Writer) extends Actor {

  import context._
  // Since a restart does not clear out the mailbox, it often is best to terminate the children upon failure and re-create them explicitly from the supervisor

  var measurements = List[Measurement]()
  val workerRouter = actorOf(Props(new WriterActor(writerCreator())).withRouter(FromConfig()), name = "workerRouter")
  var numberOfBatches = 0
  var numberOfDone = 0
  var receivedAll = false
  var counter = 0
  //val workerRouter = context.actorOf(Props(new WriterActor(Writer.create())))

  protected def receive: Receive = {

    case msmt : Measurement => {

      counter += 1
      measurements = msmt :: measurements

      if(measurements.length == Settings.BatchSize) {
        workerRouter ! WriteWork(measurements)
        numberOfBatches += 1
        measurements = List[Measurement]()
      }

    }

    case WorkDone => {
      numberOfDone += 1
      println("number of done " + numberOfDone + " out of " + numberOfBatches)
      if (receivedAll && numberOfDone == numberOfBatches)
        parent ! WorkDone
    }

    case Flush =>  {
      println("flush received at " + counter)
      println("remaining msmts " + measurements.length)
      workerRouter ! WriteWork(measurements)
      numberOfBatches += 1
      measurements = List[Measurement]()
      receivedAll = true
    }


    case Stop => {
      println("received stop")
      // Stops this actor and all its supervised children
      stop(self)
    }

  }

}