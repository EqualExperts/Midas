package com.ee.midas.config

import java.net.{Socket, URI, InetAddress, URL}
import com.ee.midas.utils.Loggable
import java.io.File

final case class Configuration(deltasDir: URL, private val apps: List[String]) extends Loggable with ApplicationParsers {

  private val parsedApps = scala.collection.mutable.Map[URI, (Application, ApplicationWatcher)](parseApplications: _*)

  private def parseApplications: List[(URI, (Application, ApplicationWatcher))] =
    apps map { app =>
      val absoluteAppConfigDir: URL = new File(deltasDir.getPath + app).toURI.toURL
      parse(absoluteAppConfigDir) match {
        case scala.util.Success(app) => (app.configDir.toURI, app)
        case scala.util.Failure(t)   => logError(s"Failed to parse Application ${app} because ${t.getMessage}")
      }
    } collect { case (uri: URI, app: Application) =>
      val appWatcher = new ApplicationWatcher(app)
      uri -> (app, appWatcher)
    }

  def applications: List[Application] = parsedApps.values.map { case (app, _) => app }.toList

  def getApplication(ip: InetAddress): Option[Application] = applications.find(app => app.hasNode(ip))

  def update(application: Application): Unit = {
    val appConfigDir = application.configDir.toURI
    parsedApps(appConfigDir) = (application, new ApplicationWatcher(application))
  }

  def update(newConfiguration: Configuration): Unit = {
    val oldConfig = parsedApps.keySet
    val newConfig = newConfiguration.parsedApps.keySet
    val common = oldConfig intersect newConfig
    val toBeAdded = newConfig diff common
    logInfo(s"Applications to be Added $toBeAdded")
    val toBeRemoved = oldConfig diff common
    logInfo(s"Applications to be Removed $toBeRemoved")

    val newApps = newConfiguration.parsedApps.filter { case (k, v) => toBeAdded.contains(k) }
    toBeRemoved.foreach { k =>
      val (app, appWatcher) = parsedApps(k)
      app.stop
      appWatcher.stopWatching
      val removed = parsedApps.remove(k)
      logInfo(s"Removed $removed")
    }
    parsedApps ++= newApps
    logInfo(s"Total Applications $parsedApps")
  }

  def stop = parsedApps.values.foreach { case (_, watcher) => watcher.stopWatching }

  def start = parsedApps.values.foreach { case (_, watcher) => watcher.startWatching }

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
