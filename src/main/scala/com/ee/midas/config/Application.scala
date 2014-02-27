package com.ee.midas.config

import java.net.{Socket, InetAddress, URL}
import com.ee.midas.transform.{Transformer, TransformType}
import com.ee.midas.utils.Loggable
import com.ee.midas.DeltasProcessor
import scala.{None, Some}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator

class Application(val configDir: URL, private[Application] var _name: String, private var transformType: TransformType, private var nodes: Set[Node])
  extends Watchable[Application] with DeltasProcessor with Loggable {

  private val translator = new Translator[Transformer](new Reader, new ScalaGenerator)
  private var transformer = processDeltaFiles

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

  final def update(newApplication: Application) = {
    _name = newApplication.name
    val newAppTransformer = newApplication.processDeltaFiles
    if(newAppTransformer != Transformer.empty) {
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

    val toBeRemoved = nodes diff common
    logInfo(s"Stopping Nodes to be Removed $toBeRemoved")
    toBeRemoved.foreach(node => node.stop)
    nodes ++= toBeAdded
    //todo: do something to remove old nodes here
    nodes --= toBeRemoved

    logInfo(s"Total Nodes $nodes")
    logError(s"Updated Nodes for Application ${name}")
  }

  final def hasNode(ip: InetAddress): Boolean = nodes.exists(node => node.ip == ip)

  final def getNode(ip: InetAddress): Option[Node] = nodes.find(node => node.ip == ip)

  final def isActive = nodes.exists(node => node.isActive)

  final def stop = nodes foreach { node => node.stop }

  final def acceptAuthorized(appSocket: Socket, mongoSocket: Socket): Unit = {
    val appIp = appSocket.getInetAddress
    getNode(appIp) match {
      case Some(node) => node.startDuplexPipe(appSocket, mongoSocket, transformer)
      case None => logError(s"Node with IP Address $appIp does not exist for Application: $name in Config Dir $configDir")
    }
  }

  final def name = _name

  final override def equals(other: Any): Boolean = other match {
    case that: Application => this.configDir.toURI == that.configDir.toURI
    case _ => false
  }

  final override val hashCode: Int = 17 * (17 + configDir.toURI.hashCode)

  final override def toString = s"""Application(configDir = ${configDir.toURI}, name = $name, mode = $transformType, nodes = ${nodes mkString "," }, $transformer"""
}
