package com.ee.midas.hotdeploy

trait DeployableHolder[T <: Deployable[T]] {
  private val deployable = createDeployable

  def get =
    this.synchronized {
      deployable
    }

  def set(newT: T) =
    this.synchronized {
      this.deployable.injectState(newT)
    }
  
  def createDeployable: T

  override def toString =
    s"""
      |===============================================================
      |${deployable}
      |===============================================================
    """
}
