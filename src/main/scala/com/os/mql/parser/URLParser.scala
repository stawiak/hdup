package com.os.mql.parser

import util.parsing.combinator._
import java.net.URLDecoder.decode

case class InvalidHttpRequestException(msg: String) extends Exception(msg)
case class RequestModel(httpRequestModel: URLModel) {

	if (!httpRequestModel.parameters.contains("from"))
		throw InvalidHttpRequestException("from parameter not defined")
	if (!httpRequestModel.parameters.contains("to"))
		throw InvalidHttpRequestException("to parameter not defined")


	var fromTime:Long = 0
	var toTime:Long = 0
	try {
		fromTime = httpRequestModel.parameters("from").toLong
		toTime = httpRequestModel.parameters("to").toLong
	} catch {
		case _: Throwable =>
			throw InvalidHttpRequestException("to and from must be long integer")
	}

	if (httpRequestModel.pathElements.size < 3 || httpRequestModel.pathElements.size > 4)
		throw InvalidHttpRequestException("incorrect URL")

	val kind = httpRequestModel.pathElements(0)
	val customer = decode(httpRequestModel.pathElements(1), "UTF-8")
	val location = decode(httpRequestModel.pathElements(2), "UTF-8")

	val isRollup = httpRequestModel.pathElements.size == 3
	val wireid = if (isRollup) "" else decode(httpRequestModel.pathElements(3), "UTF-8")

}

case class URLModel(pathElements: Seq[String], parameters: Map[String, String])
case class URLParameter(name: String, value: String)
trait URLParser {
	def parse(url: String): URLModel
}

object URLParser {
	def apply():URLParser = new URLParsersImpl

	class URLParsersImpl extends URLParser with JavaTokenParsers {

		//TODO For a better performance I suggest to use private lazy val instead of private def when defining parsers. Otherwise whenever a parser is references it is created again.
		def parse(urlString: String): URLModel = parseAll(url, urlString).get

		def url: Parser[URLModel] = path~"?"~parameters ^^ {
			case p~"?"~pars =>
				val m = Map.empty[String, String] ++ (pars map (x => (x.name -> x.value)))
				new URLModel(p, m)
		}

		def path: Parser[List[String]] = "/"~>pathElements

		def parameters: Parser[List[URLParameter]] = repsep(parameter, "&")
		def parameter: Parser[URLParameter] = ident~"="~wholeNumber ^^ {
			case n~"="~v => new URLParameter(n,v)
		}

		def pathElements: Parser[List[String]] = repsep(pathElement, "/")
		//TODO: replace ident with any alphanumeric
		def pathElement: Parser[String] = ident

	}
}
