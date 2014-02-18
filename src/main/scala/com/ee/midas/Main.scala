package com.ee.midas


import com.ee.midas.pipes.{SocketConnector, DuplexPipe}
import java.net._
import com.ee.midas.utils.{DirectoryWatcher, Accumulator, Loggable}
import com.ee.midas.interceptor.{MessageTracker, RequestInterceptor, ResponseInterceptor}
import com.ee.midas.dsl.generator.{ScalaGenerator}
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.Translator
import java.io.{File}
import com.ee.midas.transform.{Transformer}
import java.nio.file.StandardWatchEventKinds._
import com.ee.midas.config.{Application, Configuration, ApplicationParsers, ConfigurationParser}

object Main extends App with Loggable with ConfigurationParser with DeltasProcessor {
  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {
    val accumulatePipe = Accumulator[DuplexPipe](Nil)
    val accumulateWatcher = Accumulator[DirectoryWatcher](Nil)
    sys.ShutdownHookThread {
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
    
    var configuration: Configuration = null
    CLIParser.parse(args) match {
      case Some(cmdConfig) =>
        val waitBeforeProcessing = 100
        //Todo: tweak scala style rule so that we don't have to give types when declaring variables.
        val deltasDir = new File(cmdConfig.baseDeltasDir.getPath).toURI.toURL
        val startingMsg = s"Starting Midas on ${cmdConfig.midasHost}, port ${cmdConfig.midasPort}..."
        logInfo(startingMsg)
        println(startingMsg)
        
        val translator = new Translator[Transformer](new Reader, new ScalaGenerator)
        parse(deltasDir) match {
          case scala.util.Failure(t) => throw new IllegalArgumentException(t)
          case scala.util.Success(config) => {
            configuration = config        
            config.applications.foreach { application =>
              val processingDeltaFilesMsg = s"Processing Delta Files for Application ${application.name} in mode ${application.mode} from Dir ${application.configDir}"
              println(processingDeltaFilesMsg)
              logInfo(processingDeltaFilesMsg)
              val initialTransformer = processDeltas(translator, application.mode, application.configDir)
              logInfo(s"Initial Transformer => $initialTransformer")
              application.transformer = initialTransformer
              val dirWatchMsg = s"Setting up Directory Watcher for Application ${application.name} on ${application.configDir}..."
              println(dirWatchMsg)
              logInfo(dirWatchMsg)
              val watcher = new DirectoryWatcher(application.configDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY),
                waitBeforeProcessing, stopWatchingOnException = false)(watchEvents => {
                watchEvents.foreach { watchEvent =>
                  logInfo(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
                }
                new ApplicationParsers {
                  parse(application.configDir) match {
                    case scala.util.Success(updatedApp) => {
                      logInfo(s"Processing Deltas for Updated Application ${updatedApp.name}...")
                      val newTransformer = processDeltas(translator, updatedApp.mode, application.configDir)
                      updatedApp.transformer = newTransformer
                      logInfo(s"Installed New Transforms for Updated Application ${updatedApp.name} => $newTransformer")
                      configuration.update(updatedApp)
                    }
                    case scala.util.Failure(e) => {
                      logError(s"Parsing Updated Application Config for ${application.name} failed => ${e.getMessage})")
                      logError(s"Will Continue To Use Old Application Config for ${application}")
                    }
                  }
                }
              })
              accumulateWatcher(watcher)
              watcher.start
            }
          }
        }

        val midasSocket = new ServerSocket(cmdConfig.midasPort, maxClientConnections, InetAddress.getByName(cmdConfig.midasHost))
        import SocketConnector._
        while (true) {
          val appSocket = waitForNewConnectionOn(midasSocket)
          val appInetAddress = appSocket.getInetAddress
          val newConMsg = s"New connection received from Remote IP: ${appInetAddress} Remote Port: ${appSocket.getPort}, Local Port: ${appSocket.getLocalPort}"
          logInfo(newConMsg)
          println(newConMsg)
          try {
            val mongoDB = new Socket(cmdConfig.mongoHost, cmdConfig.mongoPort)
            val tracker = new MessageTracker()
            val unconfiguredApp = Application.unconfigured
            val application = if(configuration.hasApplication(appInetAddress)) {
              configuration.getApplication(appInetAddress) match {
                case Some(app) => app
                case None => unconfiguredApp
              }
            } else {
              unconfiguredApp
            }
            val requestInterceptor = new RequestInterceptor(tracker, application, appInetAddress)
            val responseInterceptor = new ResponseInterceptor(tracker, application)
            configuration.addApplicationListener(requestInterceptor, appInetAddress)
            configuration.addApplicationListener(responseInterceptor, appInetAddress)
            val duplexPipe = appSocket <|==|> (mongoDB, requestInterceptor, responseInterceptor)
            duplexPipe.start
            val pipeReadyMsg = s"Setup All Connections, ready to receive traffic on $duplexPipe"
            logInfo(pipeReadyMsg)
            println(pipeReadyMsg)
            accumulatePipe(duplexPipe)
          }
          catch {
            case e: ConnectException =>
              val errMsg = s"MongoDB on ${cmdConfig.mongoHost}:${cmdConfig.mongoPort} is not available!"
              println(errMsg)
              logError(errMsg)
              appSocket.close()
          }
        }

      case None =>
    }
  }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    val listeningMsg = s"Midas Ready! Listening on port ${serverSocket.getLocalPort()} for new connections..."
    logInfo(listeningMsg)
    println(listeningMsg)
    serverSocket.accept()
  }
}
