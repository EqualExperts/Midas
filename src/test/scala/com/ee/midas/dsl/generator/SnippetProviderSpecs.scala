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

package com.ee.midas.dsl.generator

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.dsl.grammar.Verb
import org.specs2.specification.Scope
import org.bson.{BasicBSONObject, BSONObject}
import com.ee.midas.transform.ResponseTypes
import java.util
import com.mongodb.util.JSON

@RunWith(classOf[JUnitRunner])
class SnippetProviderSpecs extends Specification {

  trait Snippet extends SnippetProvider with ResponseTypes with Scope {
    def provideSnippet(verb: Verb, args: Array[String]): Snippet = toSnippet(verb, args)
  }

  "Snippet Provider" should {
      "provides snippet for add" in new Snippet {
         //Given
         val verb = Verb.add
         val args: Array[String] = Array("{\"age\" : 0 }")
         val document: BSONObject = new BasicBSONObject()
         val expectedBSONObject = new BasicBSONObject("age", 0)

         //When
         val snippet = provideSnippet(verb, args)

         //Then
        snippet(document) mustEqual expectedBSONObject
      }

    "provides snippet for add for multiple fields" in new Snippet {
      //Given
      val verb = Verb.add
      val args: Array[String] = Array("{\"age\" : 0, \"isPresent\": false }")
      val document: BSONObject = new BasicBSONObject()
      val expectedBSONObject = new BasicBSONObject("age", 0).append("isPresent", false)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "parses" in new Snippet {
      //Given
      val input =
        """
          |{
          |  "zip" : 400058,
          |  "executionDate" : { "$date": "Jun 23, 1912"}
          |}
        """.stripMargin
      println(s"Parsing Input = $input")
      println("Parsed JSON = " + JSON.parse(input))

      //Then
      true mustEqual true
    }

    "provides snippet for remove" in new Snippet {
        //Given
        val verb = Verb.remove
        val args: Array[String] = Array("['age']")
        val document: BSONObject = new BasicBSONObject("age", 0)
        val expectedBSONObject = new BasicBSONObject()
        //When
        val snippet = provideSnippet(verb, args)

        //Then
       snippet(document) mustEqual expectedBSONObject
     }

    "provides snippet for copy" in new Snippet {
        //Given
        val verb = Verb.copy
        val args: Array[String] = Array("pin", "zip")
        val document: BSONObject = new BasicBSONObject("pin", 0)
        val expectedBSONObject = new BasicBSONObject("pin", 0)
        expectedBSONObject.put("zip", 0)

        //When
        val snippet = provideSnippet(verb, args)

        //Then
        snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for merge" in new Snippet {
        //Given
        val verb = Verb.merge
        val args: Array[String] = Array("[\"fname\", \"lname\"]", " ", "name")
        val document: BSONObject = new BasicBSONObject("lname", "Kennedy")
        document.put("fname", "John")
        val expectedBSONObject = new BasicBSONObject("lname", "Kennedy")
        expectedBSONObject.put("fname", "John")
        expectedBSONObject.put("name", "John Kennedy")

        //When
        val snippet = provideSnippet(verb, args)

        //Then
        (snippet(document)) mustEqual expectedBSONObject
    }

    "provides snippet for split" in new Snippet {
      //Given
      val verb = Verb.split
      val args: Array[String] = Array("name", "^([a-zA-Z]+) ([a-zA-Z]+)$", "{ \"fName\": \"$1\", \"lName\": \"$2\" }")
      val document: BSONObject = new BasicBSONObject("name", "John Kennedy")
      val expectedBSONObject = new BasicBSONObject("name", "John Kennedy")
      expectedBSONObject.put("fName", "John")
      expectedBSONObject.put("lName", "Kennedy")

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation add " in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $add: ["$age", 1] }""")
      val document: BSONObject = new BasicBSONObject("age", 10)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      val expectedBSONObject = new BasicBSONObject("age", 11.0)
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation subtract " in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $subtract: ["$age", 1] }""")
      val document: BSONObject = new BasicBSONObject()
      document.put("age", 10)
      val expectedBSONObject = new BasicBSONObject("age", 9.0)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation multiply " in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $multiply: ["$age", 2] }""")
      val document: BSONObject = new BasicBSONObject()
      document.put("age", 10)
      val expectedBSONObject = new BasicBSONObject("age", 20.0)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation divide " in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $divide: ["$age", 2] }""")
      val document: BSONObject = new BasicBSONObject()
      document.put("age", 10)
      val expectedBSONObject = new BasicBSONObject("age", 5.0)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation mod " in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $mod: ["$age", 2] }""")
      val document: BSONObject = new BasicBSONObject()
      document.put("age", 10)
      val expectedBSONObject = new BasicBSONObject("age", 0.0)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation concat" in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("name", """{ $concat: ["$name", "-"] }""")
      val document: BSONObject = new BasicBSONObject()
      document.put("name", "midas")
      val expectedBSONObject = new BasicBSONObject("name", "midas-")

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provide snippet for arithmetic transform operation containing string that generates error message in document" in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $divide: ["$age", 'someString'] }""")
      val document: BSONObject = new BasicBSONObject("age", 10)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      val expectedBSONObject = new BasicBSONObject("age", new BasicBSONObject("_errmsg",  """exception: For input string: "someString""""))
      snippet(document) mustEqual expectedBSONObject
    }

    "provide snippet for split operation containing invalid regex that generates error message in document" in new Snippet {
      //Given
      val verb = Verb.split
      val args: Array[String] = Array("name", "^Mr|Mrs|Ms|Miss ([a-zA-Z]+) ([a-zA-Z]+)$", """{"title": "$1", "firstName": "$2", "lastName": "$3"}""")
      val document: BSONObject = new BasicBSONObject("name", "Mr John Smith")

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      val expectedBSONObject = new BasicBSONObject("name", "Mr John Smith")
      expectedBSONObject.put("firstName", new BasicBSONObject("_errmsg", "exception: Cannot parse ^Mr|Mrs|Ms|Miss ([a-zA-Z]+) ([a-zA-Z]+)$"))
      expectedBSONObject.put("lastName", new BasicBSONObject("_errmsg", "exception: Cannot parse ^Mr|Mrs|Ms|Miss ([a-zA-Z]+) ([a-zA-Z]+)$"))
      expectedBSONObject.put("title", new BasicBSONObject("_errmsg", "exception: Cannot parse ^Mr|Mrs|Ms|Miss ([a-zA-Z]+) ([a-zA-Z]+)$"))
      snippet(document) mustEqual expectedBSONObject
    }
  }
}
