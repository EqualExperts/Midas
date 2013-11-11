package com.ee.midas.pipes

import java.net.Socket

class SocketConnector private (client: Socket) {
  def <==> (server: Socket) : DuplexPipe = DuplexPipe(==> (server), <== (server))

  def ==> (server: Socket) : SimplexPipe = {
    val clientIn = client.getInputStream
    val serverOut = server.getOutputStream
    new SimplexPipe("==>", clientIn, serverOut)
  }

  def <== (server: Socket) : SimplexPipe = {
    val serverIn = server.getInputStream
    val clientOut = client.getOutputStream
    new SimplexPipe("<==", serverIn, clientOut)
  }
}

object SocketConnector {
  implicit def apply(client: Socket) = new SocketConnector(client)
}
