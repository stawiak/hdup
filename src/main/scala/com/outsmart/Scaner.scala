package com.outsmart

import org.joda.time.DateTime

/**
 * @author Vadim Bobrov
*/
trait Scaner {
  def scan(customer : String, location : String, circuit : String, start : DateTime, end : DateTime) : Array[Measurement]
  def scan(customer : String, location : String, circuit : String, start : Long, end : Long) : Array[Measurement]
}
