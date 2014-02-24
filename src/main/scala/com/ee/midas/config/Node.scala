package com.ee.midas.config

import java.net.{Socket, InetAddress}
import com.ee.midas.interceptor.{ResponseInterceptor, RequestInterceptor, MessageTracker}
import com.ee.midas.pipes.SocketConnector._
import com.ee.midas.pipes.DuplexPipe
import com.ee.midas.utils.Loggable
import com.ee.midas.transform.Transformer
import scala.collection.mutable.Set

class Node(private var name: String, val ip: InetAddress, private var changeSet: ChangeSet) extends Loggable {

  private val pipes = Set[DuplexPipe]()

  final def startDuplexPipe(appSocket: Socket, mongoSocket: Socket, transformer: Transformer) = {
    clean
    val tracker = new MessageTracker()
    val requestInterceptor = new RequestInterceptor(tracker, transformer, changeSet)
    val responseInterceptor = new ResponseInterceptor(tracker, transformer)
    val duplexPipe = appSocket <|==|> (mongoSocket, requestInterceptor, responseInterceptor)
    pipes += duplexPipe
    duplexPipe.start
    val pipeReadyMsg = s"Setup Pipes for New Connection, ready to receive traffic on $duplexPipe"
    logInfo(pipeReadyMsg)
    println(pipeReadyMsg)
    duplexPipe
  }

  private def activePipes = pipes filter (_.isActive)

  def stop = activePipes foreach (_.forceStop)

  def clean = {
    val deadPipes = pipes filterNot (_.isActive)
    pipes --= deadPipes
  }

  def isActive = activePipes.size > 0

  def updateFrom(from: Node) = {
    name = from.name
    changeSet = from.changeSet
  }

  override def equals(other: Any): Boolean = other match {
    case that: Node => this.ip == that.ip
    case _ => false
  }

  override val hashCode: Int = 17 * (17 + ip.hashCode)

  override def toString = s"Node($name, $ip, $changeSet, $isActive)"
}