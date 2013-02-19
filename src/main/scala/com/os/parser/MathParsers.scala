package com.os.parser

import util.parsing.combinator.JavaTokenParsers

/**
 * @author Vadim Bobrov
 */
trait MathParsers extends JavaTokenParsers {

		def expr: Parser[Any] = term~rep("+"~term |"-"~term)
		def term: Parser[Any] = factor~rep("*"~factor |"/"~factor)
		def factor: Parser[Any] = floatingPointNumber | "("~expr~")"

}
