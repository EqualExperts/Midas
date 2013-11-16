package com.ee.midas.transform

import org.bson.BSONObject

trait Transforms extends Versioner {
  type Snippet = BSONObject => BSONObject
  val expansions: List[Snippet]
  val contractions: List[Snippet]

  def map(document: BSONObject) : BSONObject =
    (expansions ++ contractions).foldLeft(document) {
      case (document, operation) => (operation andThen version)(document)
    }
}
