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

package com.ee.midas.dsl.generator

import com.ee.midas.dsl.grammar.Verb
import org.bson.BSONObject
import com.mongodb.util.JSON
import com.ee.midas.transform.DocumentOperations._
import java.util.regex.Pattern
import com.ee.midas.dsl.expressions.{Parser, Expression}
import com.ee.midas.utils.Loggable

trait SnippetProvider extends Parser with Loggable {
   def toSnippet(verb: Verb, args: Array[String]): BSONObject => BSONObject = verb match {
     case Verb.add       => add(args(0))
     case Verb.remove    => remove(args(0))
     case Verb.copy      => copy(args(0), args(1))
     case Verb.split     => split(args(0), args(1), args(2))
     case Verb.merge     => merge(args(0), args(1), args(2))
     case Verb.transform => transform(args(0), args(1))
   }

  private def add(json: String): BSONObject => BSONObject = {
    ((document: BSONObject) => {
      val fields = JSON.parse(json).asInstanceOf[BSONObject]
      document ++ (fields, false)
    })
  }

  private def remove(json: String) : BSONObject => BSONObject = {
    ((document: BSONObject) => {
        val fields = JSON.parse(json).asInstanceOf[BSONObject]
        document -- fields
    })
  }

  private def copy(fromField: String, toField: String): BSONObject => BSONObject = {
    ((document: BSONObject) => {
        document(fromField) match {
            case Some(fromFieldValue) => document(toField) = fromFieldValue
            case None => document
        }
    })
  }

  private def merge(fieldsArray: String, separator: String, mergeField: String) : BSONObject => BSONObject = {
    val fields = fieldsArray.substring(1, fieldsArray.length() - 1)
    val fieldList = fields.split(",").map(field => {field.trim.replaceAll("\"", "")}).toList
    ((document: BSONObject) => {
        document >~< (mergeField, separator, fieldList)
    })
  }

  private def split(splitField: String, regex: String, json: String) : BSONObject => BSONObject = {
    val documentWithSplitFields = JSON.parse(json).asInstanceOf[BSONObject]
    val compiledRegex = Pattern.compile(regex)
    ((document: BSONObject) => {
      try {
        document <~> (splitField, compiledRegex, json)
      } catch {
        case t: Throwable =>
          val errMsg = if(t.getMessage == null) s"Cannot parse $regex" else t.getMessage
          documentWithSplitFields.keySet.toArray.foreach { case key: String =>
            document + (s"${key}.errmsg", s"exception: $errMsg")
          }
          document
      }
    })
  }

  private def transform(outputField: String, expressionJson: String) : BSONObject => BSONObject = {
    ((document: BSONObject) => {
      val expression: Expression = parse(expressionJson)
      try {
        val literal = expression.evaluate(document)
        document + (outputField, literal.value)
      } catch {
        case t: Throwable =>
          document + (s"${outputField}.errmsg", s"exception: ${t.getMessage}")
      }
    })
  }
}
