package com.outsmart

import org.scalatest.FunSuite

/**
 * @author Vadim Bobrov
*/
class DataGenTest extends FunSuite {


  test("random customer") {
    val dataGen = new DataGen
    assert(dataGen.getRandomCustomer.startsWith("customer"))
  }




}
