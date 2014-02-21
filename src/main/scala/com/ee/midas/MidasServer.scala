package com.ee.midas

import com.ee.midas.config.{ConfigurationParser, Configuration}
import java.net.{Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.{Loggable}
import java.io.File
import scala.Some
import java.util.concurrent.TimeUnit

import org.apache.log4j.helpers.FileWatcher

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser with FileWatcher {

  val maxClientConnections = 50
  val configuration: Configuration = parseConfiguration(cmdConfig)
  setupShutdownHook


  private def processNewConnection(appSocket: Socket, cmdConfig: CmdConfig, configuration: Configuration) = {
    val appInetAddress = appSocket.getInetAddress
    val newConMsg = s"New connection received from Remote IP: ${appInetAddress} Remote Port: ${appSocket.getPort}, Local Port: ${appSocket.getLocalPort}"
    logInfo(newConMsg)
    println(newConMsg)

    configuration.getApplication(appInetAddress) match {
      case Some(application) => application.startDuplexPipe(appSocket, cmdConfig)
      case None => rejectUnauthorized(appSocket)
    }
  }

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
    val listeningMsg = s"Midas Ready! Listening on port ${serverSocket.getLocalPort()} for new connections..."
    logInfo(listeningMsg)
    println(listeningMsg)
    serverSocket.accept()
  }


  private def rejectUnauthorized(appSocket: Socket) = {
    logError(s"Client on ${appSocket.getInetAddress} Not authorized to connect to Midas!")
    appSocket.close()
    logError(s"Client Socket Closed.")
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
    configuration.stopApplications
    val shutdownMsg = "Midas Shutdown Complete!"
    logInfo(shutdownMsg)
    println(shutdownMsg)
  }

  def start = {
    val startingMsg = s"Starting Midas on ${cmdConfig.midasHost}, port ${cmdConfig.midasPort}..."
    logInfo(startingMsg)
    println(startingMsg)
    setupConfigurationWatcher(cmdConfig, configuration)
    configuration.startApplications
    val midasSocket = new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
    while (true) {
      val appSocket = waitForNewConnectionOn(midasSocket)
      processNewConnection(appSocket, cmdConfig, configuration)
    }
  }
}
