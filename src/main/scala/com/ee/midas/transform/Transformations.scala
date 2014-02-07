package com.ee.midas.transform

import com.mongodb.util.JSON
import org.bson.BSONObject
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.dsl.expressions.Parser

class Transformations extends Transforms with Parser {
  //  WARNING: DO NOT WRITE ANYTHING IN THIS FILE, IT IS REGENERATED AT RUNTIME!!
  override var expansions: Map[String, VersionedSnippets] = Map()
  override var contractions: Map[String, VersionedSnippets] = Map()
  override implicit var transformType = TransformType.EXPANSION
}


