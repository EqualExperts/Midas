package com.ee.midas.transform

object TransformsHolder {
  private val transforms = new Transformations

  def get =
    this.synchronized {
      transforms
    }

  def set(newTransforms: Transforms) =
    this.synchronized {
      this.transforms.update(newTransforms)
    }

  override def toString =
    s"""
      |===============================================================
      |${transforms}
      |===============================================================
    """
}
