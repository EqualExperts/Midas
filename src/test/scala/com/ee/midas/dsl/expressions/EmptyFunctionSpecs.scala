package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class EmptyFunctionSpecs extends Specification {
  "Empty" should {
    "return null when no arguments are supplied" in {
      //Given
      val empty = EmptyFunction()
      val document = new BasicBSONObject()

      //When
      val result = empty.evaluate(document).value

      //Then
      result mustEqual null
    }

    "return null when 1 argument is supplied" in {
      //Given
      val empty = EmptyFunction(Literal(1))
      val document = new BasicBSONObject()

      //When
      val result = empty.evaluate(document).value

      //Then
      result mustEqual null
    }

    "return null when more than 1 argument is supplied" in {
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
