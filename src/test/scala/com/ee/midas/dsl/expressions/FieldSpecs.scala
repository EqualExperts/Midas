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

import org.specs2.mutable.Specification

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import org.specs2.matcher.DataTables

@RunWith(classOf[JUnitRunner])
class FieldSpecs extends Specification with DataTables {
  "Field" should {
    "give the value of a field if present in the document" in {
      //Given
      val fieldExpression = Field("zip")
      val zipValue = 4000058
      val document = new BasicBSONObject("zip", zipValue)

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(zipValue)
    }

    "give appropriate result if field is not present in the document" in {
      //Given
      val fieldExpression = Field("pin")
      val document = new BasicBSONObject()

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(null)
    }

    "give the value of a 2-level nested field" in {
      //Given
      val fieldExpression = Field("address.zip")
      val zipValue = 4000058
      val document = new BasicBSONObject("address", new BasicBSONObject("zip", zipValue))

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(zipValue)
    }

    "give the value of a 3-level nested field" in {
      //Given
      val fieldExpression = Field("address.line.1")
      val value = "Some Street"
      val document = new BasicBSONObject("address", 
        new BasicBSONObject("line", new BasicBSONObject("1", value)))

      //When
      val result = fieldExpression.evaluate(document)

      //Then
      result mustEqual Literal(value)
    }

    "stringify just like how it is written" ^ {
          "field"             |  "fieldString"        |
        Field("age")          !  "Field(age)"         |
        Field("order.date")   !  "Field(order.date)"  |>
        { (field, fieldString) =>  field.toString mustEqual fieldString }
    }
  }
}