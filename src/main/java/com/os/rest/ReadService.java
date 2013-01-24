package com.os.rest;

import akka.util.Timeout;
import com.os.measurement.MeasuredValue;
import com.os.measurement.Measurement;
import com.os.rest.exchange.TimeSeriesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	public String getMessage() throws Exception {

		//MeasurementReadRequest readRequest = new MeasurementReadRequest("a", "b", "c")
		Measurement msmt = new Measurement("a", "b", "c", 1, 1, 1, 1);

		//actorService.getIncomingHandler().tell(msmt);
		Timeout timeout = new Timeout(10000);
		Future<Object> future = ask(actorService.getReadMaster(), "question", 10000);
		MeasuredValue[] res = (MeasuredValue[])Await.result(future, timeout.duration());
		TimeSeriesData tsd = new TimeSeriesData();

		for(MeasuredValue mv : res)
			tsd.put(new Timestamp(mv.timestamp()), mv.energy());

		return tsd.toJSONString();


/*
		List<TimeSeriesElement> list = new ArrayList<TimeSeriesElement>();
		list.add(new TimeSeriesElement(2,7));
		return new MeasurementList(list);
*/
	}


}
