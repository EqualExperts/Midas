package com.ee.midas.transform

import org.bson.BSONObject
import TransformType._

trait Transforms extends Versioner {
  type Snippet = BSONObject => BSONObject
  type Snippets = Iterable[Snippet]
  type VersionedSnippets = Map[Int, Snippet]
  val expansions : Map[String, VersionedSnippets]
  val contractions : Map[String, VersionedSnippets]

  def canBeApplied(fullCollectionName: String): Boolean =
    expansions.keySet.contains(fullCollectionName) || contractions.keySet.contains(fullCollectionName)

  def map(document: BSONObject)(implicit fullCollectionName: String, transformType : TransformType) : BSONObject =  {
    versionedSnippets(fullCollectionName) match {
      case map if map.isEmpty => document
      case vs =>
        val version = getVersion(document) match {
          case Some(version) => version + 1
          case None => 1
        }
        val snippets = snippetsFrom(version, vs)
        applySnippets(snippets, document)
    }
  }

  def versionedSnippets(fullCollectionName: String)(implicit transformType: TransformType): VersionedSnippets =
    if(transformType == EXPANSION)
      expansions(fullCollectionName)
    else if(transformType == CONTRACTION)
      contractions(fullCollectionName)
    else Map.empty

  def snippetsFrom(version: Int, versionedSnippets: VersionedSnippets) =
    versionedSnippets.filterKeys(v => v >= version).unzip._2

  def applySnippets(snippets: Snippets, document: BSONObject)(implicit transformType: TransformType): BSONObject =
    snippets.foldLeft(document) {
      case (document, snippet) => (snippet andThen version)(document)
    }
}
