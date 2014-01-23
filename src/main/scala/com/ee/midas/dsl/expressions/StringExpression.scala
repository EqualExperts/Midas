package com.ee.midas.dsl.expressions

import org.bson.BSONObject

final case class Concat(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft("") { (concatenated, expression) =>
      expression.evaluate(document) match {
        case Literal(null) => concatenated
        case Literal(value) => concatenated + value
      }
    }
    Literal(result)
  }
}