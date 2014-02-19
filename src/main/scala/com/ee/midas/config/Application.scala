package com.ee.midas.config

import java.net.{URL, InetAddress}
import com.ee.midas.transform.{Transformer, TransformType}
import org.bson.BSONObject

final case class ChangeSet(number: Long) {
  require(number >= 0L)

  override def toString = s"ChangeSet($number)"
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet) {
  override def toString = s"Node($name, $ip, $changeSet)"
}

case class Application(configDir: URL, name: String, mode: TransformType, nodes: List[Node], var transformer: Transformer = Transformer.empty) {
  def hasNode(ip: InetAddress): Boolean =
    nodes.exists(node => node.ip == ip)
  
  def getNode(ip: InetAddress): Option[Node] =
    nodes.find(node => node.ip == ip)
  
  def changeSet(ip: InetAddress): Option[ChangeSet] = getNode(ip) match {
    case None => None
    case Some(Node(_, _, cs)) => Some(cs)
  }

  def transformRequest(document: BSONObject, fullCollectionName: String, ip: InetAddress): BSONObject =
    changeSet(ip) match {
      case Some(ChangeSet(cs)) => transformer.transformRequest(document, cs, fullCollectionName)
      case None => document
    }

  def transformResponse(document: BSONObject, fullCollectionName: String): BSONObject =
    transformer.transformResponse(document, fullCollectionName)

  override def toString = s"""Application(configDir = ${configDir.toURI}, name = $name, mode = $mode, nodes = ${nodes mkString "," }, $transformer"""
}

