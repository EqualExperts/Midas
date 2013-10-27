package com.ee.midas.data

import java.io.IOException

class DuplexPipe private (val id: Long, private val request: SimplexPipe, private val response: SimplexPipe) {
  val name = getClass.getSimpleName + "-%d".format(id)
  private val duplexGroup = new ThreadGroup(name)
  private val exceptionHandler = new UncaughtExceptionHandler(this)

  private val requestThread = new Thread(duplexGroup, request, fullName(request.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val responseThread = new Thread(duplexGroup, response, fullName(response.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val monitor = new PipesMonitor(this, checkEveryMillis = 5000)
  private val monitorThread = new Thread(duplexGroup, monitor, fullName("Monitor"))

  private def fullName(name: String) = duplexGroup.getName + "-" + name + "-Thread"

  def start = {
    requestThread.start
    responseThread.start
    monitorThread.start
  }

  def dump : Unit = {
    println("Pipe Name = " + duplexGroup.getName())
    println("Active Threads = " + duplexGroup.activeCount())
    if(requestThread.isAlive) {
      println("Request Thread Id = " + requestThread.getId)
      println("Request Thread Name = " + requestThread.getName)
    }
    if(responseThread.isAlive) {
      println("Response Thread Name = " + responseThread.getName)
      println("Response Thread Id = " + responseThread.getId)
    }
    if(monitorThread.isAlive) {
      println("Monitor Thread Name = " + monitorThread.getName)
      println("Monitor Thread Id = " + monitorThread.getId)
    }
  }
  
  def isActive = request.isActive && response.isActive

  def forceStop = {
    request.forceStop
    response.forceStop
  }

  override def toString = name
  
  class PipesMonitor (duplexPipe: DuplexPipe, val checkEveryMillis: Long = 2000) extends Runnable {
    private var keepRunning = true

    def close = keepRunning = false

    override def run = {
      while(keepRunning) {
        try {
          duplexPipe.dump
          if(!duplexPipe.isActive) {
            val threadName = Thread.currentThread().getName()
            println("[" + threadName + "] Detected Broken Pipe...Initiating Duplex Pipe Closure")
            duplexPipe.forceStop
            keepRunning = false
            println("["+ threadName + "] Shutting down Monitor")
          }            
          Thread.sleep(checkEveryMillis)
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
          println("["+ threadName + "] UncaughtExceptionHandler Received IOException in %s".format(e.getMessage))
          println("["+ threadName + "] Closing pipe: " + pipe.duplexGroup.getName)
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

