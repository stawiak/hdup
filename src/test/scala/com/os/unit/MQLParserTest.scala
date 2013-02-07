package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql._
import com.os.mql.MQLColumnAll
import com.os.mql.MQLFrom
import com.os.mql.MQLSelect
import com.os.mql.MQLQuery
import com.os.mql.MQLTableEnergy
import com.os.mql.MQLTableRollup


/**
 * @author Vadim Bobrov
 */
class MQLParserTest extends FlatSpec with ShouldMatchers {

	val parser = new MQLParser()

	"query parser" should "parse * in select" in {
		val mql: String = "select * from rollup"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
				MQLSelect(List(MQLColumnAll())),
				MQLFrom(MQLTableRollup()), _
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "parse column list in select" in {
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

	it should "parse comparison condition" in {
		val mql: String = "select timestamp, value from energy where timestamp > 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
				MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
				MQLFrom(MQLTableEnergy()),
				Some(MQLWhere(MQLComparisonCondition(MQLColumnTimestamp(), ">", _)))
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "parse between and condition" in {
		val mql: String = "select timestamp, value from energy where timestamp between 1 and 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
			MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
			MQLFrom(MQLTableEnergy()),
			Some(MQLWhere(MQLBetweenCondition(MQLColumnTimestamp(), 1, 3.5)))
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "not allow lower value greater than upper value in between and condition" in {
		val mql: String = "select timestamp, value from energy where timestamp between 3.5 and 1"
		evaluating {
			val res = parser.parseAll(parser.query, mql)
		} should produce [AssertionError]
	}

	it should "be case-insensitive" in {
		val mql: String = "Select Timestamp, Value From Energy Where Timestamp > 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
			MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
			MQLFrom(MQLTableEnergy()),
			Some(MQLWhere(MQLComparisonCondition(MQLColumnTimestamp(), ">", _)))
			), _) =>
			case x => fail(x.toString)
		}
	}

	"MQL parser" should "parse union" in {
		val mql: String = "select timestamp, value from rollup where timestamp = 3.5 union select * from energy"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		res match {
			case parser.Success(MQLUnion(
				List(MQLQuery(
					MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
					MQLFrom(MQLTableRollup()),
					Some(MQLWhere(MQLComparisonCondition(MQLColumnTimestamp(), "=", _)))
				), _)
			),_) =>
			case x => fail(x.toString)
		}
	}

	//TODO fail parse

}
