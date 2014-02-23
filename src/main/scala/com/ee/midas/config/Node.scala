package com.ee.midas.config

import java.net.{Socket, InetAddress}
import scala.util.Try
import com.ee.midas.interceptor.{ResponseInterceptor, RequestInterceptor, MessageTracker}
import com.ee.midas.pipes.SocketConnector._
import com.ee.midas.pipes.DuplexPipe
import com.ee.midas.utils.{Accumulator, Loggable}
import com.ee.midas.transform.Transformer

final class Node(private var name: String, val ip: InetAddress, private var changeSet: ChangeSet, private val pipes: Accumulator[DuplexPipe] = Accumulator[DuplexPipe]) extends Loggable {

  def startDuplexPipe(appSocket: Socket, mongoHost: String, mongoPort: Int, transformer: Transformer) = {
    val appIp = appSocket.getInetAddress
    setupDuplexPipe(appSocket, mongoHost, mongoPort, transformer) match {
      case scala.util.Success(duplexPipe) =>
        pipes(duplexPipe)
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

  private def setupDuplexPipe(appSocket: Socket, mongoHost: String, mongoPort: Int, transformer: Transformer) = {
    //todo: remove inactive duplex pipes here (proactive cleaning)
    Try {
      val mongoSocket = new Socket(mongoHost, mongoPort)
      val tracker = new MessageTracker()
      val requestInterceptor = new RequestInterceptor(tracker, transformer, changeSet)
      val responseInterceptor = new ResponseInterceptor(tracker, transformer)
      appSocket <|==|> (mongoSocket, requestInterceptor, responseInterceptor)
    }
  }

  private def activePipes = pipes(null) filter (_.isActive)

  def stop = activePipes foreach (_.forceStop)

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

  override def toString = s"Node($name, $ip, $changeSet)"
}