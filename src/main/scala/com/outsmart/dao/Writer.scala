package com.outsmart.dao

import com.outsmart.util.OpenClosable

/**
 * @author Vadim Bobrov
*/
trait Writer extends OpenClosable {
  def write(customer : String, location : String, wireid : String, timestamp : Long, value : Long)
}
