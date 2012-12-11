package com.outsmart

import org.apache.hadoop.hbase.client.{Put, Result, Scan, HTable}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.joda.time.DateTime
import org.apache.hadoop.hbase.util.Bytes

/**
 * @author Vadim Bobrov
*/
class WriterImpl {

  val config = HBaseConfiguration.create()
  config.set("hbase.zookeeper.quorum", Setting.HOST)
  var table: HTable = null


  def open() {
    table = new HTable(config, Setting.TABLE_NAME)
    table.setAutoFlush(false)
    //table.setWriteBufferSize(100)  this is 2 Mb by default
  }

  def write(customer : String, location : String, wireid : String, timestamp : Long, value : Long) {

    val rowkey = RowKeyUtils.createRowKey(customer, location, wireid, timestamp)
    val p = new Put(rowkey)

    p.add(Bytes.toBytes(Setting.COLUMN_FAMILY_NAME), Bytes.toBytes(Setting.QUALIFIER_NAME),Bytes.toBytes(value))
    table.put(p)
  }


  def close()  {
    // this should be called at the end of batch write
    table.close()
  }

}
