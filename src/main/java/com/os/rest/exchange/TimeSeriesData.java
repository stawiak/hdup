package com.os.rest.exchange;

/*
 * Copyright (c) 2010 OutSmart Power Systems, Inc. -- All Rights Reserved.
 */

import com.os.rest.exchange.json.JSONArray;
import com.os.rest.exchange.json.JSONObject;
import com.os.rest.exchange.json.JSONObjectSerializable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A time series
 * @author uwe
 */
public class TimeSeriesData extends TreeMap<Long, Double> implements JSONObjectSerializable, Cloneable {

	public enum Option {

		ABSOLUTE_VALUES_ONLY,
		EXTRAPOLATE,
		IGNORE_FALLING_SLOPE
	};

	public TimeSeriesData() {
	}

	/**
	 * Helper
	 * @param ts
	 * @param d
	 */
	public void put(Timestamp ts, double d) {
		super.put(ts.getTime(), d);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		String delim = "";
		for (long t : keySet()) {
			double d = get(t);
			sb.append(delim).append(t).append("/").append(d);
			delim = ",";
		}
		sb.append("]");
		return (sb.toString());
	}

	/**
	 * Convert this bean to a JSON string
	 * @return the json string
	 */
	public String toJSONString() {
		return (toJSON().toString());
	}

	/**
	 * Convert the time series to a json object
	 * @return the json object
	 */
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		try {
			JSONArray ja = new JSONArray();
			for (long t : keySet()) {
				double d = get(t);
				JSONObject item = new JSONObject();
				item.put(String.valueOf(t), d);
				ja.add(item);
			}
			jo.put("data", ja);
			jo.put("size", size());
		} catch (Exception e) {
			// ignore
		}
		return (jo);
	}

	/**
	 * Convert a json object to a time series.
	 * @param jo is the json object
	 */
	public void fromJSON(JSONObject jo) {
		try {
			JSONArray ja = jo.getJSONArray("data");
			int size = ja.length();
			for (int i = 0; i < size; i++) {
				JSONObject item = ja.getJSONObject(i);
				for (String timeString : item.keys()) {
					double value = item.getDouble(timeString);
					long time = Long.parseLong(timeString);
					put(new Timestamp(time), value);
				}
			}
		} catch (Exception e) {
			// @todo: maybe we should not ignore this
		}
	}

	/**
	 * Get the json object name
	 * @return
	 */
	public String getJSONObjectName() {
		return "TimeSeriesData";
	}

	/**
	 * Clone this time series
	 * @return a clone
	 */
	@Override
	public Object clone() {
		TimeSeriesData data = new TimeSeriesData();
		for (Long t : keySet()) {
			Double d = get(t);
			data.put(t, d);
		}
		return (data);
	}

	/**
	 * Merge another time series into this one
	 * @param other is the other time series
	 * @return a combined time series
	 */
	public final TimeSeriesData merge(TimeSeriesData other) {
		return merge(other, 0L, Long.MAX_VALUE);
	}

	public final TimeSeriesData merge(TimeSeriesData other, Long from, Long to) {
		TimeSeriesData result = new TimeSeriesData();
		result.mergeHere(this, from, to);
		result.mergeHere(other, from, to);
		return result;
	}

	/**
	 * Merge another time series into this one. NOTE: this mutates this time series!
	 * @param other
	 */
	public final void mergeHere(final TimeSeriesData other, Long from, Long to) {
		synchronized (other) {
			for (Map.Entry<Long, Double> entry : other.entrySet()) {
				Long key = entry.getKey();
				if (key >= from && key <= to) {
					this.put(key, entry.getValue());
				}
			}
		}
	}


	// ========================================================================
	// Math operations
	/**
	 * Add two time series
	 * @param other is the other time series
	 * @return a combined time series
	 */
	public final TimeSeriesData add(TimeSeriesData other) {
		TimeSeriesData data = new TimeSeriesData();
		addOperation(data, this, other);
		return (data);
	}

	/**
	 * Destructively add another time series
	 * @param other is the other time series
	 */
	public final void addHere(TimeSeriesData other) {
		addOperation(this, this, other);
	}

	/**
	 * Add 2 time series
	 * @param result the results
	 * @param ts1 the first time series
	 * @param ts2 the second time series
	 */
	private void addOperation(TimeSeriesData result, TimeSeriesData ts1, TimeSeriesData ts2) {
		// merge the time stamps
		Set<Long> keySet = new HashSet<Long>();
		keySet.addAll(ts1.keySet());
		keySet.addAll(ts2.keySet());
		for (Long t : keySet) {
			Double d1 = ts1.get(t);
			if (d1 == null) {
				d1 = 0.0;
			}
			Double d2 = ts2.get(t);
			if (d2 == null) {
				d2 = 0.0;
			}
			result.put(t, d1 + d2);
		}
	}

	/**
	 * Add two time series
	 * @return a combined time series
	 */
	public final TimeSeriesData add(double value) {
		TimeSeriesData data = new TimeSeriesData();
		addOperation(data, this, value);
		return (data);
	}

	/**
	 * Destructivly add another time series
	 */
	public final void addHere(double value) {
		addOperation(this, this, value);
	}

	/**
	 * Add 2 time series
	 * @param result the results
	 * @param ts1 the first time series
	 */
	private final void addOperation(TimeSeriesData result, TimeSeriesData ts1, double value) {
		for (Long t : ts1.keySet()) {
			Double d1 = ts1.get(t);
			result.put(t, d1 + value);
		}
	}

	/**
	 * Multiply a time series with a value
	 * @param s is the scale
	 * @return a modified time series
	 */
	public final TimeSeriesData times(double s) {
		TimeSeriesData result = new TimeSeriesData();
		timesOperation(result, this, s);
		return (result);
	}

	/**
	 * Multiply a time series in place with a value
	 * @param s is the scale
	 * @return a modified time series
	 */
	public final void timesHere(double s) {
		timesOperation(this, this, s);
	}

	/**
	 * Add 2 time series
	 * @param result the results
	 */
	private void timesOperation(TimeSeriesData result, TimeSeriesData ts, double s) {
		for (Long t : ts.keySet()) {
			Double value = ts.get(t);
			result.put(t, s * value);
		}
	}
	/**
	 * Multiply two time series
	 * @param other is the other time series
	 * @return a combined time series
	 */
	public final TimeSeriesData times(TimeSeriesData other) {
		TimeSeriesData data = new TimeSeriesData();
		timesOperation(data, this, other);
		return (data);
	}

	/**
	 * Destructivly multiply another time series
	 * @param other is the other time series
	 */
	public final void timesHere(TimeSeriesData other) {
		timesOperation(this, this, other);
	}

	/**
	 * Multiply 2 time series
	 * @param result the results
	 * @param ts1 the first time series
	 * @param ts2 the second time series
	 */
	private void timesOperation(TimeSeriesData result, TimeSeriesData ts1, TimeSeriesData ts2) {
		// merge the time stamps
		Set<Long> keySet = new HashSet<Long>();
		keySet.addAll(ts1.keySet());
		keySet.addAll(ts2.keySet());
		for (Long t : keySet) {
			Double d1 = ts1.get(t);
			if (d1 == null) {
				d1 = 0.0;
			}
			Double d2 = ts2.get(t);
			if (d2 == null) {
				d2 = 0.0;
			}
			result.put(t, d1 * d2);
		}
	}

	/**
	 * Abs this time series data and get a new one
	 * @return the abs data
	 */
	public TimeSeriesData abs() {
		TimeSeriesData result = new TimeSeriesData();
		absOperation(result);
		return (result);
	}

	/**
	 * Abs this time series in place
	 */
	public void absHere() {
		absOperation(this);
	}

	/**
	 * The abe operation
	 * @param result
	 */
	private void absOperation(TimeSeriesData result) {
		for (Long t : keySet()) {
			Double value = get(t);
			result.put(t, Math.abs(value));
		}
	}

	/**
	 * See if we have a particular option in the list
	 * @param opt
	 * @param opts
	 * @return
	 */
	private final boolean haveOpt(Option opt, Option... opts) {
		if (opts == null) {
			return (false);
		}
		for (Option p : opts) {
			if (p == opt) {
				return (true);
			}
		}
		return (false);
	}

	/**
	 * Calculate the derivative of this time series.
	 * We are using a simple Newton estimation to calculate the derivative.
	 * <code>d = t2 - t1</code>
	 * <code>f'(x) ~ (f(x + d) - f(x)) / d</code>
	 * There are also a few options have help with the our knowlegde of the data.
	 * @return the derivative
	 */
	private final TimeSeriesData deriveNewton(TimeSeriesData source, Option... opts) {
		boolean extrapolateFirst = haveOpt(Option.EXTRAPOLATE, opts);
		boolean ignoreFallingSlope = haveOpt(Option.IGNORE_FALLING_SLOPE, opts);

		TimeSeriesData data = new TimeSeriesData();

		boolean first = true;
		double t1 = 0;
		double t2 = 0;
		double fx1 = 0;
		double fx2 = 0;

		for (Long t : source.keySet()) {
			// shift the previous iteration
			fx1 = fx2;
			t1 = t2;

			// get the next set
			t2 = t;
			fx2 = get(t);
			if (first) {
				first = false;
				continue;
			}
			double slope = (fx2 - fx1) / (t2 - t1);
			// ignore any falling slope
			if (ignoreFallingSlope && slope < 0) {
				slope = 0;
			}
			if (extrapolateFirst) {
				data.put((long) t1, slope);
				extrapolateFirst = false;
			}
			data.put(t, slope);
		}
		return (data);
	}

	/**
	 * Derive the power data, we assuming we have energy data at hand.
	 * @return the resulting time series
	 */
	public final TimeSeriesData derivePower(){
		TimeSeriesData d = deriveNewton(this, Option.IGNORE_FALLING_SLOPE,Option.EXTRAPOLATE);
		return d;
	}

	/**
	 * Get the point in time where we have max value;
	 * @return the time
	 */
	public final Long getMaxValueTime() {
		double v = Double.NEGATIVE_INFINITY;
		Long pt = new Long(0);
		for (Long t : keySet()) {
			double d = get(t);
			if (d > v) {
				pt = t;
				v = d;
			}
		}
		return (pt);
	}

	/**
	 * Get the max value
	 * @return the max value
	 */
	public final double getMaxValue() {
		return (get(getMaxValueTime()));
	}

	/**
	 * Get the point in time where we have min value;
	 * @return the time
	 */
	public final Long getMinValueTime() {
		double v = Double.POSITIVE_INFINITY;
		Long pt = new Long(0);
		for (Long t : keySet()) {
			double d = get(t);
			if (d < v) {
				pt = t;
				v = d;
			}
		}
		return (pt);
	}

	/**
	 * Get the min value
	 * @return the min value
	 */
	public final double getMinValue() {
		return (get(getMinValueTime()));
	}

	public final double getAverage(long from, long to) {
		Long fromKey = floorKey(from);
		Long toKey = ceilingKey(to);
		if(fromKey == null || toKey == null)
			throw new IllegalArgumentException("Invalid range "+from+" to "+to);

		double sum = 0.0;
		Map<Long,Double> subset = subMap(fromKey, toKey);
		for(double d : subset.values()) {
			sum += d;
		}

		return sum / (double)subset.size();
	}

	/**
	 * Get the average value in the set
	 * @return the average value
	 */
	public final double getAverage() {
		return (getSum() / (double) size());
	}

	/**
	 * Sum up all the values in the data set.
	 * @return the sum
	 */
	public final double getSum() {
		double total = 0.0;
		for (double d : values()) {
			total += d;
		}
		return (total);
	}

	/**
	 * Calculate the duty cycle in this time series.
	 * @return the duty cycle
	 */
	public final double getDutyCycle() {
		return (getDutyCycle(getAverage()));
	}

	/**
	 * Calculate the duty cycle in this time series relativ to
	 * a threshold value
	 * @param threshold is the base line value
	 * @return the duty cycle
	 */
	public final double getDutyCycle(double threshold) {
		TimeSeriesData dc = new TimeSeriesData();
		for (Long t : keySet()) {
			double value = get(t) - threshold;
			if (value > 0) {
				dc.put(t, 1.0);
			} else {
				dc.put(t, 0.0);
			}
		}
		return (dc.getAverage());
	}
}
