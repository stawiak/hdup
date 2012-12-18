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
case object WorkDone
case class WriteWork(val measurements: Seq[Measurement])

class MasterActor(val numberOfWorkers : Int, val listener: ActorRef) extends Actor {

  import context._
  // Since a restart does not clear out the mailbox, it often is best to terminate the children upon failure and re-create them explicitly from the supervisor

  var measurements = List[Measurement]()
  val workerRouter = actorOf(Props(new WriterActor(Writer.create())).withRouter(RoundRobinRouter(numberOfWorkers)), name = "workerRouter")
  var numberOfBatches = 0
  var numberOfDone = 0
  var receivedAll = false
  //val workerRouter = context.actorOf(Props(new WriterActor(Writer.create())))

  protected def receive: Receive = {

    case msmt : Measurement => {

      if(measurements.length == Settings.BatchSize) {
        workerRouter ! WriteWork(measurements)
        numberOfBatches += 1
        measurements = List[Measurement]()
      } else
        measurements = msmt :: measurements
    }

    case WorkDone => {
      numberOfDone += 1
      println("number of done " + numberOfDone + " out of " + numberOfBatches)
      if (receivedAll && numberOfDone == numberOfBatches)
        parent ! WorkDone
    }

    case Flush =>  {
      println("flush received")
      workerRouter ! WriteWork(measurements)
      numberOfBatches += 1
      receivedAll = true
    }


    case Stop => {
      println("received stop")
      // Stops this actor and all its supervised children
      stop(self)
    }

  }

}
