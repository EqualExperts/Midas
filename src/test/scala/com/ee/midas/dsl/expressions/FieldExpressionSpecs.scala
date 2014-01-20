package com.ee.midas.dsl.expressions

import org.specs2.mutable.Specification

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject

@RunWith(classOf[JUnitRunner])
class FieldExpressionSpecs extends Specification {
  "Field Expression" should {
    "give the value of a field if present in the document" in {
      //Given
      val fieldExpression = FieldExpression("zip")
      val zipValue = 4000058
      val document = new BasicBSONObject("zip", zipValue)

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual zipValue
    }

    "returns null if field is not present in the document" in {
      //Given
      val fieldExpression = FieldExpression("pin")
      val document = new BasicBSONObject()

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual null
    }

    //todo: specs for nested fields
  }
}