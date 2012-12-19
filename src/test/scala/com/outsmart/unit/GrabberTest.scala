package com.outsmart.unit

import org.scalatest.FunSuite
import com.outsmart.{TestScannerServiceImpl, Grabber}

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

    assert(res(0).value === 1)
    assert(res(1).value === 2)
    assert(res(2).value === 3)
  }


  test("returns the results of two scanners") {
    val grabber : Grabber = new Grabber(new TestScannerServiceImpl())

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05"),
      ("2012-01-01", "2012-01-05")
    ))

    assert(res.length === 6)
    res foreach println
  }


}
