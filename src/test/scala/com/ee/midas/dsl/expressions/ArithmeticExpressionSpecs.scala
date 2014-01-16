package com.ee.midas.transform.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class ArithmeticExpressionSpecs extends Specification {
  "Add Function" should {
    "returns 0 when no arguments are supplied" in {
      //Given
      val add = Add[Int]()
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document)

      //Then
      result mustEqual 0
    }

    "returns sum of arguments supplied for Int" in {
      //Given
      val add = Add(Constant(1), Constant(3))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document)

      //Then
      result mustEqual 4
    }

    "returns sum of arguments supplied for Double" in {
      //Given
      val add = Add(Constant(1d), Constant(3.5))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document)

      //Then
      result mustEqual 4.5
    }

  }

  "Multiply Function" should {
    "returns 0 when no arguments are supplied" in {
      //Given
      val multiply = Multiply[Int]()
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual 0
    }

    "returns argument value when 1 argument is supplied" in {
      //Given
      val value = 3.5
      val multiply = Multiply(Constant(value))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual value
    }

    "returns product when multiple args are supplied" in {
      //Given
      val multiply = Multiply(Constant(.5), Constant(2d))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual 1.0
    }

  }
}
