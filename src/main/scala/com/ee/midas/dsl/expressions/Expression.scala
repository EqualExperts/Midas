package com.ee.midas.dsl.expressions

import org.bson.BSONObject

sealed trait Expression {
  def evaluate(document: BSONObject): Literal
}

final case class Literal(val value: Any) extends Expression {
  def evaluate(document: BSONObject) = Literal(value)
}

import com.ee.midas.transform.DocumentOperations._
final case class Field(name: String) extends Expression {
  def evaluate(document: BSONObject) = document(name) match {
    case Some(value) => Literal(value)
    case None => Literal(null)
  }
}

sealed abstract class Function(expressions: Expression*) extends Expression

abstract class ArithmeticFunction(expressions: Expression*) extends Function(expressions: _*) {
  def value(literal: Literal): Double = literal match {
    case Literal(null) => 0
    case Literal(x) => x.toString.toDouble
  }
}

abstract class StringFunction(expressions: Expression*) extends Function(expressions: _*) {
  def value(literal: Literal): String = literal match {
    case Literal(null) => ""
    case Literal(x) => x.toString
  }
}
