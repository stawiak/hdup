package com.os.rest;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Vadim Bobrov
 * A simple wrapper around a collection to pass through web-service
 */
@XmlRootElement
public class MeasurementList {

	private List<TimeSeriesElement> timeseries;

	public MeasurementList() {
	}

	public MeasurementList(List<TimeSeriesElement> timeseries) {
		this.timeseries = timeseries;
	}

	public List<TimeSeriesElement> getTimeseries() {
		return timeseries;
	}

	public void setTimeseries(List<TimeSeriesElement> timeseries) {
		this.timeseries = timeseries;
	}
}
