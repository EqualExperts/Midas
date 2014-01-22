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
      val fieldExpression = Field("zip")
      val zipValue = 4000058
      val document = new BasicBSONObject("zip", zipValue)

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(zipValue)
    }

    "returns null if field is not present in the document" in {
      //Given
      val fieldExpression = Field("pin")
      val document = new BasicBSONObject()

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(null)
    }

    "give the value of a 2-level nested field" in {
      //Given
      val fieldExpression = Field("address.zip")
      val zipValue = 4000058
      val document = new BasicBSONObject("address", new BasicBSONObject("zip", zipValue))

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(zipValue)
    }

    "give the value of a 3-level nested field" in {
      //Given
      val fieldExpression = Field("address.line.1")
      val value = "Some Street"
      val document = new BasicBSONObject("address", 
        new BasicBSONObject("line", new BasicBSONObject("1", value)))

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(value)
    }
  }
}