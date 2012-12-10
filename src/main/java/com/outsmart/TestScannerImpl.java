package com.outsmart;

import com.google.common.primitives.Longs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vadim Bobrov
 */
public class TestScannerImpl implements Scanner {

    @Override
    public long[] scan(String customer, String location, String circuit, DateTime start, DateTime end) throws IOException {
        return scan(customer, location, circuit, start.getMillis(), end.getMillis());
    }

    @Override
    public long[] scan(String customer, String location, String circuit, Long start, Long end) throws IOException {
        return new long[]{1, 2, 3};
    }

}
