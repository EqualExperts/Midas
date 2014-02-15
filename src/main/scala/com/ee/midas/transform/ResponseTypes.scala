package com.ee.midas.transform

import org.bson.BSONObject
import scala.collection.immutable.TreeMap

trait ResponseTypes {
  type Snippet = BSONObject => BSONObject
  type Snippets = Iterable[Snippet]
  type VersionedSnippets = TreeMap[Double, Snippet]
}
