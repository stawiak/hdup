package com.outsmart

import dao.Scanner
import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
class TestScanerImpl extends Scanner{
  def scan(customer: String, location: String, wireid: String, start: Long, end: Long): Array[Measurement] = {
    Array(new Measurement(1,1),new Measurement(2,2),new Measurement(3,3))
  }
}
