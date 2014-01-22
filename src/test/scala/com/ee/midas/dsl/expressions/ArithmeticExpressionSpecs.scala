package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class ArithmeticExpressionSpecs extends Specification {
  "Add" should {
    "returns 0 when no arguments are supplied" in {
      //Given
      val add = Add()
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "returns sum of arguments supplied for Int" in {
      //Given
      val add = Add(Literal(1), Literal(3))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 4
    }

    "returns sum of homogenous arg types" in {
      //Given
      val add = Add(Literal(1d), Literal(3.5))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 4.5
    }

  }

  "Multiply" should {
    "returns 0 when no arguments are supplied" in {
      //Given
      val multiply = Multiply()
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "returns argument value when 1 argument is supplied" in {
      //Given
      val value = 3.5
      val multiply = Multiply(Literal(value))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(value)
    }

    "returns product of homogeneous args type" in {
      //Given
      val multiply = Multiply(Literal(.5), Literal(2d))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(1.0)
    }

  }
}
