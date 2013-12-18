package com.ee.midas.hotdeploy

trait Deployable[T] {
  def injectState(t: T)
}
