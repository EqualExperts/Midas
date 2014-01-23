package com.ee.midas.dsl.expressions

import org.bson.BSONObject

@FunctionExpression(classOf[Add])
final case class Add(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft(0d) { (sum, expression) =>
      expression.evaluate(document) match {
        case Literal(null) => sum
        case Literal(value) => sum + value.toString.toDouble
      }
    }
    Literal(result)
  }
}

@FunctionExpression(classOf[Multiply])
final case class Multiply(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject) =
    if(expressions.length == 0) {
      Literal(0)
    } else {
      val result = expressions.foldLeft(1d) { (product, expression) =>
        expression.evaluate(document) match {
          case Literal(null) => product
          case Literal(value) => product * value.toString.toDouble
        }
      }
      Literal(result)
    }
}

