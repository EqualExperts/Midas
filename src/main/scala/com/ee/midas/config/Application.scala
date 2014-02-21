package com.ee.midas.config

import java.net.{Socket, InetAddress, URL}
import com.ee.midas.transform.{Transformer, TransformType}
import com.ee.midas.utils.{DirectoryWatcher, Loggable}
import com.ee.midas.{CmdConfig, DeltasProcessor}
import java.nio.file.StandardWatchEventKinds._
import scala.{None, Some}
import org.bson.BSONObject
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import scala.util.{Try}
import com.ee.midas.pipes.{SocketConnector, DuplexPipe}
import com.ee.midas.interceptor.{ResponseInterceptor, RequestInterceptor, MessageTracker}
import SocketConnector._

case class Application(configDir: URL, private var name: String, private var transformType: TransformType, private var nodes: List[Node])
  extends Loggable with DeltasProcessor with ApplicationParsers {
  private val translator = new Translator[Transformer](new Reader, new ScalaGenerator)
  private var transformer = processDeltaFiles
  private val watcher = setupDirectoryWatcher
  private val connections = scala.collection.mutable.Map[InetAddress, List[DuplexPipe]]()

  private def setupDirectoryWatcher: DirectoryWatcher = {
    val waitBeforeProcessing = 100
    val dirWatchMsg = s"Setting up Directory Watcher for Application ${name} on ${configDir}..."
    println(dirWatchMsg)
    logInfo(dirWatchMsg)
    new DirectoryWatcher(configDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY),
      waitBeforeProcessing, stopWatchingOnException = false)(watchEvents => {
      watchEvents.foreach { watchEvent =>
        logInfo(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
      }

      parse(configDir) match {
        case scala.util.Success(updatedApp) => {
          //update self
          name = updatedApp.name
          transformType = updatedApp.transformType
          if(updatedApp.transformer != Transformer.empty) {
            transformer = updatedApp.transformer
          }
          nodes = updatedApp.nodes
          updatedApp.stop
          cleanStaleConnections
          logError(s"Updated Configuration for ${name}")
        }
        case scala.util.Failure(t) =>
          logError(s"Failed to parse Application ${name} because ${t.getMessage}")
          logError(s" Will Continue To Use Old Application Config")
      }
    })
  }

  private def processDeltaFiles: Transformer = {
    val processingDeltaFilesMsg =
      s"Processing Delta Files for Application ${name} in mode ${transformType} from Dir ${configDir}"
    println(processingDeltaFilesMsg)
    logInfo(processingDeltaFilesMsg)
    try {
      processDeltas(translator, transformType, configDir)
    } catch {
      case e: Throwable => Transformer.empty
    }
  }

  private def cleanStaleConnections = {
    val connectionsToTerminate = connections.filter { case (ip, _) => !hasNode(ip) }
    stopActivePipes(connectionsToTerminate)
    connectionsToTerminate foreach { case (ip, _) => connections.remove(ip) }
  }
  
  private def stopActivePipes(connections: scala.collection.mutable.Map[InetAddress, List[DuplexPipe]]) = {
    for {
      (ip, pipes) <- connections
      pipe <- pipes if pipe.isActive
    } yield pipe.forceStop
  }

  def hasNode(ip: InetAddress): Boolean =
    nodes.exists(node => node.ip == ip)

  def getNode(ip: InetAddress): Option[Node] =
    nodes.find(node => node.ip == ip)

  def changeSet(ip: InetAddress): Option[ChangeSet] = getNode(ip) match {
    case None => None
    case Some(Node(_, _, cs)) => Some(cs)
  }

  def transformRequest(document: BSONObject, fullCollectionName: String, ip: InetAddress): BSONObject =
    changeSet(ip) match {
      case Some(ChangeSet(cs)) => transformer.transformRequest(document, cs, fullCollectionName)
      case None => document
    }

  def transformResponse(document: BSONObject, fullCollectionName: String): BSONObject =
    transformer.transformResponse(document, fullCollectionName)
  
  def start = watcher.start
  
  def stop = {
    watcher.stopWatching
    stopActivePipes(connections)
  }

  def startDuplexPipe(appSocket: Socket, cmdConfig: CmdConfig): Unit = {
    val mongoHost = cmdConfig.mongoHost
    val mongoPort = cmdConfig.mongoPort
    val appIp = appSocket.getInetAddress
    setupDuplexPipe(appSocket, mongoHost, mongoPort) match {
      case scala.util.Success(duplexPipe) =>
        if(connections.isDefinedAt(appIp)) {
          val pipes = connections(appIp)
          connections(appIp) = duplexPipe :: pipes
        } else {
          connections(appIp) = duplexPipe :: Nil
        }
        duplexPipe.start
        val pipeReadyMsg = s"Setup Pipes for New Connection, ready to receive traffic on $duplexPipe"
        logInfo(pipeReadyMsg)
        println(pipeReadyMsg)
      case scala.util.Failure(t) =>
        val errMsg = s"MongoDB on ${mongoHost}:${mongoPort} is not available!  Terminating connection from ${appIp}, Please retry later."
        println(errMsg)
        logError(errMsg)
        appSocket.close()
    }
  }

  private def setupDuplexPipe(appSocket: Socket, mongoHost: String, mongoPort: Int) = {
    //todo: remove inactive duplex pipes here  (proactive cleaning)
    val appInetAddress = appSocket.getInetAddress
    Try {
      val mongoSocket = new Socket(mongoHost, mongoPort)
      val tracker = new MessageTracker()
      val requestInterceptor = new RequestInterceptor(tracker, this, appInetAddress)
      val responseInterceptor = new ResponseInterceptor(tracker, this, appInetAddress)
      appSocket <|==|> (mongoSocket, requestInterceptor, responseInterceptor)
    }
  }

  override def toString = s"""Application(configDir = ${configDir.toURI}, name = $name, mode = $transformType, nodes = ${nodes mkString "," }, $transformer"""
}
