package com.ee.midas.dsl.expressions

import org.bson.BSONObject

@FunctionExpression(classOf[Concat])
final case class Concat(expressions: Expression*) extends StringFunction(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft("") { (concatenated, expression) =>
        concatenated + value(expression.evaluate(document))
    }
    Literal(result)
  }
}