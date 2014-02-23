package com.ee.midas

import com.ee.midas.config.{ConfigurationParser, Configuration}
import java.net.{BindException, InetAddress, ServerSocket}
import com.ee.midas.utils.{Loggable}
import java.io.File
import java.util.concurrent.TimeUnit

import org.apache.log4j.helpers.FileWatcher

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser with FileWatcher {

  val maxClientConnections = 50
  val configuration: Configuration = parseConfiguration(cmdConfig)
  setupShutdownHook


  private def parseConfiguration(cmdConfig: CmdConfig): Configuration = {
    val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
    parse(deltasDir, Configuration.filename) match {
      case scala.util.Failure(t) => throw new IllegalArgumentException(t)
      case scala.util.Success(configuration) => {
        logDebug(s"Configuration $configuration")
        configuration
      }
    }
  }


  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    val listeningMsg = s"Midas Ready! Listening on IP: ${serverSocket.getInetAddress}, Port ${serverSocket.getLocalPort()} for new connections..."
    logInfo(listeningMsg)
    println(listeningMsg)
    serverSocket.accept()
  }


  private def setupConfigurationWatcher(cmdConfig: CmdConfig, configuration: Configuration) = {
    val midasConfigFile = new File(cmdConfig.baseDeltasDir.getPath + File.separator + Configuration.filename)
    watch(midasConfigFile, 2, TimeUnit.SECONDS) {
      val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
      parse(deltasDir, Configuration.filename) match {
        case scala.util.Failure(t) => throw new IllegalArgumentException(t)
          //todo: revisit this
        case scala.util.Success(newConfiguration) => configuration.update(newConfiguration)
      }
    }
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

  def start = {
    val startingMsg = s"Starting Midas on ${cmdConfig.midasHost}, port ${cmdConfig.midasPort}..."
    logInfo(startingMsg)
    println(startingMsg)
    setupConfigurationWatcher(cmdConfig, configuration)
    configuration.start
    try {
      val midasSocket = new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
      while (true) {
        val appSocket = waitForNewConnectionOn(midasSocket)
        configuration.processNewConnection(appSocket, cmdConfig.mongoHost, cmdConfig.mongoPort)
      }
    } catch {
      case e: BindException =>
        val errMsg = s"Cannot Start Midas on IP => ${cmdConfig.midasHost}, Port => ${cmdConfig.midasPort}.  Please Check Your Server IP or Port."
        logError(errMsg)
        println(errMsg)
    }
  }
}
