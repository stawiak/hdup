package com.outsmart.dao

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.HBaseConfiguration
import com.outsmart.Settings

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


  /** If you were ever required to access the write buffer content, you would find that
    ArrayList<Put> getWriteBuffer() can be used to get the in- ternal list of buffered
    Put instances you have added so far calling
    table.put(put).
      I mentioned earlier that it is exactly that list that makes HTable not safe
    for multithreaded use. Be very careful with what you do to that list when
    accessing it directly. You are bypassing the heap size checks, or you
    might modify it while a flush is in progress!

    Since the client buffer is a simple list retained in the local process mem-
    ory, you need to be careful not to run into a problem that terminates
    the process mid-flight. If that were to happen, any data that has not yet
    been flushed will be lost! The servers will have never received that data,
    and therefore there will be no copy of it that can be used to recover from
    this situation.
  */
  def getTable() : HTable = {
    println("giving table")
    try {
      val table = new HTable(config, Settings.TableName)
      table.setAutoFlush(false)
      //table.setWriteBufferSize(100)  this is 2 Mb by default
      table
    } catch {
      case e: Exception => {println("caught exception " + e); null}
    }
  }
}
