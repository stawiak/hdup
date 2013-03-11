package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.util.CounterMap


/**
 * @author Vadim Bobrov
 */
class CounterMapTest extends FlatSpec with ShouldMatchers {

	val counterMap = new CounterMap[String]()

	"counter map" should "correctly collect data" in {
		counterMap.incr("first")
		counterMap.incr("first")
		counterMap.incr("second")

		counterMap.toString should be ("first -> 2,second -> 1")
	}


}
