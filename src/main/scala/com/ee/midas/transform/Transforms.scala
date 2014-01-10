package com.ee.midas.transform

import org.bson.BSONObject
import TransformType._
import com.ee.midas.hotdeploy.Deployable

abstract class Transforms extends Versioner with Deployable[Transforms] {
  type Snippet = BSONObject => BSONObject
  type Snippets = Iterable[Snippet]
  type VersionedSnippets = Map[Double, Snippet]
  var expansions : Map[String, VersionedSnippets]
  var contractions : Map[String, VersionedSnippets]

  def injectState(fromTransforms: Transforms) = {
    this.expansions = fromTransforms.expansions
    this.contractions = fromTransforms.contractions
  }

  def canBeApplied(fullCollectionName: String): Boolean =
    expansions.keySet.contains(fullCollectionName) || contractions.keySet.contains(fullCollectionName)

  def map(document: BSONObject)(implicit fullCollectionName: String, transformType: TransformType) : BSONObject =  {
    versionedSnippets match {
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

  def versionedSnippets(implicit fullCollectionName: String, transformType: TransformType): VersionedSnippets =
    if(transformType == EXPANSION)
      expansions(fullCollectionName)
    else if(transformType == CONTRACTION)
      contractions(fullCollectionName)
    else Map.empty

  def snippetsFrom(version: Double, versionedSnippets: VersionedSnippets) =
    versionedSnippets.filterKeys(v => v >= version).unzip._2

  def applySnippets(snippets: Snippets, document: BSONObject)(implicit transformType: TransformType): BSONObject =
    snippets.foldLeft(document) {
      case (document, snippet) => (snippet andThen version)(document)
    }

  override def toString =
    s"""
      |======================================================================
      |Expansions = ${expansions.size} [${expansions mkString "\n"}]
      |
      |Contractions = ${contractions.size} [${contractions mkString "\n"}]
      |======================================================================
     """.stripMargin
}
