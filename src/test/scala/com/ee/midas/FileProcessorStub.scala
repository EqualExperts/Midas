package com.ee.midas

import com.ee.midas.transform.Transforms
import org.bson.{BSONObject, BasicBSONObject}
import com.mongodb.util.JSON
import com.ee.midas.transform.DocumentOperations._

class FileProcessorStub extends Transforms {
  var expansions: Map[String, FileProcessorStub#VersionedSnippets] = Map(("field", Map((1d,dummyExpansionFunc1))))
  var contractions: Map[String, FileProcessorStub#VersionedSnippets] = Map(("field", Map((1d,dummyExpansionFunc2))))

  def dummyExpansionFunc1: Snippet = (bsonObj: BSONObject) => {
    bsonObj.put("expansion1", "applied")
    bsonObj
  }

  def dummyExpansionFunc2: Snippet = (bsonObj: BSONObject) => {
    bsonObj.put("expansion2", "applied")
    bsonObj
  }
}
