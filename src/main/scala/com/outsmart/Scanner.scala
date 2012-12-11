package com.outsmart

import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
trait Scanner {
  def scan(customer : String, location : String, wireid : String, start : DateTime, end : DateTime) : Array[Measurement]
  def scan(customer : String, location : String, wireid : String, start : Long, end : Long) : Array[Measurement]
}
