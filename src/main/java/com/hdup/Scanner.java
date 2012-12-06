package com.hdup;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * @author Vadim Bobrov
 */
public class Scanner {

    private HTable table;

    public Scanner() throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "localhost");

        table = new HTable(config, "test");
    }

    public void scan(String customer, String location, String circuit, Long start, Long end) throws IOException {

        byte[] startRowKey = RowKeyUtil.createRowKey(customer, location, circuit, end);
        byte[] endRowKey = RowKeyUtil.createRowKey(customer, location, circuit, start);

        Scan scan = new Scan(startRowKey, endRowKey);

        scan.addColumn(Bytes.toBytes("data"), Bytes.toBytes("power"));

        ResultScanner results = table.getScanner(scan);
        Result res = null;
        System.out.println("init scanner");
        try {

            while((res = results.next()) != null) {
                byte[] row = res.getRow();
                byte[] value = res.getValue(Bytes.toBytes("data"), Bytes.toBytes("power"));
                long msmt = Bytes.toLong(value);
                System.out.println(msmt);
            }
        } finally {
            results.close();
        }


    }

}
