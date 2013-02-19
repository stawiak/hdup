package com.os.mql.parser

import com.os.mql.model._
import com.os.mql.model.MQLSelect
import com.os.mql.model.MQLColumnLocation
import com.os.mql.model.MQLColumnTimestamp
import com.os.mql.model.MQLColumnValue
import com.os.mql.model.MQLQuery
import com.os.mql.model.MQLColumnStringLiteral
import com.os.mql.model.MQLColumnCustomer
import com.os.mql.model.MQLColumnWireId
import com.os.mql.model.MQLFrom
import com.os.mql.model.MQLWhere
import com.os.mql.model.MQLUnion
import util.parsing.combinator._
import com.os.mql.executor.{MQLExecutor, MQLCommand}
import com.os.parser.{MathParsers, DateParsers}

/**
 * @author Vadim Bobrov
 */
trait MQLParser {
	def parse(mql: String): Traversable[MQLCommand]
}

object MQLParser {
	def apply():MQLParser = new MQLParsersImpl

	class MQLParsersImpl extends MQLParser with JavaTokenParsers with DateParsers with MathParsers {

		class MyRichString(str: String) {
			def ignoreCase: Parser[String] = ("""(?i)\Q""" + str + """\E""").r

			def stripQuotes: String = { str.substring(1, str.length - 1) }
		}

		implicit def pimpString(str: String): MyRichString = new MyRichString(str)

		//TODO customer, location, wireid filters, wireid - if not rollup
		//TODO date parsing

		//TODO order by
		//TODO where - multiple conditions OR-ed or AND-ed
		//TODO number literals in select
		//TODO expr in select and where

		//TODO For a better performance I suggest to use private lazy val instead of private def when defining parsers. Otherwise whenever a parser is references it is created again.
		def parse(queryString: String): Traversable[MQLCommand] = {
			val query = parseAll(mql, queryString)
			new MQLExecutor(query.get).generateExecutePlan
		}

		def mql: Parser[MQLUnion] = repsep(query, "union") ^^ {
			case queries: List[MQLQuery] => new MQLUnion(queries)
		}

		def query: Parser[MQLQuery] = select~from~opt(where) ^^ {
			case s~f~w => new MQLQuery(s, f, w)
		}

		// Clauses
		def select: Parser[MQLSelect] = ("select".ignoreCase~>repsep(selectColumnItem, ",")) ^^ { new MQLSelect(_)	}
		def from: Parser[MQLFrom] = ("from".ignoreCase~>tableName) ^^ {	new MQLFrom(_) }
		def where: Parser[MQLWhere] = "where".ignoreCase~>conditions ^^ { new MQLWhere(_) }

		// Columns
		def selectColumnItem: Parser[MQLColumn] = (columnValue | columnTimestamp | columnCustomer | columnLocation | columnWireId | stringLiteral) ^^ {
			case col: MQLColumn => col
			case s: String => MQLColumnStringLiteral(s)
		}

		def columnValue: Parser[MQLColumn] = "value".ignoreCase ^^ { s => MQLColumnValue }
		def columnTimestamp: Parser[MQLColumn] = "timestamp".ignoreCase ^^ { s => MQLColumnTimestamp }
		def columnCustomer: Parser[MQLColumn] = "customer".ignoreCase ^^ { s => MQLColumnCustomer }
		def columnLocation: Parser[MQLColumn] = "location".ignoreCase ^^ { s => MQLColumnLocation }
		def columnWireId: Parser[MQLColumn] = "wireid".ignoreCase ^^ { s => MQLColumnWireId }


		// Tables
		def tableName: Parser[MQLTable] = ("energy".ignoreCase | "current".ignoreCase | "vamps".ignoreCase | "interpolated".ignoreCase | "rollup".ignoreCase) ^^ {s => MQLTable(s.toLowerCase)}

		// Conditions
		def conditions: Parser[List[MQLCondition]] = repsep(condition, "and".ignoreCase)
		def condition: Parser[MQLCondition] = (comparisonCondition | betweenCondition | equalCondition)

		def equalCondition: Parser[MQLCondition] = ((columnCustomer | columnLocation | columnWireId)~"="~stringLiteral) ^^ {
			case cn~cmp~s => MQLCondition(cn, cmp, s.stripQuotes)
		}

		def comparisonCondition: Parser[MQLCondition] = ((columnTimestamp | columnValue)~("=" | ">" | "<")~floatingPointNumber) ^^ {
			case cn~cmp~fpn => MQLCondition(cn, cmp, fpn.toDouble)
		}

		def betweenCondition: Parser[MQLCondition] = (betweenNumberCondition | betweenTimeCondition)

		def betweenNumberCondition: Parser[MQLCondition] = ((columnValue | columnTimestamp)~"between"~floatingPointNumber~"and"~floatingPointNumber) ^^ {
			case cn~between~fps~and~fpe =>
				assert(fps.toDouble <= fpe.toDouble, "lower BETWEEN AND value less than upper value")
				MQLCondition(cn, fps.toDouble, fpe.toDouble)
		}

		def betweenTimeCondition: Parser[MQLCondition] = (columnTimestamp~"between"~timeValue~"and"~timeValue) ^^ {
			case cn~between~ts~and~te =>
				assert(ts.getMillis <= te.getMillis, "lower BETWEEN AND value less than upper value")
				MQLCondition(cn, ts.getMillis, te.getMillis)
		}


	}


}
