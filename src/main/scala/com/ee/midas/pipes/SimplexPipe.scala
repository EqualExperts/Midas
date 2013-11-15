package com.ee.midas.pipes

import java.io.{OutputStream, InputStream}
import com.ee.midas.utils.{Loggable}

class SimplexPipe(val name: String, val src: InputStream, val tgt: OutputStream, val interceptable: Interceptable = Interceptable())
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
    do {
      bytesRead = interceptable.intercept(src, tgt)
    } while (bytesRead != EOF && !gracefulStop)
    isRunning = false
  }

  def stop : Unit = gracefulStop = true

  def isActive = isRunning

  def forceStop : Unit = {
    val threadName = Thread.currentThread().getName()
    log.info("[" + threadName + "] " + toString + ": Closing Streams...")
    src.close()
    tgt.close()
    log.info("[" + threadName + "] " + toString + ": Closing Streams Done")
  }

  def inspect: Unit = {
    log.info("Pipe Name = " + name)
    log.info("isActive? = " + isActive)
  }

  override def toString  = getClass.getSimpleName + ":" + name
}
