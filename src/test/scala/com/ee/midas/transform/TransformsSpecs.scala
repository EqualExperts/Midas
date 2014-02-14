package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import scala.collection.immutable.TreeMap
import org.specs2.specification.Scope
import com.ee.midas.config.ChangeSet

@RunWith(classOf[JUnitRunner])
class TransformsSpecs extends Specification with Mockito {
  trait Transformations extends Transforms with Scope {
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

    override var responseExpansions : Map[String, VersionedSnippets] = Map("validCollectionForExpansion" -> dummyVersionExpansion)
    override var responseContractions : Map[String, VersionedSnippets] = Map("validCollectionForContraction" -> dummyVersionContraction)

    override var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map(((1,"validCollection"), 4d))
    override var requestContractions: Map[ChangeSetCollectionKey, Double] = Map(((1,"validCollection"), 2d))
  }

  "Response transforms" should {
    "apply expansion snippets" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformResponse(document, "validCollectionForExpansion")

      //then
      transformedDocument.containsField("expansion1") && transformedDocument.containsField("expansion2")
    }

    "add expansion version to a virgin document" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformResponse(document, "validCollectionForExpansion")

      //then
      transformedDocument.get(TransformType.EXPANSION.versionFieldName()) must_== (dummyVersionExpansion.size)
    }

    "update expansion version of already transformed document" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.EXPANSION.versionFieldName, initialVersion)

      //when
      val transformedDocument = transformResponse(document, "validCollectionForExpansion")

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.EXPANSION.versionFieldName) must_== dummyVersionExpansion.size)
    }

    "apply contraction snippets" in new Transformations {
      //given
      val document = new BasicBSONObject("name", "dummy")
      override var transformType = TransformType.CONTRACTION

      //when
      val transformedDocument = transformResponse(document, "validCollectionForContraction")

      //then
      transformedDocument.containsField("contraction1") && transformedDocument.containsField("contraction2")
    }

    "add contraction version to a virgin document" in new Transformations {
      //given
      val document = new BasicBSONObject("name", "dummy")
      override var transformType = TransformType.CONTRACTION

      //when
      val transformedDocument = transformResponse(document, "validCollectionForContraction")

      //then
      transformedDocument.get(TransformType.CONTRACTION.versionFieldName()) must_== (dummyVersionContraction.size)
    }

    "update contraction version of already transformed document" in new Transformations {
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

    "accept new expansions and contractions" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val newTransformsMock = mock[Transforms]
      val mockExpansions = mock[Map[String, newTransformsMock.VersionedSnippets]]
      val mockContractions = mock[Map[String, newTransformsMock.VersionedSnippets]]
      newTransformsMock.responseExpansions returns mockExpansions
      newTransformsMock.responseContractions returns mockContractions

      //when
      injectState(newTransformsMock)

      //then
      responseExpansions mustEqual mockExpansions
      responseContractions mustEqual mockContractions
    }

    "return false if transformations cannot be applied for a collection name" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val collection = "invalidCollectionName"

      //when
      //then
      canTransformResponse(collection) must beFalse
    }

    "return true if expansion can be applied for a collection name" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val collection = "validCollectionForExpansion"

      //when
      //then
      canTransformResponse(collection) must beTrue
    }

    "return true if contraction can be applied for a collection name" in new Transformations {
      //given
      override var transformType = TransformType.CONTRACTION
      val collection = "validCollectionForContraction"

      //when
      //then
      canTransformResponse(collection) must beTrue
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
