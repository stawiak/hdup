package com.outsmart;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * @author Vadim Bobrov
 */
public class Scanner {

    private HTable table;

    public Scanner() throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", Settings.HOST);

        table = new HTable(config, Settings.TABLE_NAME);
    }

    public void scan(String customer, String location, String circuit, DateTime start, DateTime end) throws IOException {
        scan(customer, location, circuit, start.getMillis(), end.getMillis());
    }

    public void scan(String customer, String location, String circuit, Long start, Long end) throws IOException {

        byte[] startRowKey = RowKeyUtil.createRowKey(customer, location, circuit, end);
        byte[] endRowKey = RowKeyUtil.createRowKey(customer, location, circuit, start);

        Scan scan = new Scan(startRowKey, endRowKey);

        scan.addColumn(Bytes.toBytes(Settings.COLUMN_FAMILY_NAME), Bytes.toBytes(Settings.QUALIFIER_NAME));

        ResultScanner results = table.getScanner(scan);
        Result res = null;
        System.out.println("init scanner");
        try {

            while((res = results.next()) != null) {
                byte[] row = res.getRow();
                byte[] value = res.getValue(Bytes.toBytes(Settings.COLUMN_FAMILY_NAME), Bytes.toBytes(Settings.QUALIFIER_NAME));
                long msmt = Bytes.toLong(value);
                System.out.println(msmt);
            }
        } finally {
            results.close();
        }


    }

}
