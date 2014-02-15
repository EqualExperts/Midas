package com.ee.midas.transform

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.bson.BasicBSONObject

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

    "Do not override  expansion version if it exists in a document" in {
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
  }
}
