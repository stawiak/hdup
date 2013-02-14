package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.model._
import com.os.mql.model.MQLSelect
import com.os.mql.model.MQLQuery
import com.os.mql.model.MQLColumnTimestamp
import com.os.mql.model.MQLColumnValue
import scala.Some
import com.os.mql.parser.MQLParser.MQLParsersImpl


/**
 * @author Vadim Bobrov
 */
class MQLParserTest extends FlatSpec with ShouldMatchers {

	val parser = new MQLParsersImpl()

	"query parser" should "parse column list in select" in {
		val mql: String = "select timestamp, value from energy"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
				MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
				MQLFrom(MQLTableEnergy()), _
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "parse string literal in select" in {
		val mql: String = "select \"timestamp\", value from energy"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
			MQLSelect(List(MQLColumnStringLiteral(_), MQLColumnValue())),
			MQLFrom(MQLTableEnergy()), _
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "reject if no customer condition is specified" in {
		val mql: String = "select timestamp, value from energy where location = \"a\" and value > 3.5"
		evaluating {
			val res = parser.parseAll(parser.query, mql)
		} should produce [InvalidMQLException]
	}

	it should "reject if no location condition is specified" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and value > 3.5"
		evaluating {
			val res = parser.parseAll(parser.query, mql)
		} should produce [InvalidMQLException]
	}

	it should "reject if unsupported condition is specified" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and location = \"a\" and timestamp > 3.5"
		evaluating {
			val res = parser.parseAll(parser.query, mql)
		} should produce [UnsupportedConditionException]
	}

	it should "reject if no wireid is specified while not querying rollup" in {
		val mql: String = "select timestamp, value from current where customer = \"a\" and location = \"a\" and value > 3.5"
		evaluating {
			val res = parser.parseAll(parser.query, mql)
		} should produce [InvalidMQLException]
	}

	it should "parse comparison condition" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and location = \"a\" and wireid = \"a\" and value > 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
				MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
				MQLFrom(MQLTableEnergy()),
				Some(MQLWhere(List(
					MQLCustomerCondition(_),
					MQLLocationCondition(_),
					MQLWireIdCondition(_),
					MQLValueCondition(">", _))))
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "parse between and time condition" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and location = \"a\" and wireid = \"a\" and timestamp between '2010-04-20' and '2011-11-14 13:22:45'"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
			MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
			MQLFrom(MQLTableEnergy()),
			Some(MQLWhere(List(
				MQLCustomerCondition(_),
				MQLLocationCondition(_),
				MQLWireIdCondition(_),
				MQLTimeRangeCondition(1271736000000.0, 1321294965000.0))))
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "not allow lower value greater than upper value in between and condition" in {
		val mql: String = "select timestamp, value from energy where customer = \"a\" and location = \"a\" and timestamp between 3.5 and 1"
		evaluating {
			val res = parser.parseAll(parser.query, mql)
		} should produce [AssertionError]
	}

	it should "be case-insensitive" in {
		val mql: String = "Select Timestamp, Value From Energy Where customer = \"a\" and location = \"a\" and wireid = \"a\" and value > 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
			MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
			MQLFrom(MQLTableEnergy()),
			Some(MQLWhere(List(
				MQLCustomerCondition(_),
				MQLLocationCondition(_),
				MQLWireIdCondition(_),
				MQLValueCondition(">", _))))
			), _) =>
			case x => fail(x.toString)
		}
	}

	"MQL parser" should "parse union" in {
		val mql: String = "select timestamp, value from rollup where customer = \"a\" and location = \"a\" and value = 3.5 union select value from energy where customer = \"a\" and location = \"a\" and wireid = \"a\""
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		res match {
			case parser.Success(MQLUnion(
				List(MQLQuery(
					MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
					MQLFrom(MQLTableRollup()),
					Some(MQLWhere(List(
						MQLCustomerCondition(_),
						MQLLocationCondition( _),
						MQLValueCondition("=", _))))
				), _)
			),_) =>
			case x => fail(x.toString)
		}
	}

	//TODO fail parse
	"date parser" should "parse date" in {
		val mql: String = "2012-01-02 16:21:55"
		val res = parser.parseAll(parser.dateTime("yyyy-MM-dd HH:mm:ss"), mql)
		println(res)
	}

}
