package com.outsmart.unit

import org.scalatest.FlatSpec
import com.outsmart.TestScannerImpl
import org.scalatest.matchers.ShouldMatchers
import com.outsmart.dao.Grabber

/**
 * @author Vadim Bobrov
 */
class GrabberTest extends FlatSpec with ShouldMatchers {

	val grabber : Grabber = new Grabber(new TestScannerImpl())

	"A single scanner" should "return 3 values" in {
		val res = grabber.grab("customer1", "location1", "wireid1", List[(String, String)](
			("2012-01-01", "2012-01-05")
		))

		res should have length (3)
	}

	it should "be 1,2,3" in {
		val res = grabber.grab("customer1", "location1", "wireid1", List[(String, String)](
			("2012-01-01", "2012-01-05")
		))

		res(0).energy should be (1)
		res(1).energy should be (2)
		res(2).energy should be (3)
	}


	"2 scanners" should "return 6 values" in {

		val res = grabber.grab("customer1", "location1", "wireid1", List[(String, String)](
			("2012-01-01", "2012-01-05"),
			("2012-01-01", "2012-01-05")
		))

		res should have length (6)
	}


}
