package com.outsmart

import org.scalatest.FunSuite

/**
 * @author Vadim Bobrov
*/
class RowKeyUtilsTest extends FunSuite {

  test("timestamp correct") {
    val rowkey = RowKeyUtils.createRowKey("customer", "location", "wireid", 123)
    assert(RowKeyUtils.getTimestamp(rowkey) == 123)
  }


}
