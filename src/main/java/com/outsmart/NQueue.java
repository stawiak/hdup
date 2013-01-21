package com.outsmart;

import com.outsmart.measurement.TimedValue;

/**
 * @author Vadim Bobrov
 */
public interface NQueue {
	TimedValue get(int i);

	boolean isFull();

	void offer(TimedValue tv);
}
