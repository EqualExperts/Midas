package com.ee.midas.config

import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType
import java.io.File
import scala.util.{Success, Failure}
import com.ee.midas.utils.Loggable

final case class ChangeSet(number: Long) {
  require(number >= 0L)

  override def toString = s"ChangeSet($number)"
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet) {
  override def toString = s"Node($name, $ip, $changeSet)"
}

final case class Application(configDir: URL, name: String, mode: TransformType, nodes: List[Node]) {
  def hasNode(ip: InetAddress): Boolean =
    nodes.exists(node => node.ip == ip)
  
  def getNode(ip: InetAddress): Option[Node] =
    nodes.find(node => node.ip == ip)
  
  def changeSet(ip: InetAddress): Option[ChangeSet] = getNode(ip) match {
    case None => None
    case Some(Node(_, _, cs)) => Some(cs)
  }

  override def toString = s"""Application($configDir, $name, $mode, ${nodes mkString "," }"""
}

final case class Configuration(deltasDir: URL, private val apps: List[String]) extends Loggable {
  
  private val appParsers = new ApplicationParsers {}
  val applications = apps map { app =>
    val absoluteAppConfigDir: URL = new File(deltasDir.getPath + app).toURI.toURL
    logInfo(s"Looking for Application Config in $absoluteAppConfigDir")
    appParsers.parse(absoluteAppConfigDir) match {
      case Success(app) => {
        logInfo(s"Parsed Application Config in ${app.configDir}")
        app
      }
      case Failure(t)   => logWarn(s"Could Not Parse Application $app: ${t.getMessage}")
    }
  } collect {
    case app: Application => app
  }

  def hasApplication(ip: InetAddress): Boolean = 
    applications.exists(app => app.hasNode(ip))

  def hasApplication(name: String): Boolean =
    applications.exists(app => app.name == name)

  def getApplication(ip: InetAddress): Option[Application] =
    applications.find(app => app.hasNode(ip))

  override def toString = s"""Configuration($deltasDir, ${applications mkString "," }"""
}
