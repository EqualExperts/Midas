package com.ee.midas.data

import java.io.{OutputStream, InputStream}
import java.net.SocketException

class SimplexPipe(val source: InputStream, val destination: OutputStream) extends Thread {

  override def run : Unit = {
    try{
      handle
    }
    catch {
      case e:SocketException => println("Socket closed")
    }
  }

  def handle : Unit = {
    var bytesRead: Int =0
    val data:Array[Byte] = new Array[Byte](1024 * 16)
    var numOfRetries:Int = 0
    do{
        numOfRetries = checkSourceStream(numOfRetries)
        bytesRead=source.read(data)
        if(bytesRead > 0) {
          destination.write(data,0,bytesRead)
          destination.flush()
        }
    } while(bytesRead!= -1)

  }

  def checkSourceStream(numOfRetries:Int):Int = {
    var numOfRetriesNew:Int = numOfRetries
    if(source.available()== 0){
       if(numOfRetries>= 15)
           println("data not available on connection")
       try{
            Thread.sleep(50,0)
       }
       catch {
         case e:InterruptedException => println(e.printStackTrace())
       }
      numOfRetriesNew = numOfRetriesNew + 1
    }
    numOfRetries
  }
}
