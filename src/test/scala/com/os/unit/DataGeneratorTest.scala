package com.os.unit

import org.scalatest.FlatSpec
import com.os.DataGenerator
import org.scalatest.matchers.ShouldMatchers
import com.os.util.Timing

/**
 * @author Vadim Bobrov
 */
class DataGeneratorTest extends FlatSpec with ShouldMatchers with Timing {

	val dataGenerator = new DataGenerator

	"Randon customer name" should "start with customer" in {
		dataGenerator.getRandomCustomer should startWith ("customer")
	}

	"Select customer name" should "be customer + i" in  {
		for(i <- 0 until 20)
			dataGenerator.getCustomer(i) should be ("customer" + i)
	}

	"Random location" should "start with location" in  {
		dataGenerator.getRandomLocation should startWith ("location")
	}

	"Select location" should "be location + i" in  {
		for(i <- 0 until 2)
			dataGenerator.getLocation(i) should be ("location" + i)
	}

	"Random wireid" should "start with wireid" in  {
		dataGenerator.getRandomWireId should startWith ("wireid")
	}

	"Select wireid" should "be wireid + i" in  {
		for(i <- 0 until 300)
			dataGenerator.getWireId(i) should be ("wireid" + i)
	}

	"Random measument" should "work" in  {
		dataGenerator.getRandomMeasurement
	}

	"daily data iterator" should "return correct number of measurements" in {
		time {
			val iterator = dataGenerator.dailyDataIterator(60 * 24, false)
			iterator.length should be (3456000)
		}
	}

}
