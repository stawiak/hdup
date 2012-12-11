package com.outsmart

import org.apache.hadoop.hbase.client.{Result, Scan, HTable}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.joda.time.DateTime
import org.apache.hadoop.hbase.util.Bytes

/**
 * @author Vadim Bobrov
*/
class ScannerImpl extends Scanner {

  val config = HBaseConfiguration.create()
  config.set("hbase.zookeeper.quorum", Settings.HOST)
  val table = new HTable(config, Settings.TABLE_NAME)

  def scan(customer : String, location : String, circuit : String, start : DateTime, end : DateTime) : Array[Measurement] = {
    scan(customer, location, circuit, start.getMillis, end.getMillis)
  }

  def scan(customer : String, location : String, circuit : String, start : Long, end : Long) : Array[Measurement] = {

    var output = List[Measurement]()

    val startRowKey = RowKeyUtils.createRowKey(customer, location, circuit, end)
    val endRowKey = RowKeyUtils.createRowKey(customer, location, circuit, start)

    val scan = new Scan(startRowKey, endRowKey)

    scan.addColumn(Bytes.toBytes(Settings.COLUMN_FAMILY_NAME), Bytes.toBytes(Settings.QUALIFIER_NAME))

    val results = table.getScanner(scan)

    var res : Result = null

    println("init scanner")

    while((res = results.next()) != null) {
      val value = res.getValue(Bytes.toBytes(Settings.COLUMN_FAMILY_NAME), Bytes.toBytes(Settings.QUALIFIER_NAME))
      output = new Measurement(Bytes.toLong(value), RowKeyUtils.getTimestamp(res.getRow)) :: output
    }

    results.close()
    output.toArray
  }


}
