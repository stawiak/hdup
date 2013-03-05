package com.os.actor.write

import akka.actor.{ActorLogging, Actor}
import com.os.dao.{TimeWindowState, WriterFactory}
import com.os.util.Util._
import com.os.measurement.Measurement
import com.os.actor.GracefulStop
import management.ManagementFactory
import javax.management.ObjectName

/**
  * @author Vadim Bobrov
  */
trait WriteWorkerActorMBean
class WriteWorkerActor(val writerFactory: WriterFactory) extends Actor with ActorLogging with WriteWorkerActorMBean {

	ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("com.os.chaos:type=Writer,Writer=workers,name=\"" + writerFactory.name + self.path.name + "\""))

	val writer = writerFactory.createWriter
	var measurements = List.empty[Measurement]
	var counter = 0


	override def receive: Receive = {

		case msmt: Measurement => {
			measurements = msmt :: measurements
			counter += 1

			if(counter == writerFactory.batchSize) {
				submitJob()
				counter = 0
			}
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
		log.debug("submitting write job to " + writerFactory.name)
		using(writer) {
			// this can fail anytime and should be retried
			measurements foreach writer.write
		}

		measurements = List.empty[Measurement]
	}

}

