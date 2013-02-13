package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.parser.MQLParsers
import com.os.actor.read.{RollupReadRequest, MeasurementReadRequest}
import org.joda.time.Interval
import com.os.mql.executor.MQLExecutor


/**
 * @author Vadim Bobrov
 */
class MQLExecutorTest extends FlatSpec with ShouldMatchers {

	val parser = new MQLParsers()

	"MQL executor" should "generate single request" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and location = \"a\" and wireid = \"a\" and value > 3.5"
		val requests = new MQLExecutor(parser.parseAll(parser.mql, mql).get).generateExecutePlan
		println(requests)
		requests should be (List(MeasurementReadRequest("msmt","a","a","a", List())))
	}

	it should "generate single request and a period" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and location = \"a\" and wireid = \"a\" and timestamp between 1 and 2"
		val requests = new MQLExecutor(parser.parseAll(parser.mql, mql).get).generateExecutePlan
		println(requests)
		requests should be (List(MeasurementReadRequest("msmt","a","a","a", List(new Interval(1, 2)))))
	}

	it should "generate single rollup request and a period" in {
		val mql: String = "select timestamp, value from rollup where customer = \"a\" and location = \"a\" and timestamp between 1 and 2"
		val requests = new MQLExecutor(parser.parseAll(parser.mql, mql).get).generateExecutePlan
		println(requests)
		requests should be (List(RollupReadRequest("a","a",List(new Interval(1, 2)))))
	}

	it should "generate 2 requests for a union" in {
		val mql: String = "select timestamp, value from energy " +
			"where customer = \"a\" and location = \"a\" and wireid = \"a\" and " +
			"timestamp between 1 and 2 " +
			"union " +
			"select timestamp, value from rollup " +
			"where customer = \"a\" and location = \"a\" and timestamp between 2 and 3"
		val requests = new MQLExecutor(parser.parseAll(parser.mql, mql).get).generateExecutePlan
		println(requests)
		requests.head should be (MeasurementReadRequest("msmt","a","a","a", List(new Interval(1, 2))))
		requests.tail should be (List(RollupReadRequest("a","a",List(new Interval(2, 3)))))
	}

}
