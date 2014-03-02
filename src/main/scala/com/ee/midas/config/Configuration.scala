package com.ee.midas.config

import java.net.{Socket, InetAddress, URL}
import com.ee.midas.utils.Loggable
import java.io.File
import scala.collection.mutable.Map

class Configuration(deltasDir: URL, private val apps: List[String]) extends Loggable
with ApplicationParsers with Watchable[Configuration] {

  private val parsedApps = Map[Application, ApplicationWatcher](parseApplications: _*)

  private def parseApplications: List[(Application, ApplicationWatcher)] =
    apps map { app =>
      val absoluteAppConfigDir: URL = new File(deltasDir.getPath + app).toURI.toURL
      parse(absoluteAppConfigDir) match {
        case scala.util.Success(app) => app
        case scala.util.Failure(t)   => logError(s"Failed to parse Application ${app} because ${t.getMessage}")
      }
    } collect { case (app: Application) =>
      val appWatcher = new ApplicationWatcher(app)
      (app, appWatcher)
    }

  def applications: List[Application] = parsedApps.map { case (app, _) => app }.toList

  def getApplication(ip: InetAddress): Option[Application] = applications.find(app => app.getNode(ip).isDefined)
  
  private def diffApps(from: Configuration) = {
    val oldApps = parsedApps.keySet
    val newApps = from.parsedApps.keySet
    val common = oldApps intersect newApps
    val add = newApps diff common
    logInfo(s"Applications to be Added $add")
    val remove = oldApps diff common
    logInfo(s"Applications to be Removed $remove")
    (add, remove)
  }

  def update(fromConfig: Configuration): Unit = {
    val (addApps, removeApps) = diffApps(fromConfig)
    parsedApps --= removeApps.map { app =>
      parsedApps(app).stopWatching
      app.stop
      app
    }

    parsedApps ++= addApps.map { app =>
      val watcher = fromConfig.parsedApps(app)
      watcher.startWatching
      (app -> watcher)
    }

    logInfo(s"Total Applications = $parsedApps")
  }

  def stop = parsedApps.foreach { case (app, watcher) =>
    app.stop
    watcher.stopWatching
  }

  def start = parsedApps.foreach { case (app, watcher) =>
    watcher.startWatching
  }

  def processNewConnection(appSocket: Socket, mongoSocket: Socket) = {
    val appInetAddress = appSocket.getInetAddress
    val newConMsg = s"New connection received from Remote IP: ${appInetAddress} Remote Port: ${appSocket.getPort}, Local Port: ${appSocket.getLocalPort}"
    logInfo(newConMsg)
    println(newConMsg)

    getApplication(appInetAddress) match {
      case Some(application) => application.acceptAuthorized(appSocket, mongoSocket)
      case None => rejectUnauthorized(appSocket)
    }
  }

  private def rejectUnauthorized(appSocket: Socket) = {
    logError(s"Client on ${appSocket.getInetAddress} Not Authorized to connect to Midas!")
    appSocket.close()
    logError(s"Unauthorized Client Connection Terminated.")
  }

  override def toString = s"""Configuration(Deltas Dir = $deltasDir, Applications = ${applications mkString "," }"""
}

object Configuration {
  val filename = "midas.config"
}
