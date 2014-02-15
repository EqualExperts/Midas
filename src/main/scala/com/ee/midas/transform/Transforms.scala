package com.ee.midas.transform

abstract class Transforms extends ResponseTransforms with RequestTransforms {

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
