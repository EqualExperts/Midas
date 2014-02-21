package com.ee.midas.config

import java.net.{InetAddress}

final case class ChangeSet(number: Long) {
  require(number >= 0L)

  override def toString = s"ChangeSet($number)"
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet) {
  override def toString = s"Node($name, $ip, $changeSet)"
}

