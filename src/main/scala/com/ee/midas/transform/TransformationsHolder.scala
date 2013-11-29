package com.ee.midas.transform

object TransformationsHolder {

  private val heldTransformations = scala.collection.mutable.MutableList(new Transformations)

  def get = heldTransformations.head

  def set(transformations: Transformations) = transformations +=: heldTransformations

  override def toString =
    s"""
      |===============================================================
      |Total Transformations in Holder = ${heldTransformations.size}
      |---------------------------------------------------------------
      |${heldTransformations mkString "\n"}
      |===============================================================
    """
}
