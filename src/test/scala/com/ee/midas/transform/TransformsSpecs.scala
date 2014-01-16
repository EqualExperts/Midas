package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import scala.collection.immutable.TreeMap

@RunWith(classOf[JUnitRunner])
class TransformsSpecs extends Specification with Mockito {
  val tranformations = new Transforms {
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

    override var expansions : Map[String, VersionedSnippets] = Map("validCollectionForExpansion" -> dummyVersionExpansion)
    override var contractions : Map[String, VersionedSnippets] = Map("validCollectionForContraction" -> dummyVersionContraction)

    /*val expansions: List[Snippet] = List(dummyExpansion)
    val contractions: List[Snippet] = List(dummyContraction)*/
  }

  "transforms trait" should {
    "apply expansion snippets" in {
      //given
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = tranformations.map(document)("validCollectionForExpansion", TransformType.EXPANSION)

      //then
      transformedDocument.containsField("expansion1") && transformedDocument.containsField("expansion2")
    }

    "add expansion version to a virgin document" in {
      //given
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = tranformations.map(document)("validCollectionForExpansion", TransformType.EXPANSION)

      //then
      transformedDocument.get(TransformType.EXPANSION.versionFieldName()) must_== (tranformations.dummyVersionExpansion.size)
    }

    "update expansion version of already transformed document" in {
      //given
      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.EXPANSION.versionFieldName, initialVersion)

      //when
      val transformedDocument = tranformations.map(document)("validCollectionForExpansion", TransformType.EXPANSION)

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.EXPANSION.versionFieldName) must_== tranformations.dummyVersionExpansion.size)
    }

    "apply contraction snippets" in {
      //given
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = tranformations.map(document)("validCollectionForContraction", TransformType.CONTRACTION)

      //then
      transformedDocument.containsField("contraction1") && transformedDocument.containsField("contraction2")
    }

    "add contraction version to a virgin document" in {
      //given
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = tranformations.map(document)("validCollectionForContraction", TransformType.CONTRACTION)

      //then
      transformedDocument.get(TransformType.CONTRACTION.versionFieldName()) must_== (tranformations.dummyVersionContraction.size)
    }

    "update contraction version of already transformed document" in {
      //given
      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.CONTRACTION.versionFieldName, initialVersion)

      //when
      val transformedDocument = tranformations.map(document)("validCollectionForContraction", TransformType.CONTRACTION)

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.CONTRACTION.versionFieldName) must_== tranformations.dummyVersionContraction.size)
    }

    "accept new expansions and contractions" in {
      //given
      val newTransformsMock = mock[Transforms]
      val mockExpansions = mock[Map[String, newTransformsMock.VersionedSnippets]]
      val mockContractions = mock[Map[String, newTransformsMock.VersionedSnippets]]
      newTransformsMock.expansions returns mockExpansions
      newTransformsMock.contractions returns mockContractions

      //when
      tranformations.injectState(newTransformsMock)

      //then
      tranformations.expansions mustEqual mockExpansions
      tranformations.contractions mustEqual mockContractions
    }

    "return false if transformations cannot be applied for a collection name" in {
      //given
      val collection = "invalidCollectionName"

      //when
      //then
      tranformations.canBeApplied(collection) must beFalse
    }

    "return true if expansion can be applied for a collection name" in {
      //given
      val collection = "validCollectionForExpansion"

      //when
      //then
      tranformations.canBeApplied(collection) must beTrue
    }

    "return true if contraction can be applied for a collection name" in {
      //given
      val collection = "validCollectionForContraction"

      //when
      //then
      tranformations.canBeApplied(collection) must beTrue
    }

  }

}
