package com.hdup;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * @author Vadim Bobrov
 */
public class Writer {

    private HTable table;

    public Writer() throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "localhost");

        table = new HTable(config, "test");
        table.setAutoFlush(false);
        //table.setWriteBufferSize(100);  this is 2 Mb by default
    }

    public void write(String customer, String location, String wireid, Long timestamp, long msmt) throws IOException {

        byte[] rowkey = RowKeyUtil.createRowKey(customer, location, wireid, timestamp);
        Put p = new Put(rowkey);

        p.add(Bytes.toBytes("data"), Bytes.toBytes("power"),Bytes.toBytes(msmt));
        table.put(p);
    }

    public void close() throws IOException {
        // this should be called at the end of batch write
        table.close();
    }

}
