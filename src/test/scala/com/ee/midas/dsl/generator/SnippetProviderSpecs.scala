package com.ee.midas.dsl.generator

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.dsl.grammar.Verb
import org.specs2.specification.Scope
import org.bson.{BasicBSONObject, BSONObject}

@RunWith(classOf[JUnitRunner])
class SnippetProviderSpecs extends Specification {

  trait Snippet extends SnippetProvider with Scope {
    def snippet(verb: Verb, args: Array[String]): BSONObject => BSONObject = toSnippet(verb, args)
  }

  "Snippet Provider" should {
      "generate snippet for add" in new Snippet {
         //Given
         val verb = Verb.add
         val args: Array[String] = Array("{\"age\" : 0 }")
         val document: BSONObject = new BasicBSONObject()
         val expectedBSONObject = new BasicBSONObject("age", 0)

         //When
         val resultSnippet = snippet(verb,args)

         //Then
        resultSnippet(document) mustEqual expectedBSONObject
      }

     "generate snippet for remove" in new Snippet {
        //Given
        val verb = Verb.remove
        val args: Array[String] = Array("['age']")
        val document: BSONObject = new BasicBSONObject("age", 0)
        val expectedBSONObject = new BasicBSONObject()
        //When
        val resultSnippet = snippet(verb,args)

        //Then
        resultSnippet(document) mustEqual expectedBSONObject
     }

    "generate snippet for copy" in new Snippet {
        //Given
        val verb = Verb.copy
        val args: Array[String] = Array("pin", "zip")
        val document: BSONObject = new BasicBSONObject("pin", 0)
        val expectedBSONObject = new BasicBSONObject("pin", 0)
        expectedBSONObject.put("zip", 0)

        //When
        val resultSnippet = snippet(verb,args)

        //Then
        resultSnippet(document) mustEqual expectedBSONObject
    }

    "generate snippet for merge" in new Snippet {
        //Given
        val verb = Verb.merge
        val args: Array[String] = Array("[\"fname\",\"lname\"]", " ", "name")
        val document: BSONObject = new BasicBSONObject("lname", "Kennedy")
        document.put("fname", "John")
        val expectedBSONObject = new BasicBSONObject("lname", "Kennedy")
        expectedBSONObject.put("fname", "John")
        expectedBSONObject.put("name", "John Kennedy")

        //When
        val resultSnippet = snippet(verb,args)

        //Then
        (resultSnippet(document)) mustEqual expectedBSONObject
    }
  }

}
