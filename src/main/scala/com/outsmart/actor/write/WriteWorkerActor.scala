package com.outsmart.actor.write

import akka.actor.{ActorLogging, Actor}
import com.outsmart.dao.Writer
import com.outsmart.util.Util
import Util.withOpenClose
import com.outsmart.Settings
import com.outsmart.measurement.Measurement

/**
  * @author Vadim Bobrov
  */
class WriteWorkerActor(val tableName : String, val batchSize: Int = Settings.BatchSize) extends Actor with ActorLogging{

	val writer = Writer(tableName)
	var measurements = List[Measurement]() //new Array[Measurement](batchSize)


	protected def receive: Receive = {

		case msmt : Measurement => {
			measurements = msmt :: measurements

			if(measurements.length == batchSize)
				submitJob
		}


		case Flush =>  {
			submitJob
		}

	}


	def submitJob() {
		log.info("sumbitting write job to " + tableName)
		withOpenClose(writer) {
			// this can fail anytime and should be retried
			measurements foreach writer.write
		}

		measurements = List[Measurement]()
	}

 }
