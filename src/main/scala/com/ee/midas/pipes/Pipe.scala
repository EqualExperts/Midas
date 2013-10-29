package com.ee.midas.pipes

trait Pipe extends Startable with Stoppable {
  val name: String
  def isActive: Boolean
  def inspect: Unit
}
