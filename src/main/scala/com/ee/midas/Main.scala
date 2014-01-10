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
        val transformType = config.mode

        //Todo: tweak scala style rule so that we don't have to give types when declaring variables.
        val deltasDirURL: URL = config.deltasDirURL
        val srcScalaTemplateURI = "templates/Transformations.scala.template"
        val srcScalaDirURI = "generated/scala/"
        val srcScalaFilename = "Transformations.scala"
        val binDirURI = "generated/scala/bin/"
        val clazzName = "com.ee.midas.transform.Transformations"

        val classpathURI = "."
        val classpathDir = loader.getResource(classpathURI)
        val binDir = loader.getResource(binDirURI)
        log.info(s"Picking up Deltas from Dir = ${deltasDirURL}")
        val srcScalaTemplate = loader.getResource(srcScalaTemplateURI)
        val srcScalaDir = loader.getResource(srcScalaDirURI)
        log.info(s"Source Scala Dir = $srcScalaDir")

        val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)
        log.info(s"Processing Delta Files...")
        val deployableHolder = createDeployableHolder
        implicit val deltasProcessor =
          new DeltaFilesProcessor(new Translator(new Reader(), new ScalaGenerator()), deployableHolder)
        processDeltaFiles(transformType, deltasDirURL, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)

        log.info(s"Setting up Directory Watcher...")
        val watcher = new DirectoryWatcher(deltasDirURL.getPath, List(ENTRY_CREATE, ENTRY_DELETE),
          waitBeforeProcessing)(watchEvents => {
          watchEvents.foreach {watchEvent =>
            log.info(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
          }
          processDeltaFiles(transformType, deltasDirURL, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)
        })
        watcher.start

        log.info(s"Starting Midas Server in ${transformType} mode...")
        val midasSocket = new ServerSocket(config.midasPort, maxClientConnections, InetAddress.getByName(config.midasHost))
        val accumulate = Accumulator[DuplexPipe](Nil)

        sys.ShutdownHookThread {
          watcher.stopWatching
          val pipes = accumulate(null)
          log.info("User Forced Stop on Midas...Closing Open Connections")
          pipes filter(_.isActive) map(_.forceStop)
        }
        //TODO#2: Later from an admin client that changes the Midas mode at runtime without shutting it down
        import SocketConnector._
        while (true) {
          val application = waitForNewConnectionOn(midasSocket)
          log.info("New connection received...")
          try{
            val mongoSocket = new Socket(config.mongoHost, config.mongoPort)
            val tracker = new MessageTracker()
            val requestInterceptable = new RequestInterceptor(tracker)
            val responseInterceptable = new ResponseInterceptor(tracker, new Transformer(transformType, deployableHolder))

            val duplexPipe = application  <|==|> (mongoSocket, requestInterceptable, responseInterceptable)
            duplexPipe.start
            log.info("Setup DataPipe = " + duplexPipe.toString)
            accumulate(duplexPipe)
          }
          catch {
            case e: ConnectException  =>
              println(s"Error : MongoDB on ${config.mongoHost}:${config.mongoPort} is not available")
              log.error(s"MongoDB on ${config.mongoHost}:${config.mongoPort} is not available")
              application.close()
          }
        }

      case None =>
    }
  }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    log.info("Listening on port " + serverSocket.getLocalPort() + " for new connections...")
    serverSocket.accept()
  }

  private def processDeltaFiles(transformType: TransformType, deltasDir: URL, srcScalaTemplate: URL, srcScalaFile: File, binDir: URL,
                                clazzName: String, classpathDir: URL)(implicit deltasProcessor: DeltaFilesProcessor): Unit = {
    val writer = new PrintWriter(srcScalaFile, "utf-8")
    try {
      deltasProcessor.process(transformType, deltasDir, srcScalaTemplate, writer, srcScalaFile, binDir, clazzName, classpathDir)
    } catch {
      case e: Exception => log.info(s"Error Processing Delta File: ${e.getMessage}, Please fix the compilation issue in delta file and rethrow it in the appropriate directory!")
    } finally {
      writer.close()
    }
  }

  private def createDeployableHolder =
    new DeployableHolder[Transforms] {
      def createDeployable: Transforms = new Transformations
    }
  }

