package com.outsmart.dao

import com.outsmart.util.OpenClosable
import com.outsmart.measurement.Measurement
import com.outsmart.Settings

/**
 * @author Vadim Bobrov
*/
trait Writer extends OpenClosable {
  def write(msmt: Measurement)

}

object Writer {
	def apply(tableName : String = Settings.TableName) : Writer = new WriterImpl(tableName)
}
