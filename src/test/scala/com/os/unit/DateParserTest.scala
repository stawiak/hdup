package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.parser.DateParsers


/**
 * @author Vadim Bobrov
 */
class DateParserTest extends FlatSpec with ShouldMatchers {

	val parser = new Object with DateParsers

	"date parser" should "parse to seconds" in {
		val res = parser.parseAll(parser.toSeconds, "2012-03-14 16:21:55")
		res.get.year.get should be (2012)
		res.get.monthOfYear.get should be (3)
		res.get.dayOfMonth.get should be (14)
		res.get.hourOfDay.get should be (16)
		res.get.minuteOfHour.get should be (21)
		res.get.secondOfMinute.get should be (55)
	}

	it should "parse to day" in {
		val res = parser.parseAll(parser.toDay, "2012-03-14")
		res.get.year.get should be (2012)
		res.get.monthOfYear.get should be (3)
		res.get.dayOfMonth.get should be (14)
		res.get.hourOfDay.get should be (0)
		res.get.minuteOfHour.get should be (0)
		res.get.secondOfMinute.get should be (0)
	}

}
