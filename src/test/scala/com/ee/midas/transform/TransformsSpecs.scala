package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TransformsSpecs extends Specification with Transforms {

  def dummyExpansionFunc1: Snippet  = (bsonObj: BSONObject) => {
    bsonObj.put("expansion1", "applied")
    bsonObj
  }

  def dummyExpansionFunc2: Snippet  = (bsonObj: BSONObject) => {
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

  val dummyVersionExpansion: VersionedSnippets = Map(1d -> dummyExpansionFunc1, 2d -> dummyExpansionFunc2)
  val dummyVersionContraction: VersionedSnippets = Map(1d -> dummyContractionFunc1, 2d -> dummyContractionFunc2)

  val expansions : Map[String, VersionedSnippets] = Map("someCollection" -> dummyVersionExpansion)
  val contractions : Map[String, VersionedSnippets] = Map("someCollection" -> dummyVersionContraction)

  /*val expansions: List[Snippet] = List(dummyExpansion)
  val contractions: List[Snippet] = List(dummyContraction)*/

  "transforms trait" should {
    "apply expansion snippets" in {
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = map(document)("someCollection", TransformType.EXPANSION)

      //then
      transformedDocument.containsField("expansion1") && transformedDocument.containsField("expansion2")
    }

    "add expansion version to a virgin document" in {
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = map(document)("someCollection", TransformType.EXPANSION)

      //then
      transformedDocument.get(TransformType.EXPANSION.versionFieldName()) must_== (dummyVersionExpansion.size)
    }

    "update expansion version of already transformed document" in {
      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.EXPANSION.versionFieldName, initialVersion)

      //when
      val transformedDocument = map(document)("someCollection", TransformType.EXPANSION)

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.EXPANSION.versionFieldName) must_== dummyVersionExpansion.size)
    }

    "apply contraction snippets" in {
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = map(document)("someCollection", TransformType.CONTRACTION)

      //then
      transformedDocument.containsField("contraction1") && transformedDocument.containsField("contraction2")
    }

    "add contraction version to a virgin document" in {
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = map(document)("someCollection", TransformType.CONTRACTION)

      //then
      transformedDocument.get(TransformType.CONTRACTION.versionFieldName()) must_== (dummyVersionContraction.size)
    }

    "update contraction version of already transformed document" in {
      val initialVersion = 1d
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.CONTRACTION.versionFieldName, initialVersion)

      //when
      val transformedDocument = map(document)("someCollection", TransformType.CONTRACTION)

      //then
      !transformedDocument.containsField("expansion1") &&
        (transformedDocument.get(TransformType.CONTRACTION.versionFieldName) must_== dummyVersionContraction.size)
    }
  }

}
