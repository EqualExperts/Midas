package com.ee.midas.data

import java.io.{IOException, OutputStream, InputStream}
import java.net.SocketException
import scala.beans.BeanProperty

class SimplexPipe(val name: String, val src: InputStream, val dest: OutputStream) extends Runnable {
  val EOF = -1
  private var gracefulStop = false

  private var isRunning = false

  override def run: Unit = {
    isRunning = true
    var bytesRead = 0
    val data = new Array[Byte](1024 * 16)
    do {
      bytesRead = src.read(data)
      println(name + ", Bytes Read = " + bytesRead)
      if (bytesRead > 0) {
        dest.write(data, 0, bytesRead)
        println(name + ", Bytes Written = " + bytesRead)
        dest.flush
      }
    } while (bytesRead != EOF && !gracefulStop)
    isRunning = false
  }

  def stop = gracefulStop = true

  def isActive = isRunning

  def close = {
    val threadName = Thread.currentThread().getName()
    println("["+ threadName+"] " + toString + ": Closing Streams...")
    src.close()
    dest.close()
    println("["+ threadName+"] " + toString + ": Closing Streams Done")
  }

  override def toString = getClass.getSimpleName + ":" + name
}
