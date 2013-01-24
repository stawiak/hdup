package com.os.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vadim Bobrov
 */

@Controller
@RequestMapping("/data")
public class ReadService {

	private static final Logger log = LoggerFactory.getLogger(ReadService.class);

	@Autowired
	//private PasswordValidationService passwordValidationService;


	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody MeasurementList getMessage(){
		List<TimeSeriesElement> list = new ArrayList<TimeSeriesElement>();
		list.add(new TimeSeriesElement(2,7));
		return new MeasurementList(list);
	}


}
