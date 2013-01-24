package com.os.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Vadim Bobrov
 */
@XmlRootElement
public class TimeSeriesElement {

	private long timestamp;
	private double value;

	public TimeSeriesElement() {
	}

	TimeSeriesElement(long timestamp, double message) {
		this.timestamp = timestamp;
		this.value = message;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public double getValue() {
		return value;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(timestamp) + "/" + value;
	}
}
