package com.ee.midas.pipes

import java.net.Socket

class SocketConnector private (source: Socket) {
  def <==> (target: Socket) : DuplexPipe = DuplexPipe(==> (target), <== (target))

  def ==> (target: Socket) : SimplexPipe = {
    val srcIn = source.getInputStream
    val tgtOut = target.getOutputStream
    new SimplexPipe("==>", srcIn, tgtOut)
  }

  def <== (target: Socket) : SimplexPipe = {
    val tgtIn = target.getInputStream
    val srcOut = source.getOutputStream
    new SimplexPipe("<==", tgtIn, srcOut)
  }
}

object SocketConnector {
  implicit def apply(source: Socket) = new SocketConnector(source)
}
