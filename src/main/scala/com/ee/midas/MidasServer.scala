package com.ee.midas

import com.ee.midas.config.{ConfigurationParser, ApplicationParsers, Configuration, Application}
import java.net.{ConnectException, Socket, InetAddress, ServerSocket}
import com.ee.midas.utils.{Accumulator, DirectoryWatcher, Loggable}
import java.io.File
import java.nio.file.StandardWatchEventKinds._
import scala.Some
import com.ee.midas.transform.Transformer
import com.ee.midas.pipes.{SocketConnector, DuplexPipe}
import com.ee.midas.interceptor.{ResponseInterceptor, RequestInterceptor, MessageTracker}
import java.util.concurrent.TimeUnit
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import SocketConnector._
import org.apache.log4j.helpers.FileWatcher

class MidasServer(cmdConfig: CmdConfig) extends Loggable with ConfigurationParser with DeltasProcessor with FileWatcher {
  val translator = new Translator[Transformer](new Reader, new ScalaGenerator)
  val accumulatePipe = Accumulator[DuplexPipe](Nil)
  val accumulateWatcher = Accumulator[DirectoryWatcher](Nil)
  val maxClientConnections = 50

  setupShutdownHook


  private def processNewConnection(appSocket: Socket, cmdConfig: CmdConfig, configuration: Configuration) = {
    val appInetAddress = appSocket.getInetAddress
    val newConMsg = s"New connection received from Remote IP: ${appInetAddress} Remote Port: ${appSocket.getPort}, Local Port: ${appSocket.getLocalPort}"
    logInfo(newConMsg)
    println(newConMsg)

    configuration.getApplication(appInetAddress) match {
      case Some(application) =>
        setupDuplexPipe(appSocket, cmdConfig, configuration, application) match {
          case Some(duplexPipe) =>
            accumulatePipe(duplexPipe)
            duplexPipe.start
            val pipeReadyMsg = s"Setup Pipes for New Connection, ready to receive traffic on $duplexPipe"
            logInfo(pipeReadyMsg)
            println(pipeReadyMsg)
          case None =>
        }
      case None => rejectUnauthorized(appSocket)
    }
  }

  private def parseConfiguration(cmdConfig: CmdConfig): Configuration = {
    val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
    parse(deltasDir, Configuration.filename) match {
      case scala.util.Failure(t) => throw new IllegalArgumentException(t)
      case scala.util.Success(configuration) => {
        logDebug(s"Configuration $configuration")
        configuration.applications.foreach { application =>
          val transformer = processDeltaFiles(application)
          logDebug(s"Transformer => $transformer")
          application.transformer = transformer
          val watcher = setupAppDirectoryWatcher(configuration, application)
          accumulateWatcher(watcher)
          watcher.start
        }
        configuration
      }
    }
  }

  private def reparse(application: Application): Option[Application] = {
    val appParsers = new ApplicationParsers { }
    logInfo(s"Reparsing Updated Application Config for ${application.name}")
    appParsers.parse(application.configDir) match {
      case scala.util.Success(updatedApp) => {
        val newTransformer = processDeltaFiles(updatedApp)
        updatedApp.transformer = newTransformer
        logInfo(s"Installed New Transformer for Updated Application ${updatedApp.name} => $newTransformer")
        Some(updatedApp)
      }
      case scala.util.Failure(e) => {
        logError(s"Reparsing Updated Application Config for ${application.name} Failed!! => ${e.getMessage})")
        None
      }
    }
  }

  private def setupAppDirectoryWatcher(configuration: Configuration, application: Application): DirectoryWatcher = {
    val waitBeforeProcessing = 100
    val dirWatchMsg = s"Setting up Directory Watcher for Application ${application.name} on ${application.configDir}..."
    println(dirWatchMsg)
    logInfo(dirWatchMsg)
    new DirectoryWatcher(application.configDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY),
      waitBeforeProcessing, stopWatchingOnException = false)(watchEvents => {
      watchEvents.foreach { watchEvent =>
        logInfo(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
      }

      reparse(application) match {
        case Some(updatedApp) => {
          configuration.update(updatedApp)
          logError(s"Updated Configuration for ${application.name}")
        }
        case None => logError(s"Will Continue To Use Old Application Config for ${application}")
      }
    })
  }

  private def processDeltaFiles(application: Application): Transformer = {
    val processinngDeltaFilessMsg =
      s"Processing Delta Files for Application ${application.name} in mode ${application.mode} from Dir ${application.configDir}"
    println(processinngDeltaFilessMsg)
    logInfo(processinngDeltaFilessMsg)
    processDeltas(translator, application.mode, application.configDir)
  }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    val listeningMsg = s"Midas Ready! Listening on port ${serverSocket.getLocalPort()} for new connections..."
    logInfo(listeningMsg)
    println(listeningMsg)
    serverSocket.accept()
  }

  private def setupDuplexPipe(appSocket: Socket, cmdConfig: CmdConfig, configuration: Configuration, application: Application): Option[DuplexPipe] = {
    val appInetAddress = appSocket.getInetAddress
    val mongoHost = cmdConfig.mongoHost
    val mongoPort = cmdConfig.mongoPort
    try {
      val mongoSocket = new Socket(mongoHost, mongoPort)
      val tracker = new MessageTracker()
      val requestInterceptor = new RequestInterceptor(tracker, application, appInetAddress)
      val responseInterceptor = new ResponseInterceptor(tracker, application, appInetAddress)
      configuration.addApplicationListener(requestInterceptor, appInetAddress)
      configuration.addApplicationListener(responseInterceptor, appInetAddress)
      Some(appSocket <|==|> (mongoSocket, requestInterceptor, responseInterceptor))
    }
    catch {
      case e: ConnectException => {
        val errMsg = s"MongoDB on ${mongoHost}:${mongoPort} is not available!  Terminating connection from ${appInetAddress}, Please retry later."
        println(errMsg)
        logError(errMsg)
        appSocket.close()
        None
      }
    }
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
        case scala.util.Success(newConfiguration) => configuration.update(newConfiguration)
      }
    }
  }

  private def setupShutdownHook = sys.ShutdownHookThread {
    val watchers = accumulateWatcher(null)
    watchers.foreach(_.stopWatching)
    val pipes = accumulatePipe(null)
    val forceStopMsg = "User Forced Stop on Midas...Closing Open Connections"
    logInfo(forceStopMsg)
    println(forceStopMsg)
    pipes filter(_.isActive) map(_.forceStop)
    val shutdownMsg = "Midas Shutdown Complete!"
    logInfo(shutdownMsg)
    println(shutdownMsg)
  }

  def start = {
    val startingMsg = s"Starting Midas on ${cmdConfig.midasHost}, port ${cmdConfig.midasPort}..."
    logInfo(startingMsg)
    println(startingMsg)

    val configuration: Configuration = parseConfiguration(cmdConfig)
    setupConfigurationWatcher(cmdConfig, configuration)
    val midasSocket = new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
    while (true) {
      val appSocket = waitForNewConnectionOn(midasSocket)
      processNewConnection(appSocket, cmdConfig, configuration)
    }
  }
}
