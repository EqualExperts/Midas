package com.ee.midas.hotdeploy

trait DeployableInjector[T] {
  def injectState(t: T)
}
