package com.os.mql

import util.parsing.combinator._

/**
  * @author Vadim Bobrov
 */
class MQLParser extends JavaTokenParsers {

	class MyRichString(str: String) {
		def ignoreCase: Parser[String] = ("""(?i)\Q""" + str + """\E""").r
	}

	implicit def pimpString(str: String): MyRichString = new MyRichString(str)

	//TODO order by
	//TODO where - multiple conditions OR-ed or AND-ed
	//TODO between-and
	//TODO "string" and number literals in select
	//TODO expr in select and where
	def mql: Parser[MQLUnion] = repsep(query, "union") ^^ {
		case queries: List[MQLQuery] => new MQLUnion(queries)
	}

	def query: Parser[MQLQuery] = select~from~opt(where) ^^ {
		case s~f~w => new MQLQuery(s, f, w)
	}

	def select: Parser[MQLSelect] = ("select".ignoreCase~>columnList) ^^ { new MQLSelect(_)	}

	def from: Parser[MQLFrom] = ("from".ignoreCase~>tableName) ^^ {	new MQLFrom(_) }

	def where: Parser[MQLWhere] = "where".ignoreCase~>condition ^^ { new MQLWhere(_) }

	def columnList: Parser[List[MQLColumn]] = ("*" | repsep(columnName, ",")) ^^ {
		case "*" =>
			List[MQLColumn](MQLColumnAll())
		case cols: List[MQLColumn] =>
		     cols
	}

	def columnName: Parser[MQLColumn] = ("value".ignoreCase | "timestamp".ignoreCase) ^^ {s => MQLColumn(s.toLowerCase)}

	def tableName: Parser[MQLTable] = ("energy".ignoreCase | "current".ignoreCase | "vamps".ignoreCase | "interpolated".ignoreCase | "rollup".ignoreCase) ^^ {s => MQLTable(s.toLowerCase)}

	def condition: Parser[MQLCondition] = (columnName~("=" | ">" | "<")~floatingPointNumber) ^^ {
		case cn~cmp~fpn => new MQLCondition(cn, cmp, fpn.toDouble)
	}


	// arithmetic
	def expr: Parser[Any] = term~rep("+"~term |"-"~term)
	def term: Parser[Any] = factor~rep("*"~factor |"/"~factor)
	def factor: Parser[Any] = floatingPointNumber | "("~expr~")"

}
