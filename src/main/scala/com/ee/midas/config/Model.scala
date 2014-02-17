package com.ee.midas.config

import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType
import java.io.File
import scala.util.{Success, Failure}
import com.ee.midas.utils.Loggable

final case class ChangeSet(number: Long) {
  require(number >= 0L)
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet)

final case class Application(name: String, mode: TransformType, nodes: List[Node]) {
  def hasNode(ip: InetAddress): Boolean =
    nodes.exists(node => node.ip == ip)
  
  def getNode(ip: InetAddress): Option[Node] =
    nodes.find(node => node.ip == ip)
  
  def changeSet(ip: InetAddress): Option[ChangeSet] = getNode(ip) match {
    case None => None
    case Some(Node(_, _, cs)) => Some(cs)
  }
}

final case class Configuration(deltasDir: URL, appNames: List[String]) extends Loggable {
  val appConfigFileExtn = ".midas"
  private val appParsers = new ApplicationParsers {}
  private val parsedApps: List[Application] = parseApps
  
  private def parseApps = appNames map { appName =>
    val appDir = appName
    val appConfig: URL = new URL(s"${deltasDir}${appDir}${File.separator}${appName}${appConfigFileExtn}")
    logInfo(s"Looking for Application Config in $appConfig")
    appParsers.parse(appConfig) match {
      case Success(app) => app
      case Failure(t)   => logWarn(s"Could Not Parse Application $appName: ${t.getMessage}")
    }
  } collect {
    case app: Application => app
  }

  def hasApplication(ip: InetAddress): Boolean = 
    parsedApps.exists(app => app.hasNode(ip))

  def hasApplication(name: String): Boolean =
    parsedApps.exists(app => app.name == name)

  def getApplication(ip: InetAddress): Option[Application] = 
    parsedApps.find(app => app.hasNode(ip))
}
