package com.outsmart


/**
 * @author Vadim Bobrov
*/
trait Writer {
  def open()
  def write(customer : String, location : String, wireid : String, timestamp : Long, value : Long)
  def close()
}
