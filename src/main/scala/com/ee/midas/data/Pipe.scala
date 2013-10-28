package com.ee.midas.data

trait Pipe extends Startable {
  val name: String
  def stop: Unit
  def forceStop: Unit
  def isActive: Boolean
  def dump: Unit
}
