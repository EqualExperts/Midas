package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class EmptyFunctionSpecs extends Specification {
  "Empty Function" should {
    "Give appropriate result when no values are supplied" in {
      //Given
      val empty = EmptyFunction()
      val document = new BasicBSONObject()

      //When
      val result = empty.evaluate(document).value

      //Then
      result mustEqual null
    }

    "Give appropriate result when 1 value is supplied" in {
      //Given
      val empty = EmptyFunction(Literal(1))
      val document = new BasicBSONObject()

      //When
      val result = empty.evaluate(document).value

      //Then
      result mustEqual null
    }

    "Give appropriate result when more than 1 value is supplied" in {
      //Given
      val empty = EmptyFunction(Field("age"), Literal(3))
      val document = new BasicBSONObject()

      //When
      val result = empty.evaluate(document).value

      //Then
      result mustEqual null
    }
  }
}
