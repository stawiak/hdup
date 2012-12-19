package com.outsmart.unit

import org.scalatest.{FlatSpec}
import com.outsmart.DataGenerator
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Vadim Bobrov
*/
class DataGeneratorTest extends FlatSpec with ShouldMatchers {

  val dataGen = new DataGenerator

  "Randon customer name" should "start with customer" in {
    assert(dataGen.getRandomCustomer.startsWith("customer"))
  }

  "Select customer name" should "be customer + i" in  {
    for(i <- 0 until 20)
      dataGen.getCustomer(i) should be ("customer" + i)
  }

  "Random location" should "start with location" in  {
    assert(dataGen.getRandomLocation.startsWith("location"))
  }

  "Select location" should "be location + i" in  {
    for(i <- 0 until 2)
      dataGen.getLocation(i) should be ("location" + i)
  }

  "Random wireid" should "start with wireid" in  {
    assert(dataGen.getRandomWireId.startsWith("wireid"))
  }

  "Select wireid" should "be wireid + i" in  {
    for(i <- 0 until 300)
      dataGen.getWireId(i) should be ("wireid" + i)
  }

  "Random measument" should "work" in  {
    dataGen.getRandomMeasurement
  }


}
