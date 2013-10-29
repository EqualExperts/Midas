package com.ee.midas.data

trait Stoppable {
  def stop: Unit
  def forceStop: Unit
}
