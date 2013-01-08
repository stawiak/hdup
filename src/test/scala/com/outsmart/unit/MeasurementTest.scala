package com.outsmart.unit

import org.scalatest.FlatSpec
import com.outsmart.measurement.MeasuredValue._
import scala.Array
import com.outsmart.measurement.{Measurement, TimedValue, MeasuredValue}
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
 */
class MeasurementTest extends FlatSpec with ShouldMatchers {

	"Lower minute" should  "be lower minute boundary" in {
		minuteBoundary(Array(
			new MeasuredValue(120325, 1, 1, 1),
			new MeasuredValue(130325, 1, 1, 1),
			new MeasuredValue(120326, 1, 1, 1),
			new MeasuredValue(120325, 1, 1, 1),
			new MeasuredValue(215159991, 1, 1, 1)
		)) should be (120000, 215160000)
	}

	"Different timed values" should "be equal if timestamp is same" in {
		val a = new TimedValue(111, 111)
		val b = new TimedValue(111, 222)
		a should be (b)
	}

	"Measurements" should "be equal when all fields are equal" in {
		val a = new Measurement("", "", "", 120000,4, 0, 0)
		val b = new Measurement("", "", "", 120000,4, 0, 0)
		a should be (b)
	}



}
