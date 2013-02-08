package com.os.mql

import util.parsing.combinator._
import org.joda.time.{DateTime, MutableDateTime}
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}

/**
  * @author Vadim Bobrov
 */
class MQLParsers extends JavaTokenParsers with DateParsers with ArithmeticParsers {

	class MyRichString(str: String) {
		def ignoreCase: Parser[String] = ("""(?i)\Q""" + str + """\E""").r
	}

	implicit def pimpString(str: String): MyRichString = new MyRichString(str)

	//TODO date parsing

	//TODO order by
	//TODO where - multiple conditions OR-ed or AND-ed
	//TODO number literals in select
	//TODO expr in select and where
	def mql: Parser[MQLUnion] = repsep(query, "union") ^^ {
		case queries: List[MQLQuery] => new MQLUnion(queries)
	}

	def query: Parser[MQLQuery] = select~from~opt(where) ^^ {
		case s~f~w => new MQLQuery(s, f, w)
	}

	def select: Parser[MQLSelect] = ("select".ignoreCase~>repsep(columnItem, ",")) ^^ { new MQLSelect(_)	}

	def from: Parser[MQLFrom] = ("from".ignoreCase~>tableName) ^^ {	new MQLFrom(_) }

	def where: Parser[MQLWhere] = "where".ignoreCase~>condition ^^ { new MQLWhere(_) }

	def columnItem: Parser[MQLColumn] = (columnName | stringLiteral) ^^ {
		case col: MQLColumn => col
		case s: String => MQLColumnStringLiteral(s)
	}

	def columnName: Parser[MQLColumn] = ("value".ignoreCase | "timestamp".ignoreCase) ^^ { case s: String => MQLColumn(s.toLowerCase) }

	def tableName: Parser[MQLTable] = ("energy".ignoreCase | "current".ignoreCase | "vamps".ignoreCase | "interpolated".ignoreCase | "rollup".ignoreCase) ^^ {s => MQLTable(s.toLowerCase)}

	def condition: Parser[MQLCondition] = (comparisonCondition | betweenCondition)

	def comparisonCondition: Parser[MQLCondition] = (columnName~("=" | ">" | "<")~floatingPointNumber) ^^ {
		case cn~cmp~fpn => new MQLComparisonCondition(cn, cmp, fpn.toDouble)
	}

	def betweenCondition: Parser[MQLCondition] = (columnName~"between"~floatingPointNumber~"and"~floatingPointNumber) ^^ {
		case cn~between~fps~and~fpe =>
			assert(fps.toDouble <= fpe.toDouble, "lower BETWEEN AND value less than upper value")
			new MQLBetweenCondition(cn, fps.toDouble, fpe.toDouble)
	}

}
trait ArithmeticParsers extends JavaTokenParsers {

	def expr: Parser[Any] = term~rep("+"~term |"-"~term)
	def term: Parser[Any] = factor~rep("*"~factor |"/"~factor)
	def factor: Parser[Any] = floatingPointNumber | "("~expr~")"

}

trait DateParsers extends RegexParsers {
	def dateTime(pattern: String): Parser[DateTime] = new Parser[DateTime] {
		val dateFormat = DateTimeFormat.forPattern(pattern)

		def jodaParse(text: CharSequence, offset: Int) = {
			val mutableDateTime = new MutableDateTime
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
