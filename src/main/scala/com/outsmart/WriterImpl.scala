package com.outsmart

import org.apache.hadoop.hbase.client.{Put, Result, Scan, HTable}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.util.Bytes

/**
 * @author Vadim Bobrov
*/
class WriterImpl extends Writer {

  private var table: HTable = null


  def open() {
    table = TableFactory.getTable()
  }

  def write(customer : String, location : String, wireid : String, timestamp : Long, value : Long) {

    val rowkey = RowKeyUtils.createRowKey(customer, location, wireid, timestamp)
    val p = new Put(rowkey)

    p.add(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.QualifierName),Bytes.toBytes(value))
    table.put(p)
  }


  def close()  {
    // this should be called at the end of batch write
    table.close()
  }

}
