package com.outsmart.actor.write

import akka.actor.{ActorLogging, Actor}
import com.outsmart.dao.Writer
import com.outsmart.util.Util
import Util.using
import com.outsmart.Settings
import com.outsmart.measurement.Measurement
import com.outsmart.actor.{GracefulStop}
import com.outsmart.actor.util.Stats

/**
  * @author Vadim Bobrov
  */
class WriteWorkerActor(val tableName : String, val batchSize: Int = Settings.BatchSize) extends Actor with ActorLogging{

	val writer = Writer(tableName)
	var measurements = List[Measurement]() //new Array[Measurement](batchSize)


	override def receive: Receive = {

		case msmt : Measurement => {
			log.debug("write worker received msmt")
			Stats.receivedWriteWorker.++
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
