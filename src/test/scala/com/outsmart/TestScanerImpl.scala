package com.outsmart

import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
class TestScanerImpl extends Scanner{
  def scan(customer: String, location: String, wireid: String, start: DateTime, end: DateTime): Array[Measurement] = {
    scan(customer, location, wireid, start.getMillis, end.getMillis)
  }

  def scan(customer: String, location: String, wireid: String, start: Long, end: Long): Array[Measurement] = {
    Array(new Measurement(1,1),new Measurement(1,1),new Measurement(1,1))
  }
}
