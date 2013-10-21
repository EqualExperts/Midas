package com.ee.midas.connector

import java.io.{OutputStream, InputStream}


class DataPipe {

  def readFromClient(clientInputStream: InputStream):String={

    var data:Array[Byte] = new Array[Byte](clientInputStream.available())
    clientInputStream.read(data)
    val strData:String = new String(data)
    println("Data is "+ strData)
    strData
  }

  def writeToServer(serverOutputStream: OutputStream, data:Array[byt  e])={

    serverOutputStream.write()
  }
}
