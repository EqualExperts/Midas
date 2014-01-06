package com.ee.midas


import _root_.java.lang.Class
import _root_.java.lang.reflect.Method
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

        val deltasDirURI : String = processDeltaPath(config.deltasDir)

        val srcScalaTemplateURI = "templates/Transformations.scala.template"
        val srcScalaDirURI = "generated/scala/"
        val srcScalaFilename = "Transformations.scala"
        val binDirURI = "generated/scala/bin/"
        val clazzName = "com.ee.midas.transform.Transformations"
        val classpathURI = "."

        val classpathDir = loader.getResource(classpathURI)
        val binDir = loader.getResource(binDirURI)
        val deltasDir = loader.getResource(deltasDirURI)
        log.info(s"Deltas Dir = $deltasDir")
        val srcScalaTemplate = loader.getResource(srcScalaTemplateURI)
        val srcScalaDir = loader.getResource(srcScalaDirURI)
        log.info(s"Source Scala Dir = $srcScalaDir")
        val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

        log.info(s"Processing Delta Files...")
        val deployableHolder = createDeployableHolder
        implicit val deltasProcessor =
          new DeltaFilesProcessor(new Translator(new Reader(), new ScalaGenerator()), deployableHolder)
        processDeltaFiles(deltasDir, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)
        log.info(s"Completed...Processing Delta Files!")

        log.info(s"Setting up Directory Watcher...")
        val watcher = new DirectoryWatcher(deltasDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE),
          waitBeforeProcessing)(watchEvents => {
          watchEvents.foreach {watchEvent =>
            log.info(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
          }
          processDeltaFiles(deltasDir, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)
        })
        watcher.start

        log.info(s"Starting Midas Server in ${config.mode} mode...")
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
            val responseInterceptable = new ResponseInterceptor(tracker, new Transformer(config.mode, deployableHolder))

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

  private def processDeltaPath(deltasPath: String) : String = {
    val startOfChildDir = deltasPath.lastIndexOf("/")
    val parentDeltaDir =  deltasPath.substring(0,startOfChildDir+1)
    val parentDeltaDirURI: URI = new File(parentDeltaDir).toURI

    addToClassPath(parentDeltaDirURI.toURL)
    deltasPath.substring(startOfChildDir+1,deltasPath.length)
  }

  private def waitForNewConnectionOn(serverSocket: ServerSocket) = {
    log.info("Listening on port " + serverSocket.getLocalPort() + " for new connections...")
    serverSocket.accept()
  }

  private def processDeltaFiles(deltasDir: URL, srcScalaTemplate: URL, srcScalaFile: File, binDir: URL,
                                clazzName: String, classpathDir: URL)(implicit deltasProcessor: DeltaFilesProcessor): Unit = {
    val writer = new PrintWriter(srcScalaFile, "utf-8")
    deltasProcessor.process(deltasDir, srcScalaTemplate, writer, srcScalaFile, binDir, clazzName, classpathDir)
    writer.close()
  }

  private def createDeployableHolder =
    new DeployableHolder[Transforms] {
      def createDeployable: Transforms = new Transformations
    }


  private def addToClassPath(url : URL) = {
      val sysClassLoader: URLClassLoader = (ClassLoader.getSystemClassLoader()).asInstanceOf[URLClassLoader]
      val sysClass: Class[URLClassLoader] = classOf[URLClassLoader]
      val parameter: Class[_] = classOf[URL]
      val urlObject: Object = url
      val method: Method  = sysClass.getDeclaredMethod("addURL", parameter)
      method.setAccessible(true)
      method.invoke(sysClassLoader, urlObject)
    }
  }

