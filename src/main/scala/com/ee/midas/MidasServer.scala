package com.ee.midas

import com.ee.midas.config.{ConfigurationWatcher, ConfigurationParser, Configuration}
import java.net.{Socket, BindException, InetAddress, ServerSocket}
import com.ee.midas.utils.{Loggable}
import java.io.File
import java.util.concurrent.TimeUnit

import org.apache.log4j.helpers.FileWatcher
import scala.util.Try

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser {

  val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
  val configuration: Configuration = parseConfiguration(cmdConfig)
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
    configuration.stop
    val shutdownMsg = "Midas Shutdown Complete!"
    logInfo(shutdownMsg)
    println(shutdownMsg)
  }

  def createMongoSocket(mongoHost: String, mongoPort: Int): Try[Socket] =
    Try {
      new Socket(mongoHost, mongoPort)
    }

  def start = {
    val maxClientConnections = 50
    val startingMsg = s"Starting Midas on ${cmdConfig.midasHost}, port ${cmdConfig.midasPort}..."
    logInfo(startingMsg)
    println(startingMsg)
    new ConfigurationWatcher(configuration, cmdConfig.baseDeltasDir)
    configuration.start
    try {
      val midasSocket = new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
      while (true) {
        val appSocket = waitForNewConnectionOn(midasSocket)
        val appIp = appSocket.getInetAddress
        val (mongoHost, mongoPort) = (cmdConfig.mongoHost, cmdConfig.mongoPort)
        createMongoSocket(mongoHost, mongoPort) match {
          case scala.util.Success(mongoSocket) =>
            configuration.processNewConnection(appSocket, mongoSocket)
          case scala.util.Failure(t) =>
            val errMsg = s"MongoDB on ${mongoHost}:${mongoPort} is not available!  Terminating connection from ${appIp}, Please retry later."
            println(errMsg)
            logError(errMsg)
            appSocket.close()
        }
      }
    } catch {
      case e: BindException =>
        val errMsg = s"Cannot Start Midas on IP => ${cmdConfig.midasHost}, Port => ${cmdConfig.midasPort}.  Please Check Your Server IP or Port."
        logError(errMsg)
        println(errMsg)
    }
  }
}
