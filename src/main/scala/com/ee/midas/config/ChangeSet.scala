package com.ee.midas.config

final case class ChangeSet(number: Long = 0) {
  require(number >= 0L)

  override def toString = s"ChangeSet($number)"
}


