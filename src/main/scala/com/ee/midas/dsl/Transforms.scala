package com.ee.midas.dsl

import org.bson.BSONObject

trait Transforms {
  type Snippet = BSONObject => BSONObject
  val expansions: List[Snippet]
  val contractions: List[Snippet]
}
