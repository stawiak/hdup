package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import util.Random
import com.os.dao.clwt.CLWTRowKeyUtils

/**
 * @author Vadim Bobrov
 */
class RowKeyUtilsTest extends FlatSpec with ShouldMatchers {

	"RowKeyUtils" should "read back correct timestamp" in {
		val random = new Random()

		for (i <-1 to 1000) {
			val ts = random.nextLong()
			val rowkey = CLWTRowKeyUtils.createRowKey("customer", "location", "wireid", ts)
			CLWTRowKeyUtils.getTimestamp(rowkey) should be (ts)
		}
	}


}
