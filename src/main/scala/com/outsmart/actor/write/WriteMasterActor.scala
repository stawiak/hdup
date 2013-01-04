package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.Writer
import akka.routing.{FromConfig}
import akka.actor.SupervisorStrategy.{Stop, Resume, Restart, Escalate}
import akka.util.duration._
import akka.util.Duration


/**
 * @author Vadim Bobrov
 */
case object Flush
case object StopWriter
case object WorkDone
case class WriteWork(measurements: Seq[Measurement])

class WriteMasterActor(val workerRouterProps : Props = Props(new WriterActor(Writer.create())).withRouter(FromConfig()), val batchSize: Int = Settings.BatchSize) extends Actor with ActorLogging {

	import context._
	// Since a restart does not clear out the mailbox, it often is best to terminate
	// the children upon failure and re-create them explicitly from the supervisor

	var measurements = List[Measurement]()
	val workerRouter = actorOf(workerRouterProps, name = "workerRouter")
	var numberOfBatches = 0
	var numberOfDone = 0
	var receivedAll = false
	var counter = 0



	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = Duration.Inf) {
			case _: Exception     				⇒ Restart
			case _: Throwable                	⇒ Escalate
		}



	protected def receive: Receive = {

		case msmt : Measurement => {
			counter += 1
			measurements = msmt :: measurements

			if(measurements.length == batchSize) {
				workerRouter ! WriteWork(measurements)
				numberOfBatches += 1
				measurements = List[Measurement]()
			}

		}

		case WorkDone => {
			numberOfDone += 1
			log.debug("number of done " + numberOfDone + " out of " + numberOfBatches)
			if (receivedAll && numberOfDone == numberOfBatches)
				parent ! WorkDone
		}

		case Flush =>  {
			log.debug("flush received at " + counter)
			log.debug("remaining msmts " + measurements.length)
			workerRouter ! WriteWork(measurements)
			numberOfBatches += 1
			measurements = List[Measurement]()
			receivedAll = true
		}


		case StopWriter => {
			log.debug("write master received stop")

			// allow all children to finish processing
			while(numberOfDone != numberOfBatches)
				Thread.sleep(1000)

			sender ! StopWriter
			// stops this actor and all its supervised children
			stop(self)
		}

	}

}
