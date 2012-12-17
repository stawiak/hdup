package com.outsmart.dao

import com.outsmart.util.OpenClosable
import com.outsmart.measurement.Measurement

/**
 * @author Vadim Bobrov
*/
trait Writer extends OpenClosable {
  def write(msmt: Measurement)

}

object Writer {
  def create() : Writer = new WriterImpl()
}
