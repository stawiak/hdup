package com.os.mql

import util.parsing.combinator._

/**
  * @author Vadim Bobrov
  */
class MQLParser extends JavaTokenParsers {

	//TODO keywords and column, table names case insensitive
	//TODO multiple conditions
	def mql: Parser[Any] = select~from~where
	def select: Parser[Any] = "select"~columnList
	def from: Parser[Any] = "from"~tableName
	def where: Parser[Any] = "where"~condition

	def columnList: Parser[Any] = "*" | repsep(columnName, ",")
	def columnName: Parser[Any] = "value" | "timestamp"

	def tableName: Parser[Any] = "energy" | "current" | "vamps"

	def condition: Parser[Any] = columnName~("=" | ">" | "<")~expr


	// arithmetic
	def expr: Parser[Any] = term~rep("+"~term |"-"~term)
	def term: Parser[Any] = factor~rep("*"~factor |"/"~factor)
	def factor: Parser[Any] = floatingPointNumber | "("~expr~")"

 }
