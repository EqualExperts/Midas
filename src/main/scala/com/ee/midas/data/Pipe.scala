package com.ee.midas.data

trait Pipe extends Startable with Stoppable {
  val name: String
  def isActive: Boolean
  def inspect: Unit
}
