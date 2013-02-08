package com.os.mql.parser

import util.parsing.combinator._
import com.os.mql._
import model._
import model.MQLColumnStringLiteral
import model.MQLComparisonCondition

/**
  * @author Vadim Bobrov
 */
class MQLParsers extends JavaTokenParsers with DateParsers with MathParsers {

	class MyRichString(str: String) {
		def ignoreCase: Parser[String] = ("""(?i)\Q""" + str + """\E""").r
	}

	implicit def pimpString(str: String): MyRichString = new MyRichString(str)

	//TODO customer, location, wireid filters, wireid - if not rollup
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

