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
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class StringFunctionSpecs extends Specification {
  "Concat" should {
    "returns empty string when no values are supplied" in {
      //Given
      val concat = Concat()
      val document = new BasicBSONObject()

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "concatenate string literals" in {
      //Given
      val concat = Concat(Literal("Hello"), Literal("World"))
      val document = new BasicBSONObject()

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "HelloWorld"
    }

    "concatenate non-string literals" in {
      //Given
      val concat = Concat(Literal(1), Literal(true), Literal(3.5), Literal(null))
      val document = new BasicBSONObject()

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "1true3.5"
    }

    "concatenate literals and field values" in {
      //Given
      val concat = Concat(Literal("Mr. "), Field("name"))
      val document = new BasicBSONObject().append("name", "Test")

      //When
      val result = concat.evaluate(document).value

      //Then
      result mustEqual "Mr. Test"
    }
  }

  "ToLower" should {
    "returns empty string when no values are supplied" in {
      //Given
      val toLower = ToLower()
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "convert string literal to lower case" in {
      //Given
      val toLower = ToLower(Literal("Hello"))
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual "hello"
    }

    "return non-string literals as string" in {
      //Given
      val toLower = ToLower(Literal(1))
      val document = new BasicBSONObject()

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual "1"
    }

    "convert field value to lower case" in {
      //Given
      val toLower = ToLower(Field("name"))
      val document = new BasicBSONObject().append("name", "TEST")

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual "test"
    }

    "Gives empty string if the field value does not exit" in {
      //Given
      val toLower = ToLower(Field("naem"))
      val document = new BasicBSONObject().append("name", "test")

      //When
      val result = toLower.evaluate(document).value

      //Then
      result mustEqual ""
    }
  }

  "ToUpper" should {
    "returns empty string when no values are supplied" in {
      //Given
      val toUpper = ToUpper()
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual ""
    }

    "convert string literal to upper case" in {
      //Given
      val toUpper = ToUpper(Literal("Hello"))
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual "HELLO"
    }

    "return non-string literals as string" in {
      //Given
      val toUpper = ToUpper(Literal(1))
      val document = new BasicBSONObject()

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual "1"
    }

    "convert field value to upper case" in {
      //Given
      val toUpper = ToUpper(Field("name"))
      val document = new BasicBSONObject().append("name", "test")

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual "TEST"
    }

    "Gives empty string if the field value does not exit" in {
      //Given
      val toUpper = ToUpper(Field("naem"))
      val document = new BasicBSONObject().append("name", "test")

      //When
      val result = toUpper.evaluate(document).value

      //Then
      result mustEqual ""
    }
  }

  "Treats Literal" should {
    val stringFunction = new StringFunction {
      def evaluate(document: BSONObject): Literal = ???
    }

    "null as empty string" in {
      //When
      val treatedOutcome = stringFunction.value(Literal(null))

      //Then
      treatedOutcome mustEqual ""
    }

    "Double value as String" in {
      //When
      val treatedOutcome = stringFunction.value(Literal(23e-2))

      //Then
      treatedOutcome mustEqual "0.23"
    }

    "Integer value as String value" in {
      //When
      val treatedOutcome = stringFunction.value(Literal(23))

      //Then
      treatedOutcome mustEqual "23"
    }
  }
}
