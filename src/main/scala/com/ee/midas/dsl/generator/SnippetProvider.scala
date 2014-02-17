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

  //todo: make split more performant by removing Pattern.compile at runtime, use memoization?.
  private def split(splitField: String, regex: String, json: String) : BSONObject => BSONObject = {
     ((document: BSONObject) =>
       document <~> (splitField, Pattern.compile(regex), json))
  }

  private def transform(outputField: String, expressionJson: String) : BSONObject => BSONObject = {
    ((document: BSONObject) => {
        val expression: Expression = parse(expressionJson)
        val literal = expression.evaluate(document)
        document + (outputField, literal.value)
    })
  }
}
