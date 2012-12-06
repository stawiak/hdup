package com.outsmart;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Bobrov
 */
public class RowKeyUtilTest {

    private static final char[] symbols = new char[36];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }

    private final Random random = new Random();

    private String randomString() {
        char[] buf  = new char[random.nextInt(100)];

        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];

        return new String(buf);
    }

    @Test
    public void testGetHash() throws Exception {
        assertEquals(RowKeyUtil.getHash(randomString()).length, 16);
    }

    @Test
    public void testRowKey() throws Exception {
        byte[] rowkey = RowKeyUtil.createRowKey(randomString(), randomString(), randomString(), 7L);
        assertEquals(rowkey.length, 56);
    }
}
