package com.ee.midas.config

trait Watchable[T] {
   def update(newWatchable: T)
}
