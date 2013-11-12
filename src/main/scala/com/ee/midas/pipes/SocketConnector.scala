package com.ee.midas.pipes

import java.net.Socket

class SocketConnector private (source: Socket) {
  def <==> (target: Socket) = DuplexPipe(==> (target), <== (target))

  def ==> (target: Socket) = {
    val srcIn = source.getInputStream
    val tgtOut = target.getOutputStream
    new SimplexPipe("==>", srcIn, tgtOut)
  }

  def <== (target: Socket) = {
    val tgtIn = target.getInputStream
    val srcOut = source.getOutputStream
    new SimplexPipe("<==", DecoratorStream(tgtIn), srcOut)
  }
}

object SocketConnector {
  implicit def apply(source: Socket) = new SocketConnector(source)
}
