package com.os.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.os.mql.parser.URLParser.URLParsersImpl
import com.os.mql.parser.{RequestModel, URLParameter, URLModel}



/**
 * @author Vadim Bobrov
 */
class URLParserTest extends FlatSpec with ShouldMatchers {

	val parser = new URLParsersImpl()

	"URL parser" should "parse path elements" in {
		val url = "vamps/customer0/location0/wireid2"
		val res = parser.parse(parser.pathElements, url)
		res match {
			case parser.Success(
				List("vamps","customer0","location0","wireid2")
			, _) =>
			case x => fail(x.toString)
		}
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

	it should "parse full URL" in {
		val url = "/vamps/customer0/location0/wireid2?from=0&to=1460693438444"
		val res = parser.parseAll(parser.url, url)

		res match {
			case parser.Success(
				URLModel(List("vamps","customer0","location0","wireid2"), params)
			, _) =>
				params("from") should be ("0")
				params("to") should be ("1460693438444")
				params should have size (2)

			case x => fail(x.toString)
		}
	}

	it should "translate URL into data request for measurement request" in {
		val url = "/vamps/customer0/location0/wireid2?from=0&to=1460693438444"
		val res = parser.parseAll(parser.url, url)

		res match {
			case parser.Success(urlModel @ URLModel(_, _), _) =>
				val requestModel = new RequestModel(urlModel)
				requestModel.fromTime should be (0L)
				requestModel.toTime should be (1460693438444L)
				requestModel.kind should be ("vamps")
				requestModel.customer should be ("customer0")
				requestModel.location should be ("location0")
				requestModel.wireid should be ("wireid2")
				requestModel.isRollup should be (false)

			case x => fail(x.toString)
		}
	}

}
