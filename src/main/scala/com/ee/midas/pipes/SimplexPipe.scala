package com.ee.midas.pipes

import java.io.{OutputStream, InputStream}
import com.ee.midas.utils.Loggable

class SimplexPipe(val name: String, val src: InputStream,
                  val tgt: OutputStream, val interceptable: Interceptable = Interceptable())
  extends Pipe with Runnable with Loggable {
  val EOF = -1
  private var gracefulStop = false
  private var isRunning = false

  def start: Unit = {
    isRunning = true
    logInfo("Starting " +  toString)
  }

  override def run: Unit = {
    var bytesRead: Int = 0
    do {
      bytesRead = interceptable.intercept(src, tgt)
    } while (bytesRead != EOF && !gracefulStop)
    isRunning = false
  }

  def stop: Unit = gracefulStop = true

  def isActive = isRunning

  def forceStop: Unit = {
    val threadName = Thread.currentThread().getName()
    stop
    isRunning = false
    logInfo("[" + threadName + "] " + toString + ": Closing Streams...")
    src.close()
    tgt.close()
    logInfo("[" + threadName + "] " + toString + ": Closing Streams Done")
  }

  def inspect: Unit = {
    logInfo("Pipe Name = " + name)
    logInfo("isActive? = " + isActive)
  }

  override def toString  = getClass.getSimpleName + ":" + name
}
