package com.ee.midas.dsl.generator

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.dsl.grammar.Verb
import org.specs2.specification.Scope
import org.bson.{BasicBSONObject, BSONObject}
import com.ee.midas.transform.ResponseTypes

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
      val document: BSONObject = new BasicBSONObject()
      document.put("age", 10)
      val expectedBSONObject = new BasicBSONObject("age", 11)

      //When
      val snippet = provideSnippet(verb, args)

      //Then
      snippet(document) mustEqual expectedBSONObject
    }

    "provides snippet for transform operation subtract " in new Snippet {
      //Given
      val verb = Verb.transform
      val args: Array[String] = Array("age", """{ $subtract: ["$age", 1] }""")
      val document: BSONObject = new BasicBSONObject()
      document.put("age", 10)
      val expectedBSONObject = new BasicBSONObject("age", 9)

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
      val expectedBSONObject = new BasicBSONObject("age", 20)

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
      val expectedBSONObject = new BasicBSONObject("age", 5)

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
      val expectedBSONObject = new BasicBSONObject("age", 0)

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
  }

}
