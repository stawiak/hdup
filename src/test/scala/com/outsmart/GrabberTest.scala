package com.outsmart

import org.scalatest.FunSuite

/**
 * @author Vadim Bobrov
*/
class GrabberTest extends FunSuite {


  test("returns the results of a single scanner") {
    val grabber : Grabber = new Grabber(new TestScannerServiceImpl())

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05")
    ))

    assert(res.length === 3)

  }


  test("returns the results of two scanners") {
    val grabber : Grabber = new Grabber(new TestScannerServiceImpl())

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05"),
      ("2012-01-01", "2012-01-05")
    ))

    assert(res.length === 6)

  }


}
