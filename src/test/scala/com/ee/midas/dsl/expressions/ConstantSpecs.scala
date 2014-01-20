package com.ee.midas.dsl.expressions

import org.specs2.mutable.Specification

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject

@RunWith(classOf[JUnitRunner])
class ConstantSpecs extends Specification {
  "Constant Function" should {
    "always return the same value for an Integer" in {
      //Given
      val one = 1
      val constant = Constant(one)
      val document = new BasicBSONObject()

      //When
      val result = constant.evaluate(document)

      //Then
      result mustEqual one
    }

    "always return the same value for a Double" in {
      //Given
      val one = 1d
      val constant = Constant(one)
      val document = new BasicBSONObject()

      //When
      val result = constant.evaluate(document)

      //Then
      result mustEqual one
    }

    "always return the same value for a String" in {
      //Given
      val one = "one"
      val constant = Constant(one)
      val document = new BasicBSONObject()

      //When
      val result = constant.evaluate(document)

      //Then
      result mustEqual one
    }

  }
}
