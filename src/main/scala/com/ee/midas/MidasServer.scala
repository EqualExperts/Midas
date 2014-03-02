package com.ee.midas

import com.ee.midas.config.{ConfigurationWatcher, ConfigurationParser, Configuration}
import java.net.{Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.{Loggable}
import java.io.File
import scala.util.Try
import scala.util.control.Breaks._

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser {

  private val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
  private val configuration: Configuration = parseConfiguration(cmdConfig)
  private val watcher = new ConfigurationWatcher(configuration, cmdConfig.baseDeltasDir)
  var stopApplication = false
  var isRunning = false
  setupShutdownHook

  private def parseConfiguration(cmdConfig: CmdConfig): Configuration =
    parse(deltasDir, Configuration.filename) match {
      case scala.util.Failure(t) => throw new IllegalArgumentException(t)
      case scala.util.Success(configuration) => {
        logDebug(s"Configuration $configuration")
        configuration
      }
    }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    val listeningMsg = s"Midas Ready! Listening on IP: ${serverSocket.getInetAddress}, Port ${serverSocket.getLocalPort()} for new connections..."
    logInfo(listeningMsg)
    println(listeningMsg)
    serverSocket.accept()
  }

  private def setupShutdownHook = sys.ShutdownHookThread {
    val forceStopMsg = "User Forced Stop on Midas...Closing Open Connections"
    logInfo(forceStopMsg)
    println(forceStopMsg)
    watcher.stopWatching
    configuration.stop
    val shutdownMsg = "Midas Shutdown Complete!"
    logInfo(shutdownMsg)
    println(shutdownMsg)
  }

  def createMongoSocket: Try[Socket] =
    Try {
      new Socket(cmdConfig.mongoHost, cmdConfig.mongoPort)
    }
  
  def createServerSocket: Try[ServerSocket] = 
    Try {
      val maxClientConnections = 50
      new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
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
  
      case scala.util.Success(serverSocket) =>
       breakable {
        while (true) {
          isRunning = true
          val clientSocket = waitForNewConnectionOn(serverSocket)
          if(stopApplication){
            break
          }
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
      }
      stopApplication = false
      isRunning = false
      logInfo("Midas has halted.")
    }
  }

  def stop = {
    stopApplication = true
  }

}
