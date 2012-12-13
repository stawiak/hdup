package com.outsmart

/**
 * @author Vadim Bobrov
 */
object Timer {

  def periodicCall(seconds: Int, callback: () => Unit) {
    while (true) {
      callback()
      Thread.sleep(seconds * 1000)
    }
  }

}