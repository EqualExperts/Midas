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

abstract class Function(expressions: Expression*) extends Expression

