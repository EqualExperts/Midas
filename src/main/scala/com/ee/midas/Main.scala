package com.ee.midas


import com.ee.midas.pipes.{SocketConnector, DuplexPipe}
import java.net._
import com.ee.midas.utils.{DirectoryWatcher, Accumulator, Loggable}
import com.ee.midas.interceptor.{MessageTracker, RequestInterceptor, ResponseInterceptor}
import com.ee.midas.dsl.generator.{ScalaGenerator}
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.Translator
import java.io.{File}
import com.ee.midas.transform.{Transformer, Transforms}
import java.nio.file.StandardWatchEventKinds._
import com.ee.midas.config.{ApplicationParsers}

object Main extends App with Loggable with ApplicationParsers with DeltasProcessor {
  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {
    CLIParser.parse(args) match {
      case Some(config) =>
        val waitBeforeProcessing = 100

        val midasConfigURL = config.midasConfig
        val app = parse(midasConfigURL).get
        val mode = app.mode
        val modeMsg = s"Starting Midas in ${mode} mode...on ${config.midasHost}, port ${config.midasPort}"
        logInfo(modeMsg)
        println(modeMsg)

        //Todo: tweak scala style rule so that we don't have to give types when declaring variables.
        val appDir = app.name
        val deltasDirURL: URL = deltasDir(config, appDir)
        val processingDeltaFilesMsg = s"Processing Delta Files...from Dir ${deltasDirURL}"
        println(processingDeltaFilesMsg)
        logInfo(processingDeltaFilesMsg)
        val translator = new Translator[Transforms](new Reader, new ScalaGenerator)
        val initialTransforms = processDeltas(translator, app.mode, deltasDirURL)
        logInfo(s"Initial Transforms => $initialTransforms")
        val transformer = new Transformer(initialTransforms, app)
      
        val dirWatchMsg = s"Setting up Directory Watcher for ${config.baseDeltasDir}..."
        println(dirWatchMsg)
        logInfo(dirWatchMsg)
        val watcher = new DirectoryWatcher(config.baseDeltasDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY),
          waitBeforeProcessing, stopWatchingOnException = false)(watchEvents => {
          watchEvents.foreach {watchEvent =>
            logInfo(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
          }
          parse(midasConfigURL) match {
            case scala.util.Success(app) => {
              transformer.updateApplication(app)
              val deltasDirURL = deltasDir(config, appDir)
              val newTransforms = processDeltas(translator, app.mode, deltasDirURL)
              logInfo(s"New Transforms => $newTransforms")
              transformer.updateTransforms(newTransforms)
            }
            case scala.util.Failure(e) => logError(s"Parsing Application Config for ${app.name} failed => ${e.getMessage}")
          }
        })
        watcher.start

        val midasSocket = new ServerSocket(config.midasPort, maxClientConnections, InetAddress.getByName(config.midasHost))
        val accumulate = Accumulator[DuplexPipe](Nil)

        sys.ShutdownHookThread {
          watcher.stopWatching
          val pipes = accumulate(null)
          val forceStopMsg = "User Forced Stop on Midas...Closing Open Connections"
          logInfo(forceStopMsg)
          println(forceStopMsg)
          pipes filter(_.isActive) map(_.forceStop)
          val shutdownMsg = "Midas Shutdown Complete!"
          logInfo(shutdownMsg)
          println(shutdownMsg)
        }
        //TODO#2: Later from an admin client that changes the Midas mode at runtime without shutting it down
        import SocketConnector._
        while (true) {
          val application = waitForNewConnectionOn(midasSocket)
          val newConMsg = s"New connection received from Remote IP: ${application.getInetAddress} Remote Port: ${application.getPort}, Local Port: ${application.getLocalPort}"
          logInfo(newConMsg)
          println(newConMsg)
          try {
            val mongoSocket = new Socket(config.mongoHost, config.mongoPort)
            val tracker = new MessageTracker()
            
            val requestInterceptable = new RequestInterceptor(tracker, mode, transformer, application.getInetAddress)
            val responseInterceptable = new ResponseInterceptor(tracker, transformer)

            val duplexPipe = application  <|==|> (mongoSocket, requestInterceptable, responseInterceptable)
            duplexPipe.start
            val pipeReadyMsg = s"Setup All Connections, ready to receive traffic on $duplexPipe"
            logInfo(pipeReadyMsg)
            println(pipeReadyMsg)
            accumulate(duplexPipe)
          }
          catch {
            case e: ConnectException  =>
              val errMsg = s"MongoDB on ${config.mongoHost}:${config.mongoPort} is not available!"
              println(errMsg)
              logError(errMsg)
              application.close()
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

  private def deltasDir(config: MidasCmdConfig, appDir: String): URL = {
    new File(config.baseDeltasDir.getPath + "/" + appDir).toURI.toURL
  }
}
