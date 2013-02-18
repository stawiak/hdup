package com.os.mql.parser

import util.parsing.combinator._

case class URLModel(pathElements: Seq[String], parameters: Map[String, String])
case class URLParameter(val name: String, val value: String)
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
		def pathElement: Parser[String] = stringLiteral

	}
}
