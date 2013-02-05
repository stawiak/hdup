package com.os.unit

import org.scalatest.FlatSpec
import com.os.measurement.TimedValue
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
 */
class TimedValueTest extends FlatSpec with ShouldMatchers {

	"Different timed values" should "be equal if timestamp is same" in {
		val a = new TimedValue(111, 111)
		val b = new TimedValue(111, 222)
		a should be (b)
	}

	"Different timed values" should "not be equal if timestamp is different" in {
		val a = new TimedValue(112, 111)
		val b = new TimedValue(111, 222)
		a should not be (b)
	}

	"TimedValue hash code" should "be equal if values are equal" in {
		val a = new TimedValue(111, 111)
		val b = new TimedValue(111, 222)
		a.hashCode() should be (b.hashCode())
	}

	"TimedValue compare" should "return negative if value is less" in {
		val a = new TimedValue(111, 111)
		val b = new TimedValue(112, 222)
		a.compareTo(b) should be < (0)
	}

	"TimedValue compare" should "return positive if value is more" in {
		val a = new TimedValue(112, 111)
		val b = new TimedValue(111, 222)
		a.compareTo(b) should be > (0)
	}

	"TimedValue compare" should "return 0 if values are equal" in {
		val a = new TimedValue(111, 111)
		val b = new TimedValue(111, 222)
		a.compareTo(b) should be (0)
	}

}
