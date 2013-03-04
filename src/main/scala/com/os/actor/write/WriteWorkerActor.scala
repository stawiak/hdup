package com.os.actor.write

import akka.actor.{ActorLogging, Actor}
import com.os.dao.{TimeWindowState, WriterFactory}
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

		case state: TimeWindowState =>
			log.debug("write worker received TimeWindowState")
			using(writer) {	writer.write(state) }

		case GracefulStop =>  {
			log.debug("write worker received graceful stop")
			// writers that drop and recreate table should not be called here
			// as using(writer) will drop table
			if (!measurements.isEmpty)
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
