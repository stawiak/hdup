package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.MQLParser


/**
 * @author Vadim Bobrov
 */
class MQLParserTest extends FlatSpec with ShouldMatchers {

	val parser = new MQLParser()

	"MQL parser" should "parse select * from table" in {
		val mql: String = "select * from rollup"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	it should "parse select columnList from table" in {
		val mql: String = "select timestamp, value from energy"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	it should "parse select columnList from table where col > 3.5" in {
		val mql: String = "select timestamp, value from energy where timestamp > 3.5"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	it should "be case-insensitive" in {
		val mql: String = "Select Timestamp, Value From Energy Where Timestamp > 3.5"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	it should "parse union select columnList from table where col > 3.5 union select columnList from table where col > 3.5" in {
		val mql: String = "select timestamp, value from energy where timestamp > 3.5 union select * from current where value = 1"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	//TODO fail parse

}
