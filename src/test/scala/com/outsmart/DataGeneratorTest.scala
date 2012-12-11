package com.outsmart

import org.scalatest.FunSuite

/**
 * @author Vadim Bobrov
*/
class DataGeneratorTest extends FunSuite {

  test("random customer") {
    val dataGen = new DataGenerator
    assert(dataGen.getRandomCustomer.startsWith("customer"))
  }

  test("select customer") {
    val dataGen = new DataGenerator
    for(i <- 0 until 20)
      assert(dataGen.getCustomer(i).equals("customer" + i))
  }

  test("random location") {
    val dataGen = new DataGenerator
    assert(dataGen.getRandomLocation.startsWith("location"))
  }

  test("select location") {
    val dataGen = new DataGenerator
    for(i <- 0 until 2)
      assert(dataGen.getLocation(i).equals("location" + i))
  }

  test("random wireid") {
    val dataGen = new DataGenerator
    assert(dataGen.getRandomWireId.startsWith("wireid"))
  }

  test("select wireid") {
    val dataGen = new DataGenerator
    for(i <- 0 until 300)
      assert(dataGen.getWireId(i).equals("wireid" + i))
  }

  test("random measurement") {
    val dataGen = new DataGenerator
    assert(dataGen.getRandomMeasurement > 0)
  }

}
