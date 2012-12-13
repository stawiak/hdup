package com.outsmart.dao

import org.joda.time.DateTime
import com.outsmart.Measurement

/**
 * @author Vadim Bobrov
*/
trait Scanner {
  def scan(customer : String, location : String, wireid : String, start : DateTime, end : DateTime) : Array[Measurement] = {
    scan(customer, location, wireid, start.getMillis, end.getMillis)
  }

  def scan(customer : String, location : String, wireid : String, start : Long, end : Long) : Array[Measurement]
}
