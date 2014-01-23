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

final case class Add(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft(0d) { (sum, expression) =>
       expression.evaluate(document) match {
         case Literal(null) => sum
         case Literal(value) => sum + value.toString.toDouble
//         case inner: Expression => sum + inner.evaluate(document).toString.toDouble
       }
    }
    Literal(result)
  }
}

final case class Multiply(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject) =
    if(expressions.length == 0) {
      Literal(0)
    } else {
      val result = expressions.foldLeft(1d) { (product, expression) =>
        expression.evaluate(document) match {
          case Literal(null) => product
          case Literal(value) => product * value.toString.toDouble
//          case inner: Expression => product * inner.evaluate(document).toString.toDouble
        }
      }
      Literal(result)
    }
}


final case class Concat(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject) = {
    val result = expressions.foldLeft("") { (concatenated, expression) =>
      expression.evaluate(document) match {
        case Literal(null) => concatenated
        case Literal(value) => concatenated + value
//        case inner: Expression => concatenated + inner.evaluate(document)
      }
    }
    Literal(result)
  }
}