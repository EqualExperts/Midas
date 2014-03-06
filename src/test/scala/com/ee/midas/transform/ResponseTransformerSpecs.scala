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

package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import scala.collection.immutable.TreeMap
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ResponseTransformerSpecs extends Specification with Mockito {
  trait Transformations extends Transformer with Scope {
    def dummyExpansionFunc1: Snippet = (bsonObj: BSONObject) => {
      bsonObj.put("expansion1", "applied")
      bsonObj
    }

    def dummyExpansionFunc2: Snippet = (bsonObj: BSONObject) => {
      bsonObj.put("expansion2", "applied")
      bsonObj
    }

    def dummyContractionFunc1: Snippet = (bsonObj: BSONObject) => {
      bsonObj.put("contraction1", "applied")
      bsonObj
    }

    def dummyContractionFunc2: Snippet = (bsonObj: BSONObject) => {
      bsonObj.put("contraction2", "applied")
      bsonObj
    }

    val dummyVersionExpansion: VersionedSnippets = TreeMap(1d -> dummyExpansionFunc1, 2d -> dummyExpansionFunc2)
    val dummyVersionContraction: VersionedSnippets = TreeMap(1d -> dummyContractionFunc1, 2d -> dummyContractionFunc2)

    var responseExpansions : Map[String, VersionedSnippets] = Map("validCollectionForExpansion" -> dummyVersionExpansion)
    var responseContractions : Map[String, VersionedSnippets] = Map("validCollectionForContraction" -> dummyVersionContraction)

    var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map()
    var requestContractions: Map[ChangeSetCollectionKey, Double] = Map()
  }

  "Response transformer" should {
    "apply expansion snippets for document with valid Collection name" in new Transformations {
      //given
      var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformResponse(document, "validCollectionForExpansion")

      //then
      transformedDocument.containsField("expansion1") && transformedDocument.containsField("expansion2")
    }

    "add expansion version to a virgin document with valid Collection name" in new Transformations {
      //given
      var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformResponse(document, "validCollectionForExpansion")

      //then
      transformedDocument.get(TransformType.EXPANSION.versionFieldName()) must_== (dummyVersionExpansion.size)
    }

    "update expansion version of already transformed document with valid Collection name" in new Transformations {
      //given
      var transformType = TransformType.EXPANSION
      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.EXPANSION.versionFieldName, initialVersion)

      //when
      val transformedDocument = transformResponse(document, "validCollectionForExpansion")

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.EXPANSION.versionFieldName) must_== dummyVersionExpansion.size)
    }

    "apply contraction snippets to the document with valid Collection name" in new Transformations {
      //given
      val document = new BasicBSONObject("name", "dummy")
      var transformType = TransformType.CONTRACTION

      //when
      val transformedDocument = transformResponse(document, "validCollectionForContraction")

      //then
      transformedDocument.containsField("contraction1") && transformedDocument.containsField("contraction2")
    }

    "add contraction version to a virgin document with valid Collection name" in new Transformations {
      //given
      val document = new BasicBSONObject("name", "dummy")
      override var transformType = TransformType.CONTRACTION

      //when
      val transformedDocument = transformResponse(document, "validCollectionForContraction")

      //then
      transformedDocument.get(TransformType.CONTRACTION.versionFieldName()) must_== (dummyVersionContraction.size)
    }

    "update contraction version of already transformed document with valid Collection name" in new Transformations {
      //given
      override var transformType = TransformType.CONTRACTION

      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.CONTRACTION.versionFieldName, initialVersion)

      //when
      val transformedDocument = transformResponse(document, "validCollectionForContraction")

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.CONTRACTION.versionFieldName) must_== dummyVersionContraction.size)
    }

    "Do not transform document with invalid collection name" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val collection = "invalidCollectionName"
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformResponse(document, collection)

      //then
      transformedDocument mustEqual document
    }

  }
}
