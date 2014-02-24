package com.ee.midas.config

trait Watcher[T <: Watchable[T]] {
  def startWatching: Unit
  def stopWatching: Unit
}
