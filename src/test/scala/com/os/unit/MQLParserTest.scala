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

	"query parser" should "parse select * from table" in {
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

	it should "parse select columnList from table" in {
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

	it should "parse select columnList from table where col > 3.5" in {
		val mql: String = "select timestamp, value from energy where timestamp > 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
				MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
				MQLFrom(MQLTableEnergy()),
				Some(MQLWhere(MQLCondition(MQLColumnTimestamp(), ">", _)))
			), _) =>
			case x => fail(x.toString)
		}
	}

	it should "be case-insensitive" in {
		val mql: String = "Select Timestamp, Value From Energy Where Timestamp > 3.5"
		val res = parser.parseAll(parser.query, mql)
		println(res)
		res match {
			case parser.Success(MQLQuery(
			MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
			MQLFrom(MQLTableEnergy()),
			Some(MQLWhere(MQLCondition(MQLColumnTimestamp(), ">", _)))
			), _) =>
			case x => fail(x.toString)
		}
	}

	"MQL parser" should "parse union select columnList from table where col = 3.5 union select * from table" in {
		val mql: String = "select timestamp, value from rollup where timestamp = 3.5 union select * from energy"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		res match {
			case parser.Success(MQLUnion(
				List(MQLQuery(
					MQLSelect(List(MQLColumnTimestamp(), MQLColumnValue())),
					MQLFrom(MQLTableRollup()),
					Some(MQLWhere(MQLCondition(MQLColumnTimestamp(), "=", _)))
				), _)
			),_) =>
			case x => fail(x.toString)
		}
	}

	//TODO fail parse

}
