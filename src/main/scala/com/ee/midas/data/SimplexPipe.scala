package com.ee.midas.data

import java.io.{OutputStream, InputStream}

class SimplexPipe(val source: InputStream, val destination: OutputStream) extends Thread {
  var stopThread = false

  override def run : Unit = {
    while(!stopThread) {
      handle
    }
  }

  def close = {
    stopThread = true
  }

  def handle : Unit = {
    if(source.available() > 0) {
      val data:Array[Byte] = new Array[Byte](source.available())
      source.read(data)
      destination.write(data)
      destination.flush()
    }
  }
}
