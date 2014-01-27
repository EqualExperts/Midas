package com.ee.midas.dsl.expressions

import org.bson.BSONObject
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import com.ee.midas.utils.{Loggable, AnnotationScanner}

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

object Function extends Loggable {

  lazy val functions = new AnnotationScanner("com.ee.midas", classOf[FunctionExpression])
                          .scan
                          .map { className =>
                            log.debug(s"Loading Class $className...")
                            val clazz = Class.forName(className).asInstanceOf[Class[Function]]
                            log.debug(s"Loaded Class $className!")
                            clazz.getSimpleName.toLowerCase -> clazz
                          }.toMap.withDefaultValue(classOf[EmptyFunction])

  def apply(fnName: String, args: Expression*): Function = {
    val fnClazz = functions(fnName.toLowerCase)
    val constructor = fnClazz.getConstructor(classOf[Seq[Expression]])
    log.debug(s"Instantiating Class $fnClazz...")
    constructor.newInstance(args)
  }
}

final case class EmptyFunction(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject): Literal = Literal(null)
}

abstract class ArithmeticFunction(expressions: Expression*) extends Function(expressions: _*) {
  def value(literal: Literal): Double = literal match {
    case Literal(null) => 0
    case Literal(x) =>
      val doubleValue = x.toString.toDouble
      if(doubleValue.isNaN)
        0
      else
        doubleValue
  }
}

abstract class StringFunction(expressions: Expression*) extends Function(expressions: _*) {
  def value(literal: Literal): String = literal match {
    case Literal(null) => ""
    case Literal(x) => x.toString
  }
}
