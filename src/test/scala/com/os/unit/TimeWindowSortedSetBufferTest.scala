package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.measurement.{EnergyMeasurement, Measurement}
import com.os.util.{TimeWindowSortedSetBuffer}

/**
 * @author Vadim Bobrov
 */
class TimeWindowSortedSetBufferTest extends FlatSpec with ShouldMatchers {

	val eml = List[Measurement](
		new EnergyMeasurement("", "", "", 4, 0),
		new EnergyMeasurement("", "", "", 2, 0),
		new EnergyMeasurement("", "", "", 5, 0),
		new EnergyMeasurement("", "", "", 1, 0),
		new EnergyMeasurement("", "", "", 3, 0)
	)

	"Time window" should "be iterated in ascending time order" in {
		val tw = new TimeWindowSortedSetBuffer[Measurement]()

		eml foreach (tw += _)

		var testOutput = List.empty[Measurement]
		tw foreach (testOutput::= _)

		testOutput(0) should be (eml(2))
		testOutput(1) should be (eml(0))
		testOutput(2) should be (eml(4))
		testOutput(3) should be (eml(1))
		testOutput(4) should be (eml(3))
	}

	it should "split correctly" in {
		val tw = new TimeWindowSortedSetBuffer[Measurement]()

		eml foreach (tw += _)

		val(oldmsmt, newmsmt) = tw span (_.timestamp < 3)

		oldmsmt.size should be (2)
		newmsmt.size should be (3)

		var testOutput = List.empty[Measurement]
		oldmsmt foreach (testOutput::= _)

		testOutput(0) should be (eml(1))
		testOutput(1) should be (eml(3))

		testOutput = List.empty[Measurement]
		newmsmt foreach (testOutput::= _)

		testOutput(0) should be (eml(2))
		testOutput(1) should be (eml(0))
		testOutput(2) should be (eml(4))
	}

}
