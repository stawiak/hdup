package com.os

import measurement.{EnergyMeasurement, Measurement}
import scala.util.Random
import util.Loggable

/**
 * @author Vadim Bobrov
 */
object DataGenerator {
	def apply(): DataGenerator = new DataGenerator
}
class DataGenerator(val customerNumber: Int = 20, val locationNumber: Int = 2, val wireNumber: Int = 300) extends Loggable{

	private val CUSTOMERS =  new Array[String](customerNumber)
	private val LOCATIONS =  Array[String]("location0", "location1")
	private val WIREIDS =  new Array[String](wireNumber)

	for (i <- 0 until customerNumber)
		CUSTOMERS(i) = "customer" + i

	for (i <- 0 until wireNumber)
		WIREIDS(i) = "wireid" + i

	private val random : Random = new Random()

	def getRandomCustomer = CUSTOMERS(random.nextInt(customerNumber))
	def getRandomLocation = LOCATIONS(random.nextInt(locationNumber))
	def getRandomWireId = WIREIDS(random.nextInt(wireNumber))

	def getRandomMeasurement = new EnergyMeasurement(
		getRandomCustomer,
		getRandomLocation,
		getRandomWireId,
		System.currentTimeMillis() - random.nextInt(570000) - 30000,
		random.nextDouble()
	)

	def getRandomMeasurementSingleId = new EnergyMeasurement(
		CUSTOMERS(0),
		LOCATIONS(0),
		WIREIDS(0),
		System.currentTimeMillis(),// - random.nextInt(Settings.ExpiredTimeWindow) + 30000,
		random.nextDouble()
	)

	def getCustomer(i : Int) = CUSTOMERS(i)
	def getLocation(i : Int) = LOCATIONS(i)
	def getWireId(i : Int) = WIREIDS(i)

	/**
	 * Generate test data
	 * @param minutes  for how long to generate
	 * @param realTime use real time or every 5 minutes (warning!! if false time will be in future)
	 */
	class DailyDataIterator(val minutes: Long = 20, val realTime: Boolean = false) extends Iterator[Measurement] {

		val start = System.currentTimeMillis()// - Settings.ExpiredTimeWindow + 60 * 1000 - (1000 * 60 * minutes)
		var curTime = start
		var curCustomer, curLocation, curWireId = 0
		var counter: Long = 0L

		def hasNext: Boolean = curTime < start + 1000L * 60L * minutes

		def next(): Measurement = {
			if (curCustomer == customerNumber - 1 && curLocation == locationNumber - 1 && curWireId == wireNumber - 1) {
				if (realTime)
					curTime = System.currentTimeMillis()
				else
					curTime += 1000 * 60 * 5

				curCustomer = 0; curLocation = 0; curWireId = 0
			} else if(curWireId != wireNumber - 1)
				curWireId += 1
			else if(curLocation != locationNumber - 1) {
				curLocation += 1
				curWireId = 0
			} else {
				curCustomer += 1
				curLocation = 0; curWireId = 0
			}

			val msmt = new EnergyMeasurement(
				CUSTOMERS(curCustomer),
				LOCATIONS(curLocation),
				WIREIDS(curWireId),
				curTime,
				random.nextDouble()
			)

			counter += 1
			//debug(counter)
			msmt
		}
	}

	def dailyDataIterator(minutes: Long, realTime: Boolean): Iterator[Measurement] = new DailyDataIterator(minutes, realTime)

}
