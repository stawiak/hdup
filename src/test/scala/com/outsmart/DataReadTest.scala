package com.outsmart

import org.scalatest.FunSuite

/**
 * @author Vadim Bobrov
*/
class DataReadTest extends FunSuite{

  test("returns the results of a single scanner") {
    val grabber : Grabber = new Grabber(new ScannerServiceImpl())

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05")
    ))

    println(res.length)

    assert(res(0).value == 111)
  }


  test("returns the results of two scanners") {
    val grabber : Grabber = new Grabber(new ScannerServiceImpl())

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-05-01", "2012-05-05"),
      ("2012-05-01", "2012-05-05")
    ))

    println(res.length)
    assert(res(0).value == 555)
  }

}
