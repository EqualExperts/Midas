package com.ee.midas

import com.ee.midas.config.{ConfigurationWatcher, ConfigurationParser, Configuration}
import java.net.{Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.Loggable
import java.io.File
import scala.util.Try

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser {

  private val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
  private val configuration: Configuration = parseConfiguration(cmdConfig)
  private val watcher = new ConfigurationWatcher(configuration, cmdConfig.baseDeltasDir)
  private var isRunning = false
  private lazy val serverSocket = createServerSocket

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
    serverSocket.get.close()
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
    serverSocket match {
      case scala.util.Failure(t) =>
        val errMsg = s"Cannot Start Midas on IP => ${cmdConfig.midasHost}, Port => ${cmdConfig.midasPort}.  Please Check Your Server IP or Port."
        logError(errMsg)
        println(errMsg)
  
      case scala.util.Success(serverSocket) =>
        isRunning = true
        while (isRunning) {
          waitForNewConnectionOn(serverSocket) match {
            case scala.util.Success(clientSocket) if(isRunning) =>
                processNewConnection(clientSocket)

            case scala.util.Failure(t) =>
              val errMsg = s"Cannot accept connection on IP => ${cmdConfig.midasHost}, Port => ${cmdConfig.midasPort}. Server currently closed."
              logError(errMsg)
              println(errMsg)
          }
        }
    }
      isRunning = false
  }

}
