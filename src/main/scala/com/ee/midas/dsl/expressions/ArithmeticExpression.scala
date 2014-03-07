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

import org.bson.BSONObject

@FunctionExpression
final case class Add(expressions: Expression*) extends ArithmeticFunction(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft(0d) { (sum, expression) =>
      sum + value(expression.evaluate(document))
    }
    Literal(result)
  }
}

@FunctionExpression
final case class Multiply(expressions: Expression*) extends ArithmeticFunction(expressions: _*) {
  def evaluate(document: BSONObject) =
    if(expressions.length == 0) {
      Literal(0)
    } else {
      val result = expressions.foldLeft(1d) { (product, expression) =>
          product * value(expression.evaluate(document))
      }
      Literal(result)
    }
}

@FunctionExpression
final case class Subtract(minuend: Expression, subtrahend: Expression) extends ArithmeticFunction(minuend, subtrahend) {
  def evaluate(document: BSONObject) = {
        val left = value(minuend.evaluate(document))
        val right = value(subtrahend.evaluate(document))
        Literal(left - right)
  }
}

@FunctionExpression
final case class Divide(dividend: Expression, divisor: Expression) extends ArithmeticFunction(dividend, divisor) {
  def evaluate(document: BSONObject) = {
    val numerator = value(dividend.evaluate(document))
    val denominator = value(divisor.evaluate(document))
    Literal(numerator / denominator)
  }
}

@FunctionExpression
final case class Mod(dividend: Expression, divisor: Expression) extends ArithmeticFunction(dividend, divisor) {
  def evaluate(document: BSONObject) = {
    val numerator = value(dividend.evaluate(document))
    val denominator = value(divisor.evaluate(document))
    Literal(numerator % denominator)
  }
}