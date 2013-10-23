package com.ee.midas.data

import java.net.Socket

class PipeFactory {

  def createSimplexPipe(source: Socket, target: Socket):SimplexPipe = {
    new SimplexPipe(source.getInputStream(), target.getOutputStream())
  }
}
