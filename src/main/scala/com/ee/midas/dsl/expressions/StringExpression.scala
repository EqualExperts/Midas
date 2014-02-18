package com.ee.midas.dsl.expressions

import org.bson.BSONObject

@FunctionExpression
final case class Concat(expressions: Expression*) extends StringFunction(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft("") { (concatenated, expression) =>
        concatenated + value(expression.evaluate(document))
    }
    Literal(result)
  }
}

@FunctionExpression
final case class ToLower(expressions: Expression*) extends StringFunction(expressions: _*) {
  def evaluate(document: BSONObject) = {
    expressions.length match {
      case 0 => Literal("")
      case _ => val inputString: String = value(expressions(0).evaluate(document))
                val transformedString = inputString.toLowerCase
                Literal(transformedString)
    }
  }
}

@FunctionExpression
final case class ToUpper(expressions: Expression*) extends StringFunction(expressions: _*) {
  def evaluate(document: BSONObject) = {
    expressions.length match {
      case 0 => Literal("")
      case _ => val inputString: String = value(expressions(0).evaluate(document))
        val transformedString = inputString.toUpperCase
        Literal(transformedString)
    }
  }
}
