package com.os.actor.write

import akka.actor.{ActorLogging, Actor}
import com.os.dao.{WriterFactory, AggregatorState}
import com.os.util.Util._
import com.os.measurement.Measurement
import com.os.actor.GracefulStop

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

		// disabling and reenabling table will often cause exceptions
		// multiple retries are in order here
		case state: AggregatorState =>
			using(writer) {	writer.write(state) }

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
