package com.os

import dao.Writer
import measurement.EnergyMeasurement
import org.joda.time.DateTime
import util.Loggable
import com.os.util.Util._
import akka.actor.ActorRef

/**
 * @author Vadim Bobrov
 */
object DataFiller extends Loggable{

	val dataGenerator = new DataGenerator
	//TODO
	var writer: Writer = _ //Writer()
	/**
	 * fills the database with random crap
	 * @param records number of records
	 */
	def fillRandom(records : Int) {
		using(writer) {

			for (i <- 0 until records) {
				writer.write(dataGenerator.getRandomMeasurement)
				if (i % 1000 == 0) debug(i)
			}

		}

	}

	/**
	 * fill the database for specific customer, location and wire
	 * @param records number of records
	 */
	def fill(customer:String, location:String, wireid:String, records : Int) {
		using(writer) {

			for (i <- 0 until records) {
				writer.write(new EnergyMeasurement(customer, location, wireid, i.asInstanceOf[Long], 8))
				if (i % 1000 == 0) debug(i)
			}

		}
	}

	/**
	 * fill the database with data spread evenly among all customers, locations and wires
	 * from start time to end time every 5 minutes
	 * @param start start time
	 * @param end end time
	 * @param value value to fill
	 */
	def fillEven(start:DateTime, end:DateTime, value:Double) {
		using(writer) {

			var counter = 0

			for(l <- start.getMillis until end.getMillis by 300000) {
				if ((l % 3600000) == 0)
					debug("filling for " + new DateTime(l))

				for (i <- 0 until 20; j <- 0 until 2; k <- 0 until 300) {
					writer.write(new EnergyMeasurement(dataGenerator.getCustomer(i), dataGenerator.getLocation(j), dataGenerator.getWireId(k), l, value))
					counter += 1
				}

			}

			debug("generated " + counter + " msmts")

		}
	}

	/**
	 * fill the database with data spread evenly among all customers, locations and wires
	 * from start time to end time every 5 minutes
	 * @param start start time
	 * @param end end time
	 * @param value value to fill
	 */
	def fillEvenParallel(start:DateTime, end:DateTime, value:Double, actor: ActorRef) {
		var counter = 0

		for(l <- start.getMillis until end.getMillis by 300000) {
			if ((l % 3600000) == 0)
				debug("filling for " + new DateTime(l))

			for (i <- 0 until 20; j <- 0 until 2; k <- 0 until 300) {
				actor ! new EnergyMeasurement(dataGenerator.getCustomer(i), dataGenerator.getLocation(j), dataGenerator.getWireId(k), l, value)
				counter += 1
			}
		}

		debug("generated " + counter + " msmts")
	}

	/**
	 * fill the database with single value
	 * @param value value to fill
	 */
	def fillSimple(customer: String, location: String, wireid: String, timestamp: Long, value:Double) {
		using(writer) {
			writer.write(new EnergyMeasurement(customer, location, wireid, timestamp, value))
		}
	}

}
