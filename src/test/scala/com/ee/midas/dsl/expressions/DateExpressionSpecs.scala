package com.ee.midas.dsl.expressions

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.{BSONObject, BasicBSONObject}
import org.specs2.mutable.{Tables, Specification}
import java.text.SimpleDateFormat
import scala.Predef._

@RunWith(classOf[JUnitRunner])
class DateExpressionSpecs extends Specification with Tables {
  "Date" should {
    "returns date for various date formats" in {
           "format"   |  "value"       |
        "dd-MMM-yyyy" !  "07-Mar-2014" |
        "dd-MMM-yy"   !  "18-Aug-87"  |>
        { (format: String, value: String) =>
          //Given
          val date = Date(Literal(format), Literal(value))
          val document = new BasicBSONObject()

          //When
          val result = date.evaluate(document).value

          //Then
          val expectedDate = new SimpleDateFormat(format).parse(value)
          result mustEqual expectedDate
        }
    }

    "Treats Literal" should {
      val dateFunction = new DateFunction {
        def evaluate(document: BSONObject): Literal = ???
      }

      "null by a loud shout" in {
        //When-Then
        dateFunction.value(Literal(null)) must throwA[IllegalArgumentException]
      }

      "value as a string that would be Date parseable" in {
             "literal"            |  "expected"   |
            Literal(5)            !  "5"          |
            Literal(55.7)         !  "55.7"       |
            Literal("18-Aug-87")  !  "18-Aug-87"  |>
            { (literal: Literal , expected: String) =>
              dateFunction.value(literal) mustEqual expected
            }
      }
    }
  }
}