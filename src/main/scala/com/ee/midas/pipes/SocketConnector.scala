package com.ee.midas.pipes

import java.net.Socket
import com.ee.midas.utils.{OverridingInterceptable, Interceptable}

class SocketConnector private (source: Socket) {
  def <====> (target: Socket) = <|==|>(target)
  def <===|> (target: Socket, request: Interceptable)
                                                = DuplexPipe(===> (target, request), <=== (target))
  def <|===> (target: Socket, response: Interceptable)
                                                = DuplexPipe(===> (target), <=== (target, response))
  def <|==|> (target: Socket,
              request: Interceptable = Interceptable(),
              response: Interceptable = Interceptable())
                                       = DuplexPipe(===> (target, request), <=== (target, response))


  def ===> (target: Socket, interceptable: Interceptable = Interceptable()) = {
    val srcIn = source.getInputStream
    val tgtOut = target.getOutputStream
    new SimplexPipe("===>", srcIn, tgtOut)
  }

  def <=== (target: Socket, newInterceptable: Interceptable = Interceptable()) = {
    val tgtIn = target.getInputStream
    val srcOut = source.getOutputStream
    new SimplexPipe("<===", tgtIn, srcOut, newInterceptable)
      /*with OverridingInterceptable {
      override val interceptable = newInterceptable
    }*/
  }
}

object SocketConnector {
  implicit def apply(source: Socket) = new SocketConnector(source)
}
