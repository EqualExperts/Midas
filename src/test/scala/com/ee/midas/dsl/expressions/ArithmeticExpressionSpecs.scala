package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class ArithmeticExpressionSpecs extends Specification {
  "Add" should {
    "Give result as 0 when no values are supplied" in {
      //Given
      val add = Add()
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "Give sum of Integer values supplied " in {
      //Given
      val add = Add(Literal(1), Literal(3))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 4
    }

    "Give sum of homogenous types" in {
      //Given
      val add = Add(Literal(1d), Literal(3.5))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 4.5
    }

    "Give results as 0 for sum of nulls" in {
      //Given
      val add = Add(Literal(null))
      val document = new BasicBSONObject()

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 0
    }


    "Give sum of field value and literal" in {
      //Given
      val add = Add(Literal(1d), Field("age"))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = add.evaluate(document).value

      //Then
      result mustEqual 3.0
    }

    "Give literal value as sum when field does not exist" in {
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
    "Give result as 0 when no values are supplied" in {
      //Given
      val multiply = Multiply()
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "Give same value when 1 value is supplied" in {
      //Given
      val value = 3.5
      val multiply = Multiply(Literal(value))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(value)
    }

    "Give product of homogeneous types" in {
      //Given
      val multiply = Multiply(Literal(.5), Literal(2d))
      val document = new BasicBSONObject()

      //When
      val result = multiply.evaluate(document)

      //Then
      result mustEqual Literal(1.0)
    }

    "Give product of field value and literal" in {
      //Given
      val multiply = Multiply(Literal(1d), Field("age"))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = multiply.evaluate(document).value

      //Then
      result mustEqual 2.0
    }

    "Give product as 0 when field does not exist" in {
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
    "Give result as 0 when no values are supplied" in {
      //Given
      val subtract = Subtract()
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "Give result as 0 when 1 value is supplied" in {
      //Given
      val subtract = Subtract(Literal(0))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "Give difference when minuend and subtrahend are supplied" in {
      //Given
      val subtract = Subtract(Literal(1d), Literal(3.5))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual -2.5
    }

    "Gives difference of first two values when more than 2 are supplied ignoring the rest" in {
      //Given
      val subtract = Subtract(Literal(1d), Literal(3.5), Literal(4), Literal(-3.2))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual -2.5
    }

    "Gives difference between field value and literal" in {
      //Given
      val subtract = Subtract(Field("age"), Literal(1d))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual 1.0
    }

    "Gives difference when field does not exist" in {
      //Given
      val subtract = Subtract(Field("age"), Literal(5))
      val document = new BasicBSONObject()

      //When
      val result = subtract.evaluate(document).value

      //Then
      result mustEqual -5
    }
  }

  "Divide" should {
    "Give result as 0 when no values are supplied" in {
      //Given
      val divide = Divide()
      val document = new BasicBSONObject()

      //When
      val result = divide.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "Give result as 0 when 1 value is supplied" in {
      //Given
      val divide = Divide(Literal(3))
      val document = new BasicBSONObject()

      //When
      val result = divide.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "Give division of homogeneous types" in {
      //Given
      val divide = Divide(Literal(-4), Literal(2d))
      val document = new BasicBSONObject()

      //When
      val result = divide.evaluate(document)

      //Then
      result mustEqual Literal(-2.0)
    }

    "Give division of field value and literal" in {
      //Given
      val divide = Divide(Field("age"), Literal(2d))
      val document = new BasicBSONObject().append("age", 2)

      //When
      val result = divide.evaluate(document).value

      //Then
      result mustEqual 1.0
    }

    "Give result as 0 when field does not exist" in {
      //Given
      val divide = Divide(Field("age"), Literal(5))
      val document = new BasicBSONObject()

      //When
      val result = divide.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "Give NaN when a value is divided by 0" in {
      //Given
      val divide = Divide(Literal(5), Literal(0))
      val document = new BasicBSONObject()

      //When
      val result = divide.evaluate(document).value

      //Then
      result.asInstanceOf[Double].isNaN must beTrue
    }
  }

  "Mod" should {
    "Give result as 0 when no values are supplied" in {
      //Given
      val mod = Mod()
      val document = new BasicBSONObject()

      //When
      val result = mod.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "Give result as 0 when 1 value is supplied" in {
      //Given
      val mod = Mod(Literal(3))
      val document = new BasicBSONObject()

      //When
      val result = mod.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "Give remainder from division of homogeneous types" in {
      //Given
      val mod = Mod(Literal(-4), Literal(2d))
      val document = new BasicBSONObject()

      //When
      val result = mod.evaluate(document)

      //Then
      result mustEqual Literal(0)
    }

    "Give remainder from division of field value and literal" in {
      //Given
      val mod = Mod(Field("age"), Literal(2d))
      val document = new BasicBSONObject().append("age", 5)

      //When
      val result = mod.evaluate(document).value

      //Then
      result mustEqual 1.0
    }

    "Give result as 0 when field does not exist" in {
      //Given
      val mod = Mod(Field("age"), Literal(5))
      val document = new BasicBSONObject()

      //When
      val result = mod.evaluate(document).value

      //Then
      result mustEqual 0
    }

    "Give result as 0 when a value is divided by 0" in {
      //Given
      val mod = Mod(Literal(5), Literal(0))
      val document = new BasicBSONObject()

      //When
      val result = mod.evaluate(document).value

      //Then
      result mustEqual 0
    }
  }
}
