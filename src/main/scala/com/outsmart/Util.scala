package com.outsmart

/**
 * @author Vadim Bobrov
*/
object Util {

  def withOpenClose(oc : OpenClosable)(op: => Unit) {
    oc.open

    try{
      op
    } finally {
      oc.close
    }

  }
}
