/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.dsl.expressions

import scala.util.parsing.combinator.JavaTokenParsers
import com.ee.midas.utils.Memoize

trait Parser extends JavaTokenParsers {

  /**
   * Example:
   * --------
   * """{ $multiply: ["$age", 1, { $concat : ["$fname", ", ", "$lname" ], true },  ] }"""
   * """{ $multiply: ['$age', 1, { $concat : ['$fname', ', ', '$lname' ], true },  ] }"""
   *
   * BNF:
   * ----
   * value ::= obj | floatingPointNumber | "null" | "true" | "false" | quotedField | singleQuotedField | quotedStringLiteral | singleQuotedStringLiteral.
   * obj ::= "{" fn "}".
   * fn ::= fnName ":" fnArgs.
   * fnArgs ::= "[" values "]" | value.
   * values ::= value { "," value }.
   */
  def value: Parser[Expression] =
    (
      obj
       | floatingPointNumber        ^^ (s => Literal(s.toDouble))
       | "null"                     ^^ (s => Literal(null))
       | "true"                     ^^ (s => Literal(true))
       | "false"                    ^^ (s => Literal(false))
       | quotedField
       | singleQuotedField
       | quotedStringLiteral
       | singleQuotedStringLiteral
    )

  def field: Parser[String] = """([a-zA-Z_]\w*([\.][a-zA-Z_0-9]\w*)*)""".r
  def quotedField: Parser[Expression]       = "\"$" ~> field <~ "\"" ^^ (Field(_))
  def singleQuotedField: Parser[Expression] = "\'$" ~> field <~ "\'" ^^ (Field(_))

//  val stringWi  thoutDotAndDollar: String =
//    """([^"\p{Cntrl}\\\$\.]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*"""
  def quotedStringLiteral: Parser[Expression] =
    ("\"" + """([^"\p{Cntrl}\\\$\.]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "\"").r ^^ (s => Literal(s.replaceAllLiterally("\"", "")))
  def singleQuotedStringLiteral: Parser[Expression] =
    ("\'" + """([^'\p{Cntrl}\\\$\.]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "\'").r ^^ (s => Literal(s.replaceAllLiterally("\'", "")))

  def obj: Parser[Expression] = "{"~> fn <~"}"
  def fnArgs: Parser[List[Expression]] = "["~> repsep(value, ",") <~"]"  | (value ^^ (List(_)))
  def fnName: Parser[String] = "$"~> """[a-zA-Z_]\w*""".r
  def fn: Parser[Expression] = fnName~":"~fnArgs ^^ { case name~":"~args => Function(name, args: _*) }

  def parseFresh(input: String): Expression = parseAll(obj, input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  def parse(input: String): Expression = Memoize(parseFresh)(input)
}
