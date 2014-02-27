package com.ee.midas.utils

class SynchronizedHolder[T] private (private var t: T) {
  def get = this.synchronized {
      t
    }

  def apply(newT: T) =
    this.synchronized {
      this.t = newT
    }
}

object SynchronizedHolder {
  def apply[T](t: T) = new SynchronizedHolder[T](t)
}

