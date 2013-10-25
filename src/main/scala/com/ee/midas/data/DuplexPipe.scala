package com.ee.midas.data

import java.io.IOException

class DuplexPipe private (val id: Long, val request: SimplexPipe, val response: SimplexPipe) {

  private val duplexGroup = new ThreadGroup(getClass.getSimpleName + "-%d".format(id))
  private val exceptionHandler = new UncaughtExceptionHandler(this)

  private val requestThread = new Thread(duplexGroup, request, getPipeName(request)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val responseThread = new Thread(duplexGroup, response, getPipeName(response)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val monitor = new PipesMonitor(this, 2000)
  private val monitorThread = new Thread(duplexGroup, monitor, "PipesMonitorThread")

  private def getPipeName(pipe: SimplexPipe) =
    duplexGroup.getName + "-" + pipe.toString

  def start = {
    requestThread.start
    responseThread.start
    monitorThread.start
  }
  
  def isActive = {
    println("ThreadGroup name = " + duplexGroup.getName())
    println("Active Threads = " + duplexGroup.activeCount())
    if(requestThread.isAlive) {
      println("Request Thread Id = " + requestThread.getId)
      println("Request Thread Name = " + requestThread.getName)
    }
    if(responseThread.isAlive) {
      println("Response Thread Name = " + responseThread.getName)
      println("Response Thread Id = " + responseThread.getId)
    }
    request.isActive && response.isActive
  }

  def close = {
    request.close
    response.close
  }
  
  class PipesMonitor (duplexPipe: DuplexPipe, val monitorEveryMillis: Long) extends Runnable {
    private var keepRunning = true

    def close = keepRunning = false

    override def run = {
      while(keepRunning) {
        try {
          if(!duplexPipe.isActive) {
            val threadName = Thread.currentThread().getName()
            println("["+ threadName+"] " + "Detected Broken Pipe...Initiating Duplex Pipe Closure")
            duplexPipe.close
            keepRunning = false
            println("["+ threadName+"] " + "Shutting down Monitor")
          }            
          Thread.sleep(monitorEveryMillis)
        } catch {
          case e: InterruptedException => println( "Status Thread Interrupted")
            keepRunning = false
        }
      }
    }
  }

  class UncaughtExceptionHandler(pipe: DuplexPipe) extends Thread.UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, t: Throwable) : Unit = {
      val threadName = Thread.currentThread().getName
      t match {
        case e: IOException => {
          println("["+ threadName + "] " + "UncaughtExceptionHandler Received IOException in %s".format(e.getMessage))
          println("["+ threadName + "] " + "Closing pipe: " + pipe.duplexGroup.getName)
        }
      }
    }
  }
}

object DuplexPipe {
  private val id = new java.util.concurrent.atomic.AtomicLong(1)

  private def nextId = id.getAndIncrement

  def apply(request: SimplexPipe, response: SimplexPipe) = new DuplexPipe(nextId, request, response)
}

