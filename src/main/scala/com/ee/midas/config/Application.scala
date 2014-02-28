package com.ee.midas.config

import java.net.{Socket, InetAddress, URL}
import com.ee.midas.transform.{Transformer, TransformType}
import com.ee.midas.utils.{SynchronizedHolder, Loggable}
import com.ee.midas.DeltasProcessor
import scala.{None, Some}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator

class Application(val configDir: URL,
                  private var _name: String,
                  private var transformType: TransformType,
                  private var nodes: Set[Node],
                  private val transformerHolder: SynchronizedHolder[Transformer] = SynchronizedHolder(Transformer.empty))
  extends Watchable[Application] with DeltasProcessor with Loggable {

  private val translator = new Translator[Transformer](new Reader, new ScalaGenerator)
  transformerHolder(processDeltaFiles)

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
  
  private def updateName(from: Application) = _name = from.name

  private def updateTransformerIfSuccessfullyParsed(from: Application) = {
    val newTransformer = from.transformerHolder.get
    if(newTransformer != Transformer.empty) {
      transformerHolder(from.transformerHolder.get)
    }
  }
  
  private def updateCommonNodes(from: Application) = {
    val newNodes = from.nodes
    val commonNodes = nodes.filter(n => newNodes.contains(n)) 
    commonNodes.foreach { oldNode =>
      newNodes.find(newNode => oldNode == newNode) match {
        case Some(newNode) => oldNode.update(newNode)
        case None =>
      }
    }
  }
  
  private def diffNodes(from: Application) = {
    val newNodes = from.nodes
    val common = nodes intersect newNodes
    val add = newNodes diff common
    logInfo(s"Nodes to be Added $add")

    val remove = nodes diff common
    logInfo(s"Nodes to be Removed $remove")
    (add, remove)
  }

  final def update(fromApp: Application) = {
    updateName(fromApp)
    updateTransformerIfSuccessfullyParsed(fromApp)
    updateCommonNodes(fromApp)
    val (addNodes, removeNodes) = diffNodes(fromApp)
    logInfo(s"Stopping Nodes to be Removed $removeNodes")
    removeNodes.foreach(node => node.stop)
    nodes --= removeNodes
    
    nodes ++= addNodes
    logInfo(s"Total Nodes = $nodes")
    logInfo(s"Completed Updation of All Nodes for Application ${name}")
  }

  final def getNode(ip: InetAddress): Option[Node] = nodes.find(node => node.ip == ip)

  final def isActive = nodes.exists(node => node.isActive)

  final def stop = nodes foreach { node => node.stop }

  final def acceptAuthorized(appSocket: Socket, mongoSocket: Socket): Unit = {
    val appIp = appSocket.getInetAddress
    getNode(appIp) match {
      case Some(node) => node.startDuplexPipe(appSocket, mongoSocket, transformerHolder)
      case None => logError(s"Node with IP Address $appIp does not exist for Application: $name in Config Dir $configDir")
    }
  }

  final def name = _name

  final override def equals(other: Any): Boolean = other match {
    case that: Application => this.configDir.toURI == that.configDir.toURI
    case _ => false
  }

  final override val hashCode: Int = 17 * (17 + configDir.toURI.hashCode)

  final override def toString =
    s"""
      | //Config Dir = $configDir
      |$name {
      |   mode = ${transformType.name.toLowerCase}
      |   ${nodes mkString " "}
      |}
     """.stripMargin

}
