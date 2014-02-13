package com.ee.midas.transform

import com.ee.midas.hotdeploy.Deployable

abstract class Transforms extends Deployable[Transforms] with ResponseTransforms with RequestTransforms {

  var transformType: TransformType

  def injectState(fromTransforms: Transforms) = {
    this.responseExpansions = fromTransforms.responseExpansions
    this.responseContractions = fromTransforms.responseContractions
    this.requestExpansions = fromTransforms.requestExpansions
    this.requestContractions = fromTransforms.requestContractions
    this.transformType = fromTransforms.transformType
  }

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
