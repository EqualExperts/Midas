package com.ee.midas.transform

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.bson.{BSONObject, BasicBSONObject}

@RunWith(classOf[JUnitRunner])
class RequestVersionerSpecs extends Specification with RequestVersioner {
  "Request Versioner " should {

    "add expansion version field if it doesn't exist in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")

      //when
      addExpansionVersion(document, 4d)

      //then
      document.get(TransformType.EXPANSION.versionFieldName()) mustEqual 4d
    }

    "add contraction version field if it doesn't exist in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")

      //when
      addContractionVersion(document, 3d)

      //then
      document.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 3d
    }

    "Do not override expansion version if it exists in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")
      document.put(TransformType.EXPANSION.versionFieldName(), 1d)

      //when
      addExpansionVersion(document, 3d)

      //then
      document.get(TransformType.EXPANSION.versionFieldName()) mustEqual 1d
    }

    "Do not override contraction version if it exists in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")
      document.put(TransformType.CONTRACTION.versionFieldName(), 2d)

      //when
      addContractionVersion(document, 5d)

      //then
      document.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 2d
    }

    "add expansion version to a document with set update operator" in {
      //given
      val document = new BasicBSONObject("$set", new BasicBSONObject("name", "midas"))

      //when
      addExpansionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.EXPANSION.versionFieldName()) mustEqual 4d
      setDocument.get("name") mustEqual "midas"
    }

    "add expansion version to a document with update operators other than set" in {
      //given
      val document = new BasicBSONObject("$inc", new BasicBSONObject("value", 1))

      //when
      addExpansionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.EXPANSION.versionFieldName()) mustEqual 4d
    }

    "add contraction version to a document with set update operator" in {
      //given
      val document = new BasicBSONObject("$set", new BasicBSONObject("name", "midas"))

      //when
      addContractionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 4d
      setDocument.get("name") mustEqual "midas"
    }

    "add contraction version to a document with update operators other than set" in {
      //given
      val document = new BasicBSONObject("$inc", new BasicBSONObject("value", 1))

      //when
      addContractionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 4d
    }
  }
}
