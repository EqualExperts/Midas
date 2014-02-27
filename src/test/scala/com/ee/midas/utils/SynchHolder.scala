package com.ee.midas.utils

class SynchHolder[T](private var t: T) {
  def get = this.synchronized {
      t
    }

  def set(newT: T) =
    this.synchronized {
      this.t = newT
    }
}

