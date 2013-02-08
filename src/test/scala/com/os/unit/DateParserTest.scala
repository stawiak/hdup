package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql._
import com.os.mql.MQLFrom
import com.os.mql.MQLSelect
import com.os.mql.MQLQuery
import com.os.mql.MQLTableEnergy
import com.os.mql.MQLTableRollup


/**
 * @author Vadim Bobrov
 */
class DateParserTest extends FlatSpec with ShouldMatchers {

	val parser = new Object with DateParsers

	"date parser" should "parse yyyy-MM-dd HH:mm:ss format" in {
		val mql: String = "2012-03-14 16:21:55"
		val res = parser.parseAll(parser.dateTime("yyyy-MM-dd HH:mm:ss"), mql)
		res.get.year.get should be (2012)
		res.get.monthOfYear.get should be (3)
		res.get.dayOfMonth.get should be (14)
		res.get.hourOfDay.get should be (16)
		res.get.minuteOfHour.get should be (21)
		res.get.secondOfMinute.get should be (55)
	}

}
