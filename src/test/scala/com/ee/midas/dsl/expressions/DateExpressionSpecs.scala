/******************************************************************************
  * Copyright (c) 2014, Equal Experts Ltd
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation
  * are those of the authors and should not be interpreted as representing
  * official policies, either expressed or implied, of the Midas Project.
  ******************************************************************************/

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