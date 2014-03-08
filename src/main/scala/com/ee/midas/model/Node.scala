/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.model

import java.net.{Socket, InetAddress}
import com.ee.midas.interceptor.{ResponseInterceptor, RequestInterceptor, MessageTracker}
import com.ee.midas.pipes.SocketConnector._
import com.ee.midas.pipes.DuplexPipe
import com.ee.midas.utils.{SynchronizedHolder, Loggable}
import com.ee.midas.transform.Transformer
import scala.collection.mutable.ArrayBuffer

class Node(private var _name: String, val ip: InetAddress, private val _changeSet: ChangeSet) extends Loggable {

  private val pipes = ArrayBuffer[DuplexPipe]()
  
  private val changeSetHolder = SynchronizedHolder[ChangeSet](_changeSet)

  final def name = _name

  final def changeSet = changeSetHolder.get

  final def startDuplexPipe(client: Socket, mongo: Socket, transformerHolder: SynchronizedHolder[Transformer]) = {
    cleanDeadPipes
    val tracker = new MessageTracker()
    val requestInterceptor = new RequestInterceptor(tracker, transformerHolder, changeSetHolder)
    val responseInterceptor = new ResponseInterceptor(tracker, transformerHolder)
    val duplexPipe = client <|==|> (mongo, requestInterceptor, responseInterceptor)
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
    changeSetHolder(fromNode.changeSet)
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
      | changeSet = ${changeSetHolder.get.number}
      |}
      |
     """.stripMargin
  }
}