package com.hdup;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author Vadim Bobrov
 */
public class RowKeyUtil {

    private static final int SIZEOF_CUSTOMER = 22;
    private static final int SIZEOF_LOCATION = 22;
    private static final int SIZEOF_CIRCUIT = 22;

    public static byte[] createRowKey(String customer, String location, String circuit, Long timestamp){

        byte[] rowkey = new byte[SIZEOF_CUSTOMER + SIZEOF_LOCATION + SIZEOF_CIRCUIT + Bytes.SIZEOF_LONG];


        Bytes.putBytes(rowkey, 0, Bytes.toBytes(customer), 0, Bytes.toBytes(customer).length);
        Bytes.putBytes(rowkey, SIZEOF_CUSTOMER, Bytes.toBytes(location), 0, Bytes.toBytes(location).length);
        Bytes.putBytes(rowkey, SIZEOF_CUSTOMER + SIZEOF_LOCATION, Bytes.toBytes(circuit), 0, Bytes.toBytes(circuit).length);

        long reverseTimestamp = Long.MAX_VALUE - timestamp;
        Bytes.putLong(rowkey, SIZEOF_CUSTOMER + SIZEOF_LOCATION + SIZEOF_CIRCUIT, reverseTimestamp);

        return rowkey;
    }


}
