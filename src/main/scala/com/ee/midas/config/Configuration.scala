package com.ee.midas.config

import java.net.{URI, InetAddress, URL}
import com.ee.midas.utils.Loggable
import java.io.File
import scala.util.{Failure, Success}
import scala.collection.mutable.Map
import scala.collection.mutable.MutableList


final case class Configuration(deltasDir: URL, private val apps: List[String]) extends Loggable {

  private val appParsers = new ApplicationParsers {}

  private val appListeners = Map[URI, MutableList[ApplicationListener]]().withDefaultValue(MutableList())

  private def parseApplications: List[(URI, Application)] = {
    val allApps: List[Any] = apps map { app =>
      val absoluteAppConfigDir: URL = new File(deltasDir.getPath + app).toURI.toURL
      logInfo(s"Looking for Application Config in $absoluteAppConfigDir")
      appParsers.parse(absoluteAppConfigDir) match {
        case Success(app) => {
          logInfo(s"Parsed Application Config in ${app.configDir}")
          app
        }
        case Failure(t)   => logWarn(s"Could Not Parse Application $app: ${t.getMessage}")
      }
    }

    val parsed: List[(URI, Application)] = allApps.collect {
      case app: Application => {
        (app.configDir.toURI, app)
      }
    }
    logDebug(s"Parsed Applications ${parsed mkString ","}")
    parsed
  }

  private val parsedApps = scala.collection.mutable.Map(parseApplications: _*)

  def applications: List[Application] = parsedApps.map { case(k, v) => v }.toList

  def hasApplication(ip: InetAddress): Boolean =
    applications.exists(app => app.hasNode(ip))

  def getApplication(ip: InetAddress): Option[Application] =
    applications.find(app => app.hasNode(ip))

  def update(application: Application): Unit = {
    val appConfigDir = application.configDir.toURI
    parsedApps(appConfigDir) = application
    fireAppUpdate(appListeners(appConfigDir), application)
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
      val listeners = appListeners(k)
      fireAppUpdate(listeners, null)
      appListeners.remove(k)
      val removed = parsedApps.remove(k)
      logInfo(s"Removed $removed")
    }
    parsedApps ++= newApps
    logInfo(s"Total Applications $parsedApps")
  }

  private def fireAppUpdate(listeners: MutableList[ApplicationListener], application: Application): Unit =
    listeners.foreach(l => l.onUpdate(application))

  def addApplicationListener(listener: ApplicationListener, appInetAddress: InetAddress) = {
    getApplication(appInetAddress) match {
      case Some(application) => {
        val listeners = appListeners(application.configDir.toURI)
        listeners += listener
        logInfo(s"Added Listener $listener")
      }
      case None => logError(s"Could Not add Listener $listener")
    }
  }

  override def toString = s"""Configuration(Deltas Dir = $deltasDir, Applications = ${applications mkString "," }"""
}

object Configuration {
  val filename = "midas.config"
}
