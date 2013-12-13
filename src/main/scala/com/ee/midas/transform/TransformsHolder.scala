package com.ee.midas.transform

object TransformsHolder {
  private val transforms = new Transformations

  def get = transforms

  def set(transforms: Transforms) =
     this.transforms.update(transforms.asInstanceOf[Transforms])

  override def toString =
    s"""
      |===============================================================
      |${transforms}
      |===============================================================
    """
}
