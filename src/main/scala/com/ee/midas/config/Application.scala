package com.ee.midas.config

import java.net.{Socket, InetAddress, URL}
import com.ee.midas.transform.{Transformer, TransformType}
import com.ee.midas.utils.{DirectoryWatcher, Loggable}
import com.ee.midas.DeltasProcessor
import java.nio.file.StandardWatchEventKinds._
import scala.{None, Some}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator

class Application(val configDir: URL, private var name: String, private var transformType: TransformType, private var nodes: Set[Node])
  extends Loggable with DeltasProcessor with ApplicationParsers {
  private val translator = new Translator[Transformer](new Reader, new ScalaGenerator)
  private var transformer = processDeltaFiles
  private lazy val watcher = setupDirectoryWatcher

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
        case scala.util.Success(updatedApp) => update(updatedApp)
        case scala.util.Failure(t) =>
          logError(s"Failed to parse Application ${name} because ${t.getMessage}")
          logError(s"Will Continue To Use Old Application Config")
      }
    })
  }

  private def update(newApplication: Application) = {
    name = newApplication.name
    transformType = newApplication.transformType
    if(newApplication.transformer != Transformer.empty) {
      transformer = newApplication.transformer
    }
    //todo: work this out same as how configuration works out for nodes
    val newNodes = newApplication.nodes
    nodes.filter(n => newNodes.contains(n)).foreach { node =>
      newNodes.find(nn => node == nn) match {
        case Some(newNode) => node.updateFrom(newNode)
        case None =>
      }
    }

    val common = nodes intersect newNodes
    val toBeAdded = newNodes diff common
    logInfo(s"Nodes to be Added $toBeAdded")
    nodes ++= toBeAdded

    val toBeRemoved = nodes diff common
    logInfo(s"Stopping Nodes to be Removed $toBeRemoved")
    toBeRemoved.foreach(node => node.stop)
    //todo: do something to remove old nodes here

    logInfo(s"Total Nodes $nodes")
    logError(s"Updated Nodes for Application ${name}")
  }

  private def processDeltaFiles: Transformer = {
    val processingDeltaFilesMsg =
      s"Processing Delta Files for Application ${name} in mode ${transformType} from Dir ${configDir}"
    println(processingDeltaFilesMsg)
    logInfo(processingDeltaFilesMsg)
    processDeltas(translator, transformType, configDir) match {
      case scala.util.Success(transformer) =>
        transformer

      case scala.util.Failure(t) =>
        logError(s"Failed to Process Delta Files for Application ${name} in mode ${transformType} from Dir ${configDir}")
        logError(s"Error => ${t.getMessage}")
        Transformer.empty
    }
  }

  def hasNode(ip: InetAddress): Boolean = nodes.exists(node => node.ip == ip)

  def getNode(ip: InetAddress): Option[Node] = nodes.find(node => node.ip == ip)

  def start = watcher.start

  def stop = {
    watcher.stopWatching
    nodes foreach { node => node.stop }
  }

  def acceptAuthorized(appSocket: Socket, mongoSocket: Socket): Unit = {
    val appIp = appSocket.getInetAddress
    getNode(appIp) match {
      case Some(node) => node.startDuplexPipe(appSocket, mongoSocket, transformer)
      case None => logError(s"Node with IP Address $appIp does not exist for Application: $name in Config Dir $configDir")
    }
  }

  override def toString = s"""Application(configDir = ${configDir.toURI}, name = $name, mode = $transformType, nodes = ${nodes mkString "," }, $transformer"""
}
