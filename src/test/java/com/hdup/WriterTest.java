package com.hdup;

import org.junit.Test;

/**
 * @author Vadim Bobrov
 */
public class WriterTest {

    private Writer writer = new Writer();

    @Test
    public void testWrite() throws Exception {
        for(int i = 0; i < 10; i++)
            writer.write("a", "b", "c", i + 22L, i + 7);
    }
}
