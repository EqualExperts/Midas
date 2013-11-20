package com.ee.midas.transform

import org.bson.BSONObject

trait Transforms extends Versioner {
  type Snippet = BSONObject => BSONObject
  type VersionedSnippets = Map[Int, Snippet]
  val expansions : Map[String, VersionedSnippets]
  val contractions : Map[String, VersionedSnippets]

  def map(document: BSONObject) : BSONObject = document
//    (expansions ++ contractions).foldLeft(document) {
//      case (document, operation) => (operation andThen version)(document)
//    }
}
