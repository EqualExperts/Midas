package com.ee.midas.dsl.expressions

import org.bson.BSONObject

trait Expression[+T] {
//  def validate: Unit
  def evaluate(document: BSONObject): T
}

case class FieldExpression(name: String) extends Expression[Object] {
  def evaluate(document: BSONObject) =
    if(document.containsField(name)) document.get(name) else null
}

abstract class Function[T](expressions: Expression[T]*) extends Expression[T]

case class Constant[T](x: T) extends Function[T] {
  def evaluate(document: BSONObject): T = x
}


