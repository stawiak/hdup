package com.outsmart.data

import org.scalatest.FunSuite
import com.outsmart.dao.{Grabber, Scanner}
import com.outsmart.util.Loggable

/**
 * @author Vadim Bobrov
 */
class DataReadTest extends FunSuite with Loggable{

	test("returns the results of a single scanner") {
		val grabber : Grabber = new Grabber(Scanner())

		val res = grabber.grab("customer0", "location0", "wireid0", List[(String, String)](
			("2012-02-01", "2012-02-05")
		))

		debug(res.length)

		assert(res(0).energy === 22)
	}


	test("returns the results of two scanners") {
		val grabber : Grabber = new Grabber(Scanner())

		val res = grabber.grab("customer1", "location1", "wireid1", List[(String, String)](
			("2012-02-01", "2012-02-05"),
			("2012-03-01", "2012-03-05")
		))

		debug(res.length)
		//assert(res(0).value === 555)
	}

	test("simple scanner test") {
		val scanner = Scanner()
		val res = scanner.scan("customer1", "location1", "wireid1", 1, 1)
		debug(res.length)
		assert(res(0).energy === 1)
	}

}
