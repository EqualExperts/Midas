package com.ee.midas.data

import java.io.IOException

class DuplexPipe private (val id: Long, private val request: SimplexPipe, private val response: SimplexPipe)
extends Pipe {
  val name = classOf[DuplexPipe].getSimpleName + "-%d".format(id)
  private val duplexGroup = new ThreadGroup(name)
  private val exceptionHandler = new UncaughtExceptionHandler(this)

  private val requestThread = new Thread(duplexGroup, request, fullName(request.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val responseThread = new Thread(duplexGroup, response, fullName(response.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private def fullName(name: String) = duplexGroup.getName + "-" + name + "-Thread"

  override def start: Unit = {
    println("Starting " +  toString)
    requestThread.start
    responseThread.start
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
  }
  
  def isActive = request.isActive && response.isActive

  def forceStop = {
    request.forceStop
    response.forceStop
  }
  
  def stop = {
    request.stop
    response.stop
  }

  override def toString = name

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

  def apply(request: SimplexPipe, response: SimplexPipe) =
    new DuplexPipe(nextId, request, response) with PipesMonitorComponent {
      val checkEveryMillis: Long = 3000
    }
}

