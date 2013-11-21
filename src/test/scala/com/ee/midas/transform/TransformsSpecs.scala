package com.ee.midas.transform

import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TransformsSpecs extends Specification with Transforms {
  def dummyExpansionFunc(bsonObj: BSONObject): BSONObject = {
    bsonObj.put("expansion", "applied")
    bsonObj
  }

  def dummyContractionFunc(bsonObj: BSONObject): BSONObject = {
    bsonObj.put("contraction", "applied")
    bsonObj
  }

  val dummyExpansion: Snippet = dummyExpansionFunc
  val dummyContraction: Snippet = dummyContractionFunc
  val expansions: List[Snippet] = List(dummyExpansion)
  val contractions: List[Snippet] = List(dummyContraction)

  "transforms trait" should {
    "apply expansion and contraction snippets" in {
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = map(document)

      //then
      transformedDocument.containsField("expansion") && transformedDocument.containsField("contraction")
    }

    "add version to a virgin document" in {
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = map(document)

      //then
      transformedDocument.get("_version") must_== (expansions.size + contractions.size)
    }

    "update version of already transformed document" in {
      val initialVersion = 1
      val document = new BasicBSONObject("name", "dummy")
      document.put("_version", initialVersion)

      //when
      val transformedDocument = map(document)

      //then
      transformedDocument.get("_version") must_== (initialVersion + expansions.size + contractions.size)
    }
  }

}
