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

  "ToLower" should {
    "returns empty string when no values are supplied" in {
      //Given
      val toLower = ToLower()
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "convert string literal to lower case" in {
      //Given
      val toLower = ToLower(Literal("Hello"))
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual "hello"
    }

    "return non-string literals as string" in {
      //Given
      val toLower = ToLower(Literal(1))
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual "1"
    }

    "convert field value to lower case" in {
      //Given
      val toLower = ToLower(Field("name"))
      val document = new BasicBSONObject().append("name", "TEST")

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual "test"
    }

    "Gives empty string if the field value does not exit" in {
      //Given
      val toLower = ToLower(Field("naem"))
      val document = new BasicBSONObject().append("name", "test")

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "Treat literals with null values as empty string" in {
      //Given
      val toLower = ToLower(Literal(null))
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual ""
    }
  }

  "ToUpper" should {
    "returns empty string when no values are supplied" in {
      //Given
      val toUpper = ToUpper()
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "convert string literal to upper case" in {
      //Given
      val toUpper = ToUpper(Literal("Hello"))
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual "HELLO"
    }

    "return non-string literals as string" in {
      //Given
      val toUpper = ToUpper(Literal(1))
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual "1"
    }

    "convert field value to upper case" in {
      //Given
      val toUpper = ToUpper(Field("name"))
      val document = new BasicBSONObject().append("name", "test")

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual "TEST"
    }

    "Gives empty string if the field value does not exit" in {
      //Given
      val toUpper = ToUpper(Field("naem"))
      val document = new BasicBSONObject().append("name", "test")

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "Treat literals with null values as empty string" in {
      //Given
      val toUpper = ToUpper(Literal(null))
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual ""
    }
  }
}
