package com.ee.midas.pipes

trait Stoppable {
  def stop: Unit
  def forceStop: Unit
}
