package com.ee.midas.pipes

import java.net.Socket

class SocketConnector private (source: Socket) {

  def <====> (target: Socket) =
    <|==|>(target, Interceptable(), Interceptable())

  def <===|> (target: Socket, request: Interceptable) =
    DuplexPipe(===> (target, request), <=== (target))

  def <|===> (target: Socket, response: Interceptable) =
    DuplexPipe(===> (target), <=== (target, response))

  def <|==|> (target: Socket, request: Interceptable, response: Interceptable) =
    DuplexPipe(===> (target, request), <=== (target, response))

  def ===> (target: Socket, interceptable: Interceptable = Interceptable()) = {
    val srcIn = source.getInputStream
    val tgtOut = target.getOutputStream
    new SimplexPipe("===>", srcIn, tgtOut, interceptable)
  }

  def <=== (target: Socket, interceptable: Interceptable = Interceptable()) = {
    val tgtIn = target.getInputStream
    val srcOut = source.getOutputStream
    new SimplexPipe("<===", tgtIn, srcOut, interceptable)
  }
}

object SocketConnector {
  implicit def apply(source: Socket) = new SocketConnector(source)
}
