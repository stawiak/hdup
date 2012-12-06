package com.outsmart;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Vadim Bobrov
 */
public class WriterTest {

    private Writer writer;

    @Before
    public void setUp() throws Exception {
        writer = new Writer();
    }

    @Test
    public void testWrite() throws Exception {
        for(int i = 0; i < 10; i++)
            writer.write("a", "b", "c", i + 22L, i + 7);
    }
}
