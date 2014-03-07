package com.ee.midas.dsl.expressions

import org.bson.BSONObject
import java.text.SimpleDateFormat

@FunctionExpression
final case class Date(format: Expression, value: Expression) extends DateFunction(format, value) {
  def evaluate(document: BSONObject): Literal = {
    val formatAsString = value(format.evaluate(document))
    val dateAsString = value(value.evaluate(document))
//    val formatAsString = value(expressions(0).evaluate(document))
//    val dateAsString = value(expressions(1).evaluate(document))
    Literal(new SimpleDateFormat(formatAsString).parse(dateAsString))
  }
}