package com.ee.midas.dsl.expressions

import scala.util.parsing.combinator.JavaTokenParsers

trait Parser extends JavaTokenParsers {

  /**
   * Example:
   * --------
   * """{ $multiply: ["$age", 1, { $concat : ["$fname", ", ", "$lname" ], true },  ] }"""
   *
   *
   * BNF:
   * ----
   * value ::= obj | floatingPointNumber "null" | "true" | "false" | quotedField | stringLiteralWithoutDotAndDollar.
   * obj ::= "{" function "}".
   * function ::= functionName ":" args.
   * args ::= "[" [values] "]".
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
  def obj: Parser[Expression] = "{"~> function <~"}"
  def fnArgs: Parser[List[Expression]] = "["~> repsep(value, ",") <~"]"
  def fnName: Parser[String] = "$"~> """[a-zA-Z_]\w*""".r
  def function: Parser[Expression] = fnName~":"~fnArgs ^^ {
    case "add"~":"~args      =>  Add(args: _*)
    case "multiply"~":"~args =>  Multiply(args: _*)
    case "concat"~":"~args   =>  Concat(args: _*)
  }
}
