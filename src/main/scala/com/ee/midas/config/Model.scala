package com.ee.midas.config

import java.net.InetAddress
import com.ee.midas.transform.TransformType

final case class ChangeSet(number: Long) {
  require(number >= 0L)
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet)

final case class Application(name: String, mode: TransformType, nodes: List[Node]) {
  def hasNode(ip: InetAddress): Boolean =
    nodes.exists(node => node.ip == ip)
  
  def getNode(ip: InetAddress): Option[Node] =
    nodes.find(node => node.ip == ip)
  
  def changeSet(ip: InetAddress): Option[ChangeSet] = nodes.filter(node => node.ip == ip) match {
    case Nil => None
    case node :: remainingNodes => Option(node.changeSet)
  }
}

final case class Configuration (applications: List[Application]) {
  def hasApplication(ip: InetAddress): Boolean = 
    applications.exists(app => app.hasNode(ip))
  
  def getApplication(ip: InetAddress): Option[Application] = 
    applications.find(app => app.hasNode(ip))

}
