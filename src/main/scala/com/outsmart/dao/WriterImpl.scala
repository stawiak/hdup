package com.outsmart.dao

import org.apache.hadoop.hbase.client.{Put, HTable}
import org.apache.hadoop.hbase.util.Bytes
import com.outsmart.Settings

/**
 * @author Vadim Bobrov
*/
class WriterImpl extends Writer {

  private var table: HTable = null


  def open() {
    table = TableFactory.getTable()
  }

  /**
    (alternatively see Batch operations to group updates

      Those Put instances that have failed on the server side are kept in the local write buffer.
    They will be retried the next time the buffer is flushed. You can also access them using
    the getWriteBuffer() method of HTable and take, for example, evasive actions.
    Some checks are done on the client side, though—for example, to ensure that the put
    has a column specified or that it is completely empty. In that event, the client is throwing
    an exception that leaves the operations preceding the faulty one in the client buffer.

    The list-based put() call uses the client-side write buffer to insert all puts
    into the local buffer and then to call flushCache() implicitly. While in-
    serting each instance of Put, the client API performs the mentioned
    check. If it fails, for example, at the third put out of five—the first two
    are added to the buffer while the last two are not. It also then does not
    trigger the flush command at all.

    You need to watch out for a peculiarity using the list-based put call: you cannot control
    the order in which the puts are applied on the server side, which implies that the order
    in which the servers are called is also not under your control. Use this call with caution
    if you have to guarantee a specific order—in the worst case, you need to create smaller
    batches and explicitly flush the client-side write cache to enforce that they are sent to
    the remote servers
    */

  def write(customer : String, location : String, wireid : String, timestamp : Long, value : Long) {

    val rowkey = RowKeyUtils.createRowKey(customer, location, wireid, timestamp)
    val p = new Put(rowkey)

    p.add(Bytes.toBytes(Settings.ColumnFamilyName), Bytes.toBytes(Settings.QualifierName),Bytes.toBytes(value))
    // alternatively use
    // void put(List<Put> puts) throws IOException
    table.put(p)
  }


  def close()  {
    // this should be called at the end of batch write or
    // if not called table.flushCommits() should be issued
    table.close()
  }

}
