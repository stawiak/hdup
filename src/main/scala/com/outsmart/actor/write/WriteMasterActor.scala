package com.outsmart.actor.write

import akka.actor._
import com.outsmart.measurement.Measurement
import com.outsmart.Settings
import com.outsmart.dao.Writer
import akka.routing.{FromConfig}
import akka.actor.SupervisorStrategy.{Stop, Resume, Restart, Escalate}
import akka.util.duration._
import akka.util.{Timeout, Duration}
import akka.dispatch.Await.Awaitable
import akka.dispatch.{Future, Await}
import akka.pattern.ask


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
	val router = actorOf(workerRouterProps, name = "workerRouter")
	var numberOfBatches = 0
	var numberOfDone = 0
	var receivedAll = false
	var counter = 0

	//TODO: list needs to be cleaned as jobs are done
	var results = List[Future[Any]]()
	implicit val timeout = Timeout(20 seconds)



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
				results = (router ? WriteWork(measurements)) :: results
				numberOfBatches += 1
				measurements = List[Measurement]()
			}

		}

		case WorkDone => {
			results = results filterNot (_.isCompleted)

			numberOfDone += 1
			log.debug("number of done " + numberOfDone + " out of " + numberOfBatches)
			if (receivedAll && numberOfDone == numberOfBatches)
				parent ! WorkDone
		}

		case Flush =>  {
			log.debug("flush received at " + counter)
			log.debug("remaining msmts " + measurements.length)
			results = (router ? WriteWork(measurements)) :: results
			numberOfBatches += 1
			measurements = List[Measurement]()
			receivedAll = true
		}


		case StopWriter => {
			log.debug("write master received stop")

			// allow all children to finish processing
			results foreach { Await.ready(_, timeout.duration) }
			sender ! StopWriter

			// stops this actor and all its supervised children
			stop(self)
		}

	}

}
