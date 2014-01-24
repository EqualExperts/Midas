package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class ArithmeticExpressionSpecs extends Specification {
  "Add" should {
    "return 0 when no arguments are supplied" in {
      //Given
      val add = Add()
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "return sum of arguments supplied for Int" in {
      //Given
      val add = Add(Literal(1), Literal(3))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 4
    }

    "return sum of homogenous arg types" in {
      //Given
      val add = Add(Literal(1d), Literal(3.5))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 4.5
    }

    "return 0 for sum of nulls" in {
      //Given
      val add = Add(Literal(null))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 0
    }


    "return sum of field value and literal" in {
      //Given
      val add = Add(Literal(1d), Field("age"))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 3.0
    }

    "return literal value as sum when field does not exist" in {
      //Given
      val add = Add(Field("$age"), Literal(1d))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 1.0
    }

  }

  "Multiply" should {
    "return 0 when no arguments are supplied" in {
      //Given
      val multiply = Multiply()
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "return argument value when 1 argument is supplied" in {
      //Given
      val value = 3.5
      val multiply = Multiply(Literal(value))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(value)
    }

    "return product of homogeneous args type" in {
      //Given
      val multiply = Multiply(Literal(.5), Literal(2d))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(1.0)
    }

    "return product of field value and literal" in {
      //Given
      val multiply = Multiply(Literal(1d), Field("age"))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = multiply.evaluate(document).value

      //Then
      result mustEqual 2.0
    }

    "return product as 0 when field does not exist" in {
      //Given
      val multiply = Multiply(Field("age"), Literal(5))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document).value

      //Then
      result mustEqual 0
    }
  }

  "Subtract" should {
    "return 0 when no arguments are supplied" in {
      //Given
      val subtract = Subtract()
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "return 0 when 1 argument is supplied" in {
      //Given
      val subtract = Subtract(Literal(0))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "return difference when minuend and subtrahend are supplied" in {
      //Given
      val subtract = Subtract(Literal(1d), Literal(3.5))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual -2.5
    }

    "return difference of first two arguments when more than 2 are supplied ignoring the rest" in {
      //Given
      val subtract = Subtract(Literal(1d), Literal(3.5), Literal(4), Literal(-3.2))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual -2.5
    }

    "return difference between of field value and literal" in {
      //Given
      val subtract = Subtract(Field("age"), Literal(1d))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual 1.0
    }

    "return difference when field does not exist" in {
      //Given
      val subtract = Subtract(Field("age"), Literal(5))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual -5
    }
  }
}
