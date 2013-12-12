package com.ee.midas.transform

object TransformsHolder {

//  private val heldTransformations = scala.collection.mutable.MutableList(new Transformations)
  private val transforms = new Transformations

//  def get = heldTransformations.head
  def get = transforms

//  def set(transforms: Transformations) = transforms +=: heldTransformations
  def set(transforms: Transforms) =
     this.transforms.copy(transforms.asInstanceOf[Transforms])

  override def toString =
    s"""
      |===============================================================
      |${transforms}
      |===============================================================
    """
//  override def toString =
//    s"""
//      |===============================================================
//      |Total Transformations in Holder = ${heldTransformations.size}
//      |---------------------------------------------------------------
//      |${heldTransformations mkString "\n"}
//      |===============================================================
//    """
}
