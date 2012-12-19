package com.outsmart.unit

import org.scalatest.FunSuite
import com.outsmart.DataGenerator

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
      assert(dataGen.getCustomer(i) === ("customer" + i))
  }

  test("random location") {
    val dataGen = new DataGenerator
    assert(dataGen.getRandomLocation.startsWith("location"))
  }

  test("select location") {
    val dataGen = new DataGenerator
    for(i <- 0 until 2)
      assert(dataGen.getLocation(i) === ("location" + i))
  }

  test("random wireid") {
    val dataGen = new DataGenerator
    assert(dataGen.getRandomWireId.startsWith("wireid"))
  }

  test("select wireid") {
    val dataGen = new DataGenerator
    for(i <- 0 until 300)
      assert(dataGen.getWireId(i) === ("wireid" + i))
  }

  test("random measurement") {
    val dataGen = new DataGenerator
    dataGen.getRandomMeasurement
  }

}
