package com.ee.midas.transform

import org.bson.BasicBSONObject
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class RequestTransformerSpecs extends Specification {

  trait Transformations extends Transformer with Scope {
    var responseExpansions : Map[String, VersionedSnippets] = Map()
    var responseContractions : Map[String, VersionedSnippets] = Map()

    var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 4d))
    var requestContractions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 2d))
  }

  "Request transformer" should {
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
