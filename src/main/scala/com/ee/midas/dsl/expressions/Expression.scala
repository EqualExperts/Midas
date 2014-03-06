/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.dsl.expressions

import org.bson.BSONObject
import com.ee.midas.utils.{Loggable, AnnotationScanner}
import scala.util.{Success, Try}

sealed trait Expression {
  def evaluate(document: BSONObject): Literal
}

final case class Literal(val value: Any) extends Expression {
  def evaluate(document: BSONObject) = this

  override def toString = s"${getClass.getSimpleName}($value)"
}

import com.ee.midas.transform.DocumentOperations._
final case class Field(name: String) extends Expression {
  def evaluate(document: BSONObject) = document(name) match {
    case Some(value) => Literal(value)
    case None => Literal(null)
  }

  override def toString = s"${getClass.getSimpleName}($name)"
}

sealed abstract class Function(expressions: Expression*) extends Expression {
  override def toString = s"""${getClass.getSimpleName}(${expressions mkString ", "})"""
}

object Function extends Loggable {

  lazy val functions = new AnnotationScanner("com.ee.midas", classOf[FunctionExpression])
                          .scan
                          .map { className =>
                            logDebug(s"Loading Class $className...")
                            val clazz = Class.forName(className).asInstanceOf[Class[Function]]
                            logDebug(s"Loaded Class $className!")
                            clazz.getSimpleName.toLowerCase -> clazz
                          }
                          .toMap
                          .withDefaultValue(classOf[EmptyFunction])

  def apply(fnName: String, args: Expression*): Function = {
    val fnClazz = functions(fnName.toLowerCase)
    val constructor = fnClazz.getConstructor(classOf[Seq[Expression]])
    logDebug(s"Instantiating Class $fnClazz...")
    constructor.newInstance(args)
  }
}

final case class EmptyFunction(expressions: Expression*) extends Function(expressions: _*) {
  def evaluate(document: BSONObject): Literal = Literal(null)
}

abstract class ArithmeticFunction(expressions: Expression*) extends Function(expressions: _*) {
  def value(literal: Literal): Double = literal match {
    case Literal(null) => 0
    case Literal(x)    => x.toString.toDouble
  }
}

abstract class StringFunction(expressions: Expression*) extends Function(expressions: _*) {
  def value(literal: Literal): String = literal match {
    case Literal(null) => ""
    case Literal(x) => x.toString
  }
}
