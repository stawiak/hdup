package com.hdup;

import org.apache.hadoop.hbase.util.Bytes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Vadim Bobrov
 */
public class RowKeyUtil {

    private static final int SIZEOF_STRING = 16;

    public static byte[] createRowKey(String customer, String location, String wireid, Long timestamp){

        byte[] rowkey = new byte[SIZEOF_STRING + SIZEOF_STRING + SIZEOF_STRING + Bytes.SIZEOF_LONG];


        Bytes.putBytes(rowkey, 0, getHash(customer), 0, SIZEOF_STRING);
        Bytes.putBytes(rowkey, SIZEOF_STRING, getHash(location), 0, SIZEOF_STRING);
        Bytes.putBytes(rowkey, SIZEOF_STRING + SIZEOF_STRING, getHash(wireid), 0, SIZEOF_STRING);

        long reverseTimestamp = Long.MAX_VALUE - timestamp;
        Bytes.putLong(rowkey, SIZEOF_STRING + SIZEOF_STRING + SIZEOF_STRING, reverseTimestamp);

        return rowkey;
    }

    /*
    * get a unique (almost) hash for a string to use in row key
     */
    public static byte[] getHash(String s) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // do something
        }

        md.update(s.getBytes());
        return md.digest();
    }


}
