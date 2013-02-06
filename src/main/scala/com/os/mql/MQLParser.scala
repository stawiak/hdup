package com.os.mql

import util.parsing.combinator._

/**
  * @author Vadim Bobrov
  */
class MQLParser extends JavaTokenParsers {

	//TODO union
	//TODO keywords and column, table names case insensitive
	//TODO multiple conditions
	//TODO make where optional
	//TODO order by
	def mql: Parser[MQLQuery] = select ^^ {case s => new MQLQuery(s)}

	def select: Parser[MQLSelect] = ("select"~columnList) ^^ {
		case "select"~columnList =>
			new MQLSelect(columnList)
	}

	def from: Parser[Any] = "from"~tableName
	def where: Parser[Any] = "where"~condition

	def columnList: Parser[List[MQLColumn]] = ("*" | repsep(columnName, ",")) ^^ {
		case "*" =>
			List[MQLColumn](MQLColumnAll())
		//case ll: List =>
		//	List[MQLColumn]() ++ ll
	}

	def columnName: Parser[MQLColumn] = ("value" | "timestamp") ^^ {s => MQLColumn(s)}

	def tableName: Parser[MQLTable] = ("energy" | "current" | "vamps") ^^ {s => MQLTable(s)}

	def condition: Parser[Any] = columnName~("=" | ">" | "<")~expr


	// arithmetic
	def expr: Parser[Any] = term~rep("+"~term |"-"~term)
	def term: Parser[Any] = factor~rep("*"~factor |"/"~factor)
	def factor: Parser[Any] = floatingPointNumber | "("~expr~")"

}
