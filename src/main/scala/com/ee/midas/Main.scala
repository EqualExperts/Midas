package com.ee.midas


import com.ee.midas.pipes.{SocketConnector, DuplexPipe}
import java.net._
import com.ee.midas.utils.{DirectoryWatcher, Accumulator, Loggable}
import com.ee.midas.interceptor.{MessageTracker, RequestInterceptor, ResponseInterceptor}
import com.ee.midas.dsl.generator.ScalaGenerator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.Translator
import java.io.{PrintWriter, File}
import com.ee.midas.transform.{Transformer, Transformations, Transforms, TransformType}
import com.ee.midas.hotdeploy.DeployableHolder
import java.nio.file.StandardWatchEventKinds._

object Main extends App with Loggable {
  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {
    CLIParser.parse(args) match {
      case Some(config) =>
        val waitBeforeProcessing = 100
        val loader = Main.getClass.getClassLoader

        val midasConfigURL = config.midasConfig
        val mode = processMidasConfig(midasConfigURL)
        val modeMsg = s"Starting Midas in ${mode} mode...on ${config.midasHost}, port ${config.midasPort}"
        logInfo(modeMsg)
        println(modeMsg)

        //Todo: tweak scala style rule so that we don't have to give types when declaring variables.
        val srcScalaTemplateURI = "templates/Transformations.scala.template"
        val srcScalaDirURI = "generated/scala/"
        val srcScalaFilename = "Transformations.scala"
        val binDirURI = "generated/scala/bin/"
        val clazzName = "com.ee.midas.transform.Transformations"

        val classpathURI = "."
        val classpathDir = loader.getResource(classpathURI)
        val binDir = loader.getResource(binDirURI)
        val srcScalaTemplate = loader.getResource(srcScalaTemplateURI)
        val srcScalaDir = loader.getResource(srcScalaDirURI)
        logInfo(s"Source Scala Dir = $srcScalaDir")

        val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)
        val deltasDirURL: URL = deltasDir(config, mode)
        val processingDeltaFilesMsg = s"Processing Delta Files...from Dir ${deltasDirURL}"
        println(processingDeltaFilesMsg)
        logInfo(processingDeltaFilesMsg)
        val deployableHolder = createDeployableHolder
        implicit val deltasProcessor =
          new DeltaFilesProcessor(new Translator(new Reader(), new ScalaGenerator()), deployableHolder)
        processDeltaFiles(mode, deltasDirURL, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)

        val dirWatchMsg = s"Setting up Directory Watcher for ${config.baseDeltasDir}..."
        println(dirWatchMsg)
        logInfo(dirWatchMsg)
        val watcher = new DirectoryWatcher(config.baseDeltasDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY),
          waitBeforeProcessing)(watchEvents => {
          watchEvents.foreach {watchEvent =>
            logInfo(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
          }
          val transformType = processMidasConfig(midasConfigURL)
          val deltasDirURL = deltasDir(config, transformType)
          processDeltaFiles(transformType, deltasDirURL, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)
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
          try{
            val mongoSocket = new Socket(config.mongoHost, config.mongoPort)
            val tracker = new MessageTracker()
            val requestInterceptable = new RequestInterceptor(tracker, mode)
            val responseInterceptable = new ResponseInterceptor(tracker, new Transformer(deployableHolder))

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

  private def processDeltaFiles(transformType: TransformType, deltasDir: URL, srcScalaTemplate: URL, srcScalaFile: File, binDir: URL,
                                clazzName: String, classpathDir: URL)(implicit deltasProcessor: DeltaFilesProcessor): Unit = {
    val writer = new PrintWriter(srcScalaFile, "utf-8")
    try {
      val processDeltaFilesMsg = s"Compiling and Deploying Delta Files in Midas..."
      logInfo(processDeltaFilesMsg)
      println(processDeltaFilesMsg)
      deltasProcessor.process(transformType, deltasDir, srcScalaTemplate, writer, srcScalaFile, binDir, clazzName, classpathDir)
    } catch {
      case e: Exception =>
        val errMsg = s"Error Processing Delta File: ${e.getMessage}, Please fix the compilation issue in delta file and rethrow it in the appropriate directory!"
        logInfo(errMsg)
        println(errMsg)
    } finally {
      writer.close()
    }
  }
  
  private def deltasDir(config: MidasCmdConfig, transformType: TransformType): URL = {
    new File(config.baseDeltasDir.getPath + "/" + transformType.toString.toLowerCase).toURI.toURL
  }
  
  private def processMidasConfig(url: URL): TransformType = {
    logInfo(s"Midas Config URL = $url")
    val midasConfig = new Configuration(url)
    midasConfig.mode
  }

  private def createDeployableHolder =
    new DeployableHolder[Transforms] {
      def createDeployable: Transforms = new Transformations
    }
  }

