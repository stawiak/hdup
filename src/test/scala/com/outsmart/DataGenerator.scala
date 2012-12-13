package com.outsmart

import scala.util.Random

/**
 * @author Vadim Bobrov
*/
class DataGenerator {

  private val CUSTOMERS =  new Array[String](20)
  private val LOCATIONS =  Array[String]("location0", "location1")
  private val WIREIDS =  new Array[String](300)

  for (i <- 0 until 20)
    CUSTOMERS(i) = "customer" + i

  for (i <- 0 until 300)
    WIREIDS(i) = "wireid" + i

  private val random : Random = new Random()

  def getRandomCustomer = CUSTOMERS(random.nextInt(20))
  def getRandomLocation = LOCATIONS(random.nextInt(2))
  def getRandomWireId = WIREIDS(random.nextInt(300))
  def getRandomMeasurement = random.nextLong()

  def getCustomer(i : Int) = CUSTOMERS(i)
  def getLocation(i : Int) = LOCATIONS(i)
  def getWireId(i : Int) = WIREIDS(i)

}
