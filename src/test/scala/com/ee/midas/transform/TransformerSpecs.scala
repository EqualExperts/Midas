package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import scala.collection.immutable.TreeMap
import org.specs2.specification.Scope

//todo: split this based on individual traits: ResponseSpecs
@RunWith(classOf[JUnitRunner])
class TransformerSpecs extends Specification with Mockito {
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

    var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 4d))
    var requestContractions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 2d))
  }

  //todo: revisit whether the below are applicable
  /**

  "transform document in EXPANSION mode" in {
    //Given

    val fullCollectionName : String = "name"
    val document = new BasicBSONObject("name","testCollection")
    val expectedDocument = document.append("new", "value")
    //      transforms.transformResponse(document, fullCollectionName) returns expectedDocument

    //When
    //      val transformer = new Transformer

    //Then
    //      transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
  }

  "transforms document in CONTRACTION mode" in {
    //Given
    val fullCollectionName : String = "name"
    val document = new BasicBSONObject("name","testCollection")
    val expectedDocument = document.append("new","value")
    //      transforms.transformResponse(document, fullCollectionName) returns expectedDocument

    //When
    //      val transformer = new Transformer

    //Then
    //      transformer.transformResponse(document, fullCollectionName)  mustEqual  expectedDocument
  }

  */


  "Response transforms" should {
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

  "Request transforms" should {
    "add expansion version in EXPANSION mode" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 4d
    }

    "Do not override expansion version if already exists in EXPANSION mode" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.EXPANSION.versionFieldName, 3d)

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 3d
    }

    "add contraction and expansion version for CONTRACTION mode" in new Transformations {
      //given
      val document = new BasicBSONObject("name", "dummy")
      override var transformType = TransformType.CONTRACTION

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 4d
      transformedDocument.get("_contractionVersion") mustEqual 2d
    }

    "Do not override contraction version if already exists in CONTRACTION mode" in new Transformations {
      //given
      override var transformType = TransformType.CONTRACTION
      val document = new BasicBSONObject("name", "dummy")
      document.put("_contractionVersion", 1d)

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 4d
      transformedDocument.get("_contractionVersion") mustEqual 1d
    }


    "Do not override expansion version if already exists in CONTRACTION mode" in new Transformations {
      //given
      override var transformType = TransformType.CONTRACTION
      val document = new BasicBSONObject("name", "dummy")
      document.put("_expansionVersion", 3d)

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 3d
      transformedDocument.get("_contractionVersion") mustEqual 2d
    }
  }

}
