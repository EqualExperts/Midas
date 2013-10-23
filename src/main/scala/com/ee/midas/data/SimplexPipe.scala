package com.ee.midas.data

import java.io.{OutputStream, InputStream}

class SimplexPipe(val source: InputStream, val destination: OutputStream) extends Thread{

  override def run()={
      handle()
  }


  def handle() = {
    var bytesRead:Int=0
    do{
      if(source.available() > 0) {
        val data:Array[Byte] = new Array[Byte](source.available())
        bytesRead = source.read(data)
        destination.write(data)
        destination.flush()
      }
    }while(bytesRead != -1)
  }
}
