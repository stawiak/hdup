package com.os.unit

import org.scalatest.FlatSpec
import com.os.measurement.{EnergyMeasurement, TimedValue}
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
 */
class MeasurementTest extends FlatSpec with ShouldMatchers {

	"Different timed values" should "be equal if timestamp is same" in {
		val a = new TimedValue(111, 111)
		val b = new TimedValue(111, 222)
		a should be (b)
	}

	"Measurements" should "be equal when all fields are equal" in {
		val a = new EnergyMeasurement("", "", "", 120000,4)
		val b = new EnergyMeasurement("", "", "", 120000,4)
		a should be (b)
	}



}
