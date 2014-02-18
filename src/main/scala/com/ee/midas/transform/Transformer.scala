package com.ee.midas.transform

abstract class Transformer extends ResponseTransformer with RequestTransformer {

  var transformType: TransformType

  override def toString = {
    val separator = "\n\t\t\t\t\t\t"
    s"""
      |======================================================================
      |Request
      | |
      | +--> Expansions = ${requestExpansions.size} [${requestExpansions mkString separator}]
      | |
      | +--> Contractions = ${requestContractions.size} [${requestContractions mkString separator}]
      |----------------------------------------------------------------------
      |Response
      | |
      | +--> Expansions = ${responseExpansions.size} [${responseExpansions mkString separator}]
      | |
      | +--> Contractions = ${responseContractions.size} [${responseContractions mkString separator}]
      |======================================================================
     """.stripMargin
  }
}

object Transformer {
  private object EmptyTransformer extends Transformer {
    var responseExpansions: Map[String, VersionedSnippets] = Map()
    var responseContractions: Map[String, VersionedSnippets] = Map()
    var transformType: TransformType = TransformType.EXPANSION
    var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map()
    var requestContractions: Map[ChangeSetCollectionKey, Double] = Map()
  }

  def empty: Transformer = EmptyTransformer
}
