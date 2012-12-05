package com.hdup;

import org.junit.Test;

/**
 * @author Vadim Bobrov
 */
public class WriterTest {

    private Writer writer = new Writer();

    @Test
    public void testWrite() throws Exception {
        writer.write("a", "b", "c", 22L, 7);
    }
}
