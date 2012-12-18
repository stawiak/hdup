package com.outsmart.dao

import org.apache.hadoop.hbase.client.{HTableInterface, Put}
import org.apache.hadoop.hbase.util.Bytes
import com.outsmart.Settings
import com.outsmart.measurement.Measurement

/**
 * @author Vadim Bobrov
*/
class TestWriterImpl extends Writer {

  def open() {}

  def write(msmt: Measurement) { TestWriterImpl.counter += 1 }

  def close()  {}

}

object TestWriterImpl {
  @volatile var counter = 0
}
