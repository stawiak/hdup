package com.os.actor.write

import akka.actor.{ActorLogging, Actor}
import com.os.dao.Writer
import com.os.util.Util._
import com.os.Settings
import com.os.measurement.Measurement
import com.os.actor.GracefulStop

/**
  * @author Vadim Bobrov
  */
class WriteWorkerActor(val tableName : String, val batchSize: Int = Settings.BatchSize) extends Actor with ActorLogging{

	val writer = Writer(tableName)
	var measurements = List[Measurement]() //new Array[Measurement](batchSize)


	override def receive: Receive = {

		case msmt : Measurement => {
			measurements = msmt :: measurements

			if(measurements.length == batchSize)
				submitJob()
		}


		case GracefulStop =>  {
			log.debug("write worker received graceful stop")
			submitJob()
		}

	}


	def submitJob() {
		log.debug("submitting write job to " + tableName)
		using(writer) {
			// this can fail anytime and should be retried
			measurements foreach writer.write
		}

		measurements = List[Measurement]()
	}

 }
