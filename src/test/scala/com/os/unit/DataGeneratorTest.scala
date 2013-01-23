package com.os.unit

import org.scalatest.FlatSpec
import com.os.DataGenerator
import org.scalatest.matchers.ShouldMatchers
import com.os.util.Timing

/**
 * @author Vadim Bobrov
 */
class DataGeneratorTest extends FlatSpec with ShouldMatchers with Timing {


	"Randon customer name" should "start with customer" in {
		DataGenerator.getRandomCustomer should startWith ("customer")
	}

	"Select customer name" should "be customer + i" in  {
		for(i <- 0 until 20)
			DataGenerator.getCustomer(i) should be ("customer" + i)
	}

	"Random location" should "start with location" in  {
		DataGenerator.getRandomLocation should startWith ("location")
	}

	"Select location" should "be location + i" in  {
		for(i <- 0 until 2)
			DataGenerator.getLocation(i) should be ("location" + i)
	}

	"Random wireid" should "start with wireid" in  {
		DataGenerator.getRandomWireId should startWith ("wireid")
	}

	"Select wireid" should "be wireid + i" in  {
		for(i <- 0 until 300)
			DataGenerator.getWireId(i) should be ("wireid" + i)
	}

	"Random measument" should "work" in  {
		DataGenerator.getRandomMeasurement
	}

	"daily data iterator" should "return correct number of measurements" in {
		time {
			val iterator = DataGenerator.dailyDataIterator(60 * 24)
			iterator.length should be (3456000)
		}
	}

}
