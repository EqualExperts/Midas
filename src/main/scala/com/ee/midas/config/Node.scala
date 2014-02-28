package com.ee.midas.config

import java.net.{Socket, InetAddress}
import com.ee.midas.interceptor.{ResponseInterceptor, RequestInterceptor, MessageTracker}
import com.ee.midas.pipes.SocketConnector._
import com.ee.midas.pipes.DuplexPipe
import com.ee.midas.utils.{SynchronizedHolder, Loggable}
import com.ee.midas.transform.Transformer
import scala.collection.mutable.ArrayBuffer

class Node(private var _name: String, val ip: InetAddress, private var _changeSet: ChangeSet) extends Loggable {

  private val pipes = ArrayBuffer[DuplexPipe]()

  final def name = _name

  final def changeSet = _changeSet

  final def startDuplexPipe(appSocket: Socket, mongoSocket: Socket, transformerHolder: SynchronizedHolder[Transformer]) = {
    cleanDeadPipes
    val tracker = new MessageTracker()
    val requestInterceptor = new RequestInterceptor(tracker, transformerHolder, changeSet)
    val responseInterceptor = new ResponseInterceptor(tracker, transformerHolder)
    val duplexPipe = appSocket <|==|> (mongoSocket, requestInterceptor, responseInterceptor)
    pipes += duplexPipe
    duplexPipe.start
    val pipeReadyMsg = s"Setup Pipes for New Connection, ready to receive traffic on $duplexPipe"
    logInfo(pipeReadyMsg)
    println(pipeReadyMsg)
    duplexPipe
  }

  final def stop = pipes filter (_.isActive) foreach (_.forceStop)

  final def cleanDeadPipes = {
    val deadPipes = pipes filterNot (_.isActive)
    pipes --= deadPipes
  }

  final def isActive = pipes exists(pipe => pipe.isActive)

  final def update(fromNode: Node) = {
    _name = fromNode._name
    _changeSet = fromNode._changeSet
  }

  final override def equals(other: Any): Boolean = other match {
    case that: Node => this.ip == that.ip
    case _ => false
  }

  final override val hashCode: Int = 17 * (17 + ip.hashCode)

  final override def toString = {
    s"""
      |$name {
      | ip = ${ip.toString.substring(1)}
      | changeSet = ${changeSet.number}
      |}
      |
     """.stripMargin
  }
}