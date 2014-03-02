package com.ee.midas.dsl.expressions

import org.specs2.mutable.Specification

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.matcher.DataTables

@RunWith(classOf[JUnitRunner])
class LiteralSpecs extends Specification with DataTables {
  "Literal" should {
    "always give same value for an Integer" in {
      //Given
      val one = 1
      val literal = Literal(one)
      val document = new BasicBSONObject()

      //When
      val result = literal.evaluate(document).value

      //Then
      result mustEqual one
    }

    "always give the same value for a Double" in {
      //Given
      val one = 1d
      val constant = Literal(one)
      val document = new BasicBSONObject()

      //When
      val result = constant.evaluate(document).value

      //Then
      result mustEqual one
    }

    "always give the same value for a String" in {
      //Given
      val one = "one"
      val literal = Literal(one)
      val document = new BasicBSONObject()

      //When
      val result = literal.evaluate(document).value

      //Then
      result mustEqual one
    }

    "stringify just like how it is written" ^ {
           "literal"    |  "literalString"  |
         Literal(2.5)   !  "Literal(2.5)"   |
         Literal(true)  !  "Literal(true)"  |
         Literal(null)  !  "Literal(null)"  |
         Literal(3)     !  "Literal(3)"     |>
        { (literal, literalString) =>  literal.toString mustEqual literalString }
    }
  }
}
