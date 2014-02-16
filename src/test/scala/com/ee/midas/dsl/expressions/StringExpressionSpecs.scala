package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class StringExpressionSpecs extends Specification {
  "Concat" should {
    "returns empty string when no values are supplied" in {
      //Given
      val concat = Concat()
      val document = new BasicBSONObject()

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "concatenate string literals" in {
      //Given
      val concat = Concat(Literal("Hello"), Literal("World"))
      val document = new BasicBSONObject()

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "HelloWorld"
    }

    "concatenate non-string literals" in {
      //Given
      val concat = Concat(Literal(1), Literal(true), Literal(3.5), Literal(null))
      val document = new BasicBSONObject()

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "1true3.5"
    }

    "concatenate literals and field values" in {
      //Given
      val concat = Concat(Literal("Mr. "), Field("name"))
      val document = new BasicBSONObject().append("name", "Test")

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "Mr. Test"
    }

    "Treat literals with null values as empty string" in {
      //Given
      val concat = Concat(Literal("Mr. "), Field("name"), Field("surname"))
      val document = new BasicBSONObject().append("name", "Test")

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "Mr. Test"
    }
  }
}
