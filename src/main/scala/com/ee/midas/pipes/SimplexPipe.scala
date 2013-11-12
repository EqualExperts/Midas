package com.ee.midas.pipes

import java.io.{OutputStream, InputStream}
import com.ee.midas.utils.Loggable

class SimplexPipe(val name: String, val src: InputStream, val dest: OutputStream)
  extends Pipe with Runnable with Loggable {
  val EOF = -1
  private var gracefulStop = false
  private var isRunning = false

  def start: Unit = {
    log.info("Starting " +  toString)
  }

  override def run: Unit = {
    isRunning = true
    var bytesRead = 0
    val data = new Array[Byte](1024 * 16)
    do {
      bytesRead = src.read(data)
      log.info(name + ", Bytes Read = " + bytesRead)
      if (bytesRead > 0) {
        dest.write(data, 0, bytesRead)
        log.info(name + ", Bytes Written = " + bytesRead)
        dest.flush
      }
    } while (bytesRead != EOF && !gracefulStop)
    isRunning = false
  }

  def stop : Unit = gracefulStop = true

  def isActive = isRunning

  def forceStop : Unit = {
    val threadName = Thread.currentThread().getName()
    log.info("[" + threadName + "] " + toString + ": Closing Streams...")
    src.close()
    dest.close()
    log.info("[" + threadName + "] " + toString + ": Closing Streams Done")
  }

  def inspect: Unit = {
    log.info("Pipe Name = " + name)
    log.info("isActive? = " + isActive)
  }

  override def toString  = getClass.getSimpleName + ":" + name
}
