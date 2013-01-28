package com.os.rest;

import akka.util.Timeout;
import com.os.ActorService;
import com.os.actor.read.MeasurementReadRequest;
import com.os.measurement.MeasuredValue;
import com.os.rest.exchange.TimeSeriesData;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.sql.Timestamp;

import static akka.pattern.Patterns.ask;

/**
 * @author Vadim Bobrov
 */

@Controller
@RequestMapping("/data")
public class ReadService {

	private static final Logger log = LoggerFactory.getLogger(ReadService.class);

	@Autowired
	private ActorService actorService;


	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody String getMessage() throws Exception {

		Timeout timeout = new Timeout(60000);
		MeasurementReadRequest readRequest = new MeasurementReadRequest("customer0", "location0", "wireid0", new Interval[]{new Interval(0, Long.MAX_VALUE)});


		Future<Object> future = ask(actorService.getReadMaster(), readRequest, 10000);

		Iterable<MeasuredValue> res = (Iterable<MeasuredValue>)Await.result(future, timeout.duration());

		TimeSeriesData tsd = new TimeSeriesData();
		for(MeasuredValue mv : res)
			tsd.put(new Timestamp(mv.timestamp()), mv.energy());

		return tsd.toJSONString();
	}


}
