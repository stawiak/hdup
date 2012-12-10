package com.outsmart;

import com.google.common.primitives.Longs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public long[] scan(String customer, String location, String circuit, DateTime start, DateTime end) throws IOException {
        return scan(customer, location, circuit, start.getMillis(), end.getMillis());
    }

    public long[] scan(String customer, String location, String circuit, Long start, Long end) throws IOException {

        List<Long> output = new ArrayList();
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
                output.add(msmt);
            }
        } finally {
            results.close();

        }

        return Longs.toArray(output);
    }

    public int[] raz() {
        return new int[]{1,2,3};
    }

}
