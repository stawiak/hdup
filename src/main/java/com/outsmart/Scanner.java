package com.outsmart;

import org.joda.time.DateTime;

import java.io.IOException;

/**
 * @author Vadim Bobrov
 */
public interface Scanner {
    long[] scan(String customer, String location, String circuit, DateTime start, DateTime end) throws IOException;

    long[] scan(String customer, String location, String circuit, Long start, Long end) throws IOException;
}
