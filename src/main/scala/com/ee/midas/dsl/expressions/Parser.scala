package com.ee.midas.dsl.expressions

import scala.util.parsing.combinator.JavaTokenParsers
import com.ee.midas.utils.Memoize

trait Parser extends JavaTokenParsers {

  /**
   * Example:
   * --------
   * """{ $multiply: ["$age", 1, { $concat : ["$fname", ", ", "$lname" ], true },  ] }"""
   *
   * BNF:
   * ----
   * value ::= obj | floatingPointNumber | "null" | "true" | "false" | quotedField | stringLiteralWithoutDotAndDollar.
   * obj ::= "{" function "}".
   * fn ::= fnName ":" fnArgs.
   * fnArgs ::= "[" [values] "]".
   * values ::= value { "," value }.
   */
  def value: Parser[Expression] =
    (
      obj
       | floatingPointNumber              ^^ (s => Literal(s.toDouble))
       | "null"                           ^^ (s => Literal(null))
       | "true"                           ^^ (s => Literal(true))
       | "false"                          ^^ (s => Literal(false))
       | quotedField                      ^^ (Field(_))
       | stringLiteralWithoutDotAndDollar ^^ (s => Literal(s.replace("\"", "")))
    )

  def quotedField = "\"$" ~> """([a-zA-Z_]\w*([\.][a-zA-Z_0-9]\w*)*)""".r <~ "\""
  def stringLiteralWithoutDotAndDollar = ("\"" + """([^"\p{Cntrl}\\\$\.]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "\"").r
  def obj: Parser[Expression] = "{"~> fn <~"}"
  def fnArgs: Parser[List[Expression]] = "["~> repsep(value, ",") <~"]"
  def fnName: Parser[String] = "$"~> """[a-zA-Z_]\w*""".r
  def fn: Parser[Expression] = fnName~":"~fnArgs ^^ {
    case "add"~":"~args      =>  Add(args: _*)
    case "multiply"~":"~args =>  Multiply(args: _*)
    case "concat"~":"~args   =>  Concat(args: _*)
  }

  def parseFresh(input: String): Expression = parseAll(obj, input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  def parse(input: String): Expression = Memoize(parseFresh)(input)
}
