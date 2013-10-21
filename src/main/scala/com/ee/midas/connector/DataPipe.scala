package com.ee.midas.connector

import java.io.{OutputStream, InputStream}


class DataPipe {

  def readFromClient(clientInputStream: InputStream):Array[Byte]={
    val data:Array[Byte] = new Array[Byte](clientInputStream.available())
    clientInputStream.read(data)
    /*val strData:String = new String(data)
    println("Data is "+ strData)
    strData*/
    data
  }

  def writeToServer(serverOutputStream: OutputStream, data:Array[Byte])={
    serverOutputStream.write(data, 0, data.length)
    serverOutputStream.flush()
  }
}
