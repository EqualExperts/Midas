package com.ee.midas.config

import java.net.{URI, InetAddress, URL}
import com.ee.midas.utils.Loggable
import java.io.File
import scala.util.{Failure, Success}


final case class Configuration(deltasDir: URL, private val apps: List[String]) extends Loggable {

  private val appParsers = new ApplicationParsers {}

  private var appListeners = scala.collection.mutable.MutableList[ApplicationListener]()

  private def parseApplications: List[(URI, Application)] = apps map { app =>
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
    case app: Application => (app.configDir.toURI, app)
  }

  private val parsedApps = scala.collection.mutable.Map(parseApplications: _*)

  def applications: List[Application] = parsedApps.map { case(k, v) => v }.toList

  def hasApplication(ip: InetAddress): Boolean =
    applications.exists(app => app.hasNode(ip))

  def getApplication(ip: InetAddress): Option[Application] =
    applications.find(app => app.hasNode(ip))

  def update(application: Application) = {
    parsedApps(application.configDir.toURI) = application
    //fire listeners that are listening to this application
  }

  def addApplicationListener(listener: ApplicationListener, appInetAddress: InetAddress) = {
//    appListeners +=

  }

  override def toString = s"""Configuration($deltasDir, ${applications mkString "," }"""
}
