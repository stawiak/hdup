package com.outsmart.unit

import org.scalatest.{FlatSpec, FunSuite}
import com.outsmart.{TestScannerServiceImpl, Grabber}
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
*/
class GrabberTest extends FlatSpec with ShouldMatchers {

  val grabber : Grabber = new Grabber(new TestScannerServiceImpl())

  "A single scanner" should "return 3 values" in {
    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05")
    ))

    res.length should be (3)
  }

  it should "be 1,2,3" in {
    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05")
    ))

    res(0).value should be (1)
    res(1).value should be (2)
    res(2).value should be (3)
  }


  "2 scanners" should "return 6 values" in {

    val res = grabber.grab("customer1", "location1", "wireid1", Array[(String, String)](
      ("2012-01-01", "2012-01-05"),
      ("2012-01-01", "2012-01-05")
    ))

    res.length should be (6)
  }


}
