package com.ee.midas.transform.expressions

import org.bson.BSONObject

case class Add[T: Numeric](expressions: Expression[T]*) extends Function[T](expressions: _*) {
  def evaluate(document: BSONObject) = expressions.map(_.evaluate(document)).sum
}

case class Multiply[T: Numeric](expressions: Expression[T]*) extends Function[T](expressions: _*) {
  def evaluate(document: BSONObject) =
    if(expressions.length == 0) {
      implicitly[Numeric[T]].zero
    } else {
      expressions.map(_.evaluate(document)).product
    }
}