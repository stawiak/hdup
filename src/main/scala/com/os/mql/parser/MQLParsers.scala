package com.os.mql.parser

import util.parsing.combinator._
import com.os.mql._
import model._
import model.MQLColumnStringLiteral

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

	// Clauses
	def select: Parser[MQLSelect] = ("select".ignoreCase~>repsep(selectColumnItem, ",")) ^^ { new MQLSelect(_)	}
	def from: Parser[MQLFrom] = ("from".ignoreCase~>tableName) ^^ {	new MQLFrom(_) }
	def where: Parser[MQLWhere] = "where".ignoreCase~>condition ^^ { new MQLWhere(_) }

	// Columns
	def selectColumnItem: Parser[MQLColumn] = (columnValue | columnTimestamp | columnCustomer | columnLocation | columnWireId | stringLiteral) ^^ {
		case col: MQLColumn => col
		case s: String => MQLColumnStringLiteral(s)
	}

	def columnValue: Parser[MQLColumn] = "value".ignoreCase ^^ { s => MQLColumnValue() }
	def columnTimestamp: Parser[MQLColumn] = "timestamp".ignoreCase ^^ { s => MQLColumnTimestamp() }
	def columnCustomer: Parser[MQLColumn] = "customer".ignoreCase ^^ { s => MQLColumnCustomer() }
	def columnLocation: Parser[MQLColumn] = "location".ignoreCase ^^ { s => MQLColumnLocation() }
	def columnWireId: Parser[MQLColumn] = "wireid".ignoreCase ^^ { s => MQLColumnWireId() }


	// Tables
	def tableName: Parser[MQLTable] = ("energy".ignoreCase | "current".ignoreCase | "vamps".ignoreCase | "interpolated".ignoreCase | "rollup".ignoreCase) ^^ {s => MQLTable(s.toLowerCase)}

	// Conditions
	def condition: Parser[MQLCondition] = (comparisonCondition | betweenCondition | equalCondition)

	def equalCondition: Parser[MQLCondition] = ((columnCustomer | columnLocation | columnWireId)~"="~stringLiteral) ^^ {
		case cn~cmp~s => new MQLComparisonStringCondition(cn, cmp, s)
	}

	def comparisonCondition: Parser[MQLCondition] = ((columnTimestamp | columnValue)~("=" | ">" | "<")~floatingPointNumber) ^^ {
		case cn~cmp~fpn => new MQLComparisonNumberCondition(cn, cmp, fpn.toDouble)
	}

	def betweenCondition: Parser[MQLCondition] = (betweenNumberCondition | betweenTimeCondition)

	def betweenNumberCondition: Parser[MQLCondition] = ((columnValue | columnTimestamp)~"between"~floatingPointNumber~"and"~floatingPointNumber) ^^ {
		case cn~between~fps~and~fpe =>
			assert(fps.toDouble <= fpe.toDouble, "lower BETWEEN AND value less than upper value")
			new MQLBetweenCondition(cn, fps.toDouble, fpe.toDouble)
	}

	def betweenTimeCondition: Parser[MQLCondition] = (columnTimestamp~"between"~timeValue~"and"~timeValue) ^^ {
		case cn~between~ts~and~te =>
			assert(ts.getMillis <= te.getMillis, "lower BETWEEN AND value less than upper value")
			new MQLBetweenCondition(cn, ts.getMillis, te.getMillis)
	}

}

