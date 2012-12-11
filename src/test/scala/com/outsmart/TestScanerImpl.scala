package com.outsmart

import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
class TestScanerImpl extends Scaner{
  def scan(customer: String, location: String, circuit: String, start: DateTime, end: DateTime): Array[Measurement] = {
    scan(customer, location, circuit, start.getMillis, end.getMillis)
  }

  def scan(customer: String, location: String, circuit: String, start: Long, end: Long): Array[Measurement] = {
    Array(new Measurement(1,1),new Measurement(1,1),new Measurement(1,1))
  }
}
