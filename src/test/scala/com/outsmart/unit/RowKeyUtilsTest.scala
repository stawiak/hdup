package com.outsmart.unit

import org.scalatest.{FlatSpec}
import com.outsmart.dao.RowKeyUtils
import org.scalatest.matchers.ShouldMatchers
import util.Random

/**
 * @author Vadim Bobrov
*/
class RowKeyUtilsTest extends FlatSpec with ShouldMatchers {

  "RowKeyUtils" should "read back correct timestamp" in {
    val random = new Random()

    for (i <-1 to 1000) {
      val ts = random.nextLong()
      val rowkey = RowKeyUtils.createRowKey("customer", "location", "wireid", ts)
      RowKeyUtils.getTimestamp(rowkey) should be (ts)
    }
  }


}
