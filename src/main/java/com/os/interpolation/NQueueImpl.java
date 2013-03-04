package com.os.interpolation;

import com.os.measurement.TimedValue;

import java.util.Arrays;

/**
 * @author Vadim Bobrov
 */
public class NQueueImpl implements NQueue {

	private final TimedValue[] elems = {null, null, null, null};
	private int counter = 0;

	@Override
	public TimedValue get(int i) {
		return elems[i];
	}

	@Override
	public boolean isFull(){
		return counter == 4;
	}

	@Override
	public TimedValue[] content() {
		return (counter == 0 ? new TimedValue[0] : Arrays.copyOfRange(elems, 4 - counter, 3));
	}

	@Override
	public void offer(TimedValue tv) {
		// skip duplicates
		if(tv != null && !(elems[3] != null && tv.equals(elems[3]))) {
			// must be sorted
			assert (elems[3] == null || tv.compareTo(elems[3]) > 0);
			elems[0] = elems[1];
			elems[1] = elems[2];
			elems[2] = elems[3];
			elems[3] = tv;
			if(counter < 4) counter++;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(TimedValue tv: elems)
			sb.append(tv == null? "null" : tv );

		return sb.toString();
	}
}
