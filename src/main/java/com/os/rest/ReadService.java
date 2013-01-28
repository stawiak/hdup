package com.os.rest;

import com.os.ActorService;
import com.os.rest.exchange.TimeSeriesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

/*
		Timeout timeout = new Timeout(60000);
		MeasurementReadRequest readRequest = new MeasurementReadRequest("customer0", "location0", "wireid0", new Interval[]{new Interval(0, Long.MAX_VALUE)});
		Future<Object> future = ask(actorService.getReadMaster(), readRequest, 10000);

		Object obj = (Object)Await.result(future, timeout.duration());
		List<MeasuredValue> res = (List<MeasuredValue>)obj;
*/

		TimeSeriesData tsd = new TimeSeriesData();
/*
		for(MeasuredValue mv : res)
			tsd.put(new Timestamp(mv.timestamp()), mv.energy());
*/

		return tsd.toJSONString();
	}


}
