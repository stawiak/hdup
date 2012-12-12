package com.outsmart

/**
 * @author Vadim Bobrov
*/
object Util {

  def withOpenClose(op: => Unit, oc : OpenClosable ) {
    oc.open

    try{
      op
    } finally {
      oc.close
    }

  }
}
