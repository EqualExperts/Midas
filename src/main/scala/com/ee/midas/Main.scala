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
import scala.tools.nsc.util.ClassPath


object Main extends App with Loggable {
  val maxClientConnections = 50

  override def main(args:Array[String]): Unit = {

    val (midasHost: String, midasPort: Int, mongoHost: String, mongoPort: Int, transformType: TransformType, deltasDirURI: String) = processCLIparameters(args)
    val loader = Main.getClass.getClassLoader

    //val deltasDirURI = "deltas/"
    val deltasDirFile: File = new File(deltasDirURI)
    val parentDeltasDir = deltasDirFile.getParentFile

    println("parent "+parentDeltasDir )
    addToClassPath(parentDeltasDir.toURI.toURL)


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
    val watcher = new DirectoryWatcher(deltasDir.getPath)(watchEvent => {
      log.info(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
      processDeltaFiles(deltasDir, srcScalaTemplate, srcScalaFile, binDir, clazzName, classpathDir)
    })
    watcher.start

    log.info(s"Starting Midas Server...")
    val midasSocket = new ServerSocket(midasPort, maxClientConnections, InetAddress.getByName(midasHost))
    val accumulate = Accumulator[DuplexPipe](Nil)

    sys.ShutdownHookThread {
      watcher.stopWatching
      val pipes = accumulate(null)
      log.info("User Forced Stop on Midas...Closing Open Connections")
      pipes filter(_.isActive) map(_.forceStop)
    }
    //TODO#1: Wire this as option from cmdLine
    //TODO#2: Later from an admin client that changes the Midas mode at runtime without shutting it down
   // val transformType = TransformType.EXPANSION
    import SocketConnector._
    while (true) {
      val application = waitForNewConnectionOn(midasSocket)
      log.info("New connection received...")
      try{
        val mongoSocket = new Socket(mongoHost, mongoPort)
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
          println(s"Error : MongoDB on $mongoHost:$mongoPort is not available")
          log.error(s"MongoDB on $mongoHost:$mongoPort is not available")
          application.close()
      }
    }
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


    def processCLIparameters(args:Array[String]) = {
      val midasHost = "localhost"
      val parser = new scopt.OptionParser[Config]("midas") {
        opt[Int]("port") action { (x,c) => c.copy(midasPort = x)} text("OPTIONAL, the port on which midas will accept connections, default is 27020")
        opt[String]("source") action { (x,c) => c.copy(mongoHost = x)} text("OPTIONAL, the mongo host midas will connect to, default is localhost")
        opt[Int]("mongoPort") action { (x,c) => c.copy(mongoPort = x)} text("OPTIONAL, the mongo port midas will connect to, default is 27017")
        opt[String]("mode") action { (x,c) => { if(x.equalsIgnoreCase("contraction"))
                                                   c.copy(mode = TransformType.CONTRACTION)
                                                else
                                                   c.copy(mode = TransformType.EXPANSION)  } } text("OPTIONAL, the operation mode (EXPANSION/CONTRACTION) for midas, default is EXPANSION")
        opt[String]("deltasDir") action { (x,c) => c.copy(deltasDir = x)}  text("OPTIONAL, the location of delta files ")
        help("help") text ("Show usage")
        override def reportError(msg: String) : Unit = {
          println(usage)
          sys.exit
        }
      }

      val result = parser.parse(args,Config())
      (midasHost, result.get.midasPort , result.get.mongoHost,result.get.mongoPort, result.get.mode, result.get.deltasDir)

      }

    private def addToClassPath(uri : URL) = {
      val sysloader: URLClassLoader = (ClassLoader.getSystemClassLoader()).asInstanceOf[URLClassLoader]
      val sysclass: Class[URLClassLoader] = classOf[URLClassLoader]
      val parameters: Class[_] = classOf[URL]
      val url:Object = uri
      val method: Method  = sysclass.getDeclaredMethod("addURL", parameters)
      method.setAccessible(true)
      method.invoke(sysloader, url)
    }
  }

