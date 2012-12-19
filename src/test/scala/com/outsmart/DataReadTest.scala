package com.outsmart

import dao.ScannerServiceImpl
import org.scalatest.FunSuite

/**
 * @author Vadim Bobrov
*/
class DataReadTest extends FunSuite{

  test("returns the results of a single scanner") {
    val grabber : Grabber = new Grabber(new ScannerServiceImpl())

    val res = grabber.grab("customer0", "location0", "wireid0", Array[(String, String)](
      ("2012-02-01", "2012-02-05")
    ))

    println(res.length)

    assert(res(0).value === 22)
  }


  test("returns the results of two scanners") {
    val grabber : Grabber = new Grabber(new ScannerServiceImpl())

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-02-01", "2012-02-05"),
      ("2012-03-01", "2012-03-05")
    ))

    println(res.length)
    //assert(res(0).value === 555)
  }

  test("simple scanner test") {
    val scanner = new ScannerServiceImpl().getScanner()
    val res = scanner.scan("customer1", "location1", "wireid1", 1, 1)
    println(res.length)
    assert(res(0).value === 1)
  }

}
