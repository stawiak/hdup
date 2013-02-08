package com.os.mql.parser

import util.parsing.combinator.RegexParsers
import org.joda.time.{MutableDateTime, DateTime}
import org.joda.time.format.DateTimeFormat

/**
 * @author Vadim Bobrov
 */
trait DateParsers extends RegexParsers {

	def toSeconds: Parser[DateTime] = dateTime("yyyy-MM-dd HH:mm:ss")
	def toDay: Parser[DateTime] = dateTime("yyyy-MM-dd")

	def dateTime(pattern: String): Parser[DateTime] = new Parser[DateTime] {
		val dateFormat = DateTimeFormat.forPattern(pattern)

		def jodaParse(text: CharSequence, offset: Int) = {
			val mutableDateTime = new MutableDateTime(0, 1, 1, 0, 0, 0, 0)
			val upper = offset + dateFormat.getParser.estimateParsedLength
			val maxInput = text.subSequence(offset, if(upper > text.length) text.length() else upper).toString
			val newPos = dateFormat.parseInto(mutableDateTime, maxInput, 0)
			(mutableDateTime.toDateTime, newPos + offset)
		}

		def apply(in: Input) = {
			val source = in.source
			val offset = in.offset
			val start = handleWhiteSpace(source, offset)
			val (dateTime, endPos) = jodaParse(source, start)
			if (endPos >= 0)
				Success(dateTime, in.drop(endPos - offset))
			else
				Failure("Failed to parse date", in.drop(start - offset))
		}
	}
}
