package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.MQLParser


/**
 * @author Vadim Bobrov
 */
class MQLParserTest extends FlatSpec with ShouldMatchers {

	val parser = new MQLParser()

	"MQL parser" should "successfully parse 'select *'" in {
		val mql: String = "select *"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	"MQL parser" should "successfully parse 'select columnList'" in {
		val mql: String = "select timestamp, value"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

	"MQL parser" should "successfully parse 'select columnList' from table" in {
		val mql: String = "select timestamp, value from energy"
		val res = parser.parseAll(parser.mql, mql)
		println(res)
		//a should be (b)
	}

}
