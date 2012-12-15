package com.outsmart.dao

import org.joda.time.DateTime
import com.outsmart.measurement.MeasuredValue

/**
 * @author Vadim Bobrov
*/
trait Scanner {
  def scan(customer : String, location : String, wireid : String, start : DateTime, end : DateTime) : Array[MeasuredValue] = {
    scan(customer, location, wireid, start.getMillis, end.getMillis)
  }

  def scan(customer : String, location : String, wireid : String, start : Long, end : Long) : Array[MeasuredValue]
}
