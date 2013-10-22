package com.ee.midas.data

import java.io.{OutputStream, InputStream}

class SimplexPipe(source: InputStream, destination: OutputStream) {

  def handle() {
    if(source.available() > 0) {
      val data:Array[Byte] = new Array[Byte](source.available())
      source.read(data)
      destination.write(data)
      destination.flush()
    }
  }
}
