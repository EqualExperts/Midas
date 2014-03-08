/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.model

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

  def processNewConnection(client: Socket, mongo: Socket) = {
    val clientInetAddress = client.getInetAddress
    val newConMsg = s"New connection received from Remote IP: ${clientInetAddress} Remote Port: ${client.getPort}, Local Port: ${client.getLocalPort}"
    logInfo(newConMsg)
    println(newConMsg)

    getApplication(clientInetAddress) match {
      case Some(application) => application.acceptAuthorized(client, mongo)
      case None => rejectUnauthorized(client, mongo)
    }
  }

  private def rejectUnauthorized(client: Socket, mongo: Socket) = {
    logError(s"Client on ${client.getInetAddress} Not Authorized to connect to Midas!")
    client.close()
    logError(s"Unauthorized Client Connection Terminated.")
    mongo.close()
    logError(s"Closing connection with MongoDB on ${mongo.getInetAddress} as Client was Not Authorized to connect to Midas!")
  }

  override def toString = s"""Configuration(Deltas Dir = $deltasDir, Applications = ${applications mkString "," }"""
}

object Configuration {
  val filename = "midas.config"
}
