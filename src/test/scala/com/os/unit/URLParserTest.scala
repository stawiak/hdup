package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.parser.URLParser.URLParsersImpl
import com.os.mql.parser.{URLParameter, URLModel}


/**
 * @author Vadim Bobrov
 */
class URLParserTest extends FlatSpec with ShouldMatchers {

	val parser = new URLParsersImpl()

	"URL parser" should "parse path elements" in {
		val url = "/vamps/customer0/location0"
		val res = parser.parse(parser.pathElements, url)
		println(res.get.mkString(","))
		//println(res)
		/*
				res match {
					case parser.Success(
						URLModel(List("vamps","customer0","location0","wireid2"), _)
					, _) =>
					case x => fail(x.toString)
				}
		*/
	}

	it should "parse single parameter" in {
		val url = "a=1"
		val res = parser.parse(parser.parameter, url)
		res match {
			case parser.Success(
				URLParameter("a", "1")
			, _) =>
			case x => fail(x.toString)
		}
	}

	it should "parse multiple parameters" in {
		val url = "a=1&b=2"
		val res = parser.parse(parser.parameters, url)
		res match {
			case parser.Success(
			List(URLParameter("a", "1"), URLParameter("b", "2"))
			, _) =>
			case x => fail(x.toString)
		}
	}

	it should "parse path elements and parameters" in {
		//val url = "/vamps/customer0/location0/wireid2?from=0&to=1460693438444"
		val url = "/a?b=c"
		val res = parser.parseAll(parser.url, url)
		println(res)
/*
		res match {
			case parser.Success(
				URLModel(List("vamps","customer0","location0","wireid2"), _)
			, _) =>
			case x => fail(x.toString)
		}
*/
	}

}
