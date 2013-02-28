package com.os.actor.write

import akka.actor.{ActorLogging, Actor}
import com.os.dao.{WriterFactory, AggregatorState}
import com.os.util.Util._
import com.os.measurement.Measurement
import com.os.actor.util.GracefulStop
import concurrent.Future

/**
  * @author Vadim Bobrov
  */
class WriteWorkerActor(val writerFactory: WriterFactory) extends Actor with ActorLogging{

	val writer = writerFactory.createWriter
	var measurements = List.empty[Measurement]


	override def receive: Receive = {

		case msmt: Measurement => {
			measurements = msmt :: measurements

			if(measurements.length == writerFactory.batchSize)
				submitJob()
		}

		case state: Future[AggregatorState] =>
			//state onSuccess(writer.write(_))
			//TODO save state

		case GracefulStop =>  {
			log.debug("write worker received graceful stop")
			submitJob()
		}

	}


	def submitJob() {
		//log.debug("submitting write job to " + tableName)
		using(writer) {
			// this can fail anytime and should be retried
			measurements foreach writer.write
		}

		measurements = List.empty[Measurement]
	}

 }
