package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.BasicBSONObject
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VersionerSpecs extends Specification with Versioner{
  "Versioner " should {

    "add version field if it doesn't exist in a document" in {
      val document = new BasicBSONObject("name", "midas")

      version(document)

      document.get(VERSION_FIELD) mustEqual 1
    }

    "increment version if exists in a document" in {
      val document = new BasicBSONObject("name", "midas")
      document.put(VERSION_FIELD, 1)

      version(document)

      document.get(VERSION_FIELD) mustEqual 2
    }
  }
}
