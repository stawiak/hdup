package com.outsmart


/**
 * @author Vadim Bobrov
*/
trait Writer extends OpenClosable {
  def write(customer : String, location : String, wireid : String, timestamp : Long, value : Long)
}
