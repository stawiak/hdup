package com.outsmart

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.HBaseConfiguration

/**
 * @author Vadim Bobrov
*/
object TableFactory {

    /** it is recommended that you create HTable instances only once—and one per thread
      * and reuse that instance for the rest of the lifetime of your client application.
      * As soon as you need multiple instances of HTable, consider using the HTablePool class
      * which provides you with a convenient way to reuse multiple instances.
    */

  private val config = HBaseConfiguration.create()
  config.set("hbase.zookeeper.quorum", Settings.Host)


  def getTable() : HTable = {
    val table = new HTable(config, Settings.TableName)
    table.setAutoFlush(false)
    //table.setWriteBufferSize(100)  this is 2 Mb by default
    table
  }
}
