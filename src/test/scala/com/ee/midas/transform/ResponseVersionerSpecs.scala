package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.BasicBSONObject
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResponseVersionerSpecs extends Specification with ResponseVersioner{
  "Response Versioner " should {

    "add version field for expansion if it doesn't exist in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")

      //when
      version(document)(TransformType.EXPANSION)

      //then
      document.get(TransformType.EXPANSION.versionFieldName()) mustEqual 1
    }

    "add version field for contraction if it doesn't exist in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")

      //when
      version(document)(TransformType.CONTRACTION)

      //then
      document.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 1
    }

    "increment expansion version if it exists in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")
      document.put(TransformType.EXPANSION.versionFieldName(), 1d)

      //when
      version(document)(TransformType.EXPANSION)

      //then
      document.get(TransformType.EXPANSION.versionFieldName()) mustEqual 2
    }

    "increment contraction version if it exists in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")
      document.put(TransformType.CONTRACTION.versionFieldName(), 1d)

      //when
      version(document)(TransformType.CONTRACTION)

      //then
      document.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 2
    }
  }
}
