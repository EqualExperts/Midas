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

package com.ee.midas

import com.ee.midas.model.{ConfigurationWatcher, ConfigurationParser, Configuration}
import java.net.{Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.Loggable
import java.io.File
import scala.util.Try

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser {

  private val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
  private val configuration: Configuration = parseConfiguration(cmdConfig)
  private val watcher = new ConfigurationWatcher(configuration, cmdConfig.baseDeltasDir)
  private var isRunning = false
  private var serverSocket: ServerSocket = null

  private def parseConfiguration(cmdConfig: CmdConfig): Configuration =
    parse(deltasDir, Configuration.filename) match {
      case scala.util.Failure(t) => throw new IllegalArgumentException(t)
      case scala.util.Success(configuration) => {
        logDebug(s"Configuration $configuration")
        configuration
      }
    }

  def isActive = isRunning

  private def waitForNewConnectionOn(serverSocket: ServerSocket): Try[Socket] = Try {
    val listeningMsg = s"Midas Ready! Listening on IP: ${serverSocket.getInetAddress}, Port ${serverSocket.getLocalPort()} for new connections..."
    logInfo(listeningMsg)
    println(listeningMsg)
    serverSocket.accept()
  }

  protected def createMongoSocket: Try[Socket] =
    Try {
      new Socket(cmdConfig.mongoHost, cmdConfig.mongoPort)
    }
  
  protected def createServerSocket: Try[ServerSocket] =
    Try {
      val maxClientConnections = 50
      new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
    }

  def stop = {
    logInfo("Midas server shutdown requested. Initiating closure.")
    isRunning = false
    watcher.stopWatching
    configuration.stop
    serverSocket.close()
  }

  private def processNewConnection(clientSocket: Socket) = {
    val clientIp = clientSocket.getInetAddress
    createMongoSocket match {
      case scala.util.Failure(t) =>
        val errMsg =
          s"""
          | MongoDB on ${cmdConfig.mongoHost}:${cmdConfig.mongoPort} is not available!
          | Terminating connection from ${clientIp}, Please retry later.
          """.stripMargin
        println(errMsg)
        logError(errMsg)
        clientSocket.close()

      case scala.util.Success(mongoSocket) =>
        configuration.processNewConnection(clientSocket, mongoSocket)
    }
  }

  def start = {
    val startingMsg = s"Starting Midas on ${cmdConfig.midasHost}, port ${cmdConfig.midasPort}..."
    logInfo(startingMsg)
    println(startingMsg)

    configuration.start
    watcher.startWatching
    createServerSocket match {
      case scala.util.Failure(t) =>
        val errMsg = s"Cannot Start Midas on IP => ${cmdConfig.midasHost}, Port => ${cmdConfig.midasPort}.  Please Check Your Server IP or Port."
        logError(errMsg)
        println(errMsg)
  
      case scala.util.Success(server) =>
        serverSocket = server
        isRunning = true
        while (isRunning) {
          waitForNewConnectionOn(server) match {
            case scala.util.Success(client) if(isRunning) =>
                processNewConnection(client)

            case scala.util.Failure(t) =>
              val errMsg = s"Cannot accept connection on IP => ${cmdConfig.midasHost}, Port => ${cmdConfig.midasPort}. Server currently closed."
              logError(errMsg)
              println(errMsg)
              stop
          }
        }
    }
  }

}
