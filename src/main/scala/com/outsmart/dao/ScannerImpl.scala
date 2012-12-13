package com.outsmart.dao

import org.apache.hadoop.hbase.client.{Result, Scan, HTable}
import org.apache.hadoop.hbase.util.Bytes
import com.outsmart.{Settings, Measurement}


/**
 * @author Vadim Bobrov
*/
class ScannerImpl extends Scanner {

  def scan(customer : String, location : String, wireid : String, start : Long, end : Long) : Array[Measurement] = {

    val table = TableFactory.getTable()

    var output = List[Measurement]()

    val startRowKey = RowKeyUtils.createRowKey(customer, location, wireid, end)
    val endRowKey = RowKeyUtils.createRowKey(customer, location, wireid, start)

    val scan = new Scan(startRowKey, endRowKey)

    scan.addColumn(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.QualifierName))

    val results = table.getScanner(scan)

    var res : Result = null

    println("init scanner")

    val iterator = Iterator.continually(results.next()) takeWhile (_ != null)

    iterator foreach (res => {
      val value = res.getValue(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.QualifierName))
      output = new Measurement(Bytes.toLong(value), RowKeyUtils.getTimestamp(res.getRow)) :: output
    })

    results.close()
    output.toArray
  }


}
