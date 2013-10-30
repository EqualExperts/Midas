package com.ee.midas.pipes

import java.io.IOException
import org.slf4j.{Logger, LoggerFactory}

class DuplexPipe private (val id: Long, private val request: SimplexPipe, private val response: SimplexPipe)
extends Pipe {
  val log = LoggerFactory.getLogger(getClass)
  val name = classOf[DuplexPipe].getSimpleName + "-%d".format(id)
  private val duplexGroup = new ThreadGroup(name)
  private val exceptionHandler = new UncaughtExceptionHandler(this)

  private val requestThread = new Thread(duplexGroup, request, threadName(request.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val responseThread = new Thread(duplexGroup, response, threadName(response.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private def threadName(name: String) = duplexGroup.getName + "-" + name + "-Thread"

  override def start: Unit = {
    log.info("Starting " +  toString)
    requestThread.start
    responseThread.start
  }

  def inspect : Unit = {

    log.info("Pipe Name = " + duplexGroup.getName())
    log.info("Active Threads = " + duplexGroup.activeCount())
    if(requestThread.isAlive) {
      log.info("Request Thread Id = " + requestThread.getId)
      log.info("Request Thread Name = " + requestThread.getName)
    }
    if(responseThread.isAlive) {
      log.info("Response Thread Name = " + responseThread.getName)
      log.info("Response Thread Id = " + responseThread.getId)
    }
  }
  def isActive = request.isActive && response.isActive

  def forceStop : Unit = {
    request.forceStop
    response.forceStop
  }
  
  def stop : Unit = {
    request.stop
    response.stop
  }

  override def toString  = name

  class UncaughtExceptionHandler(pipe: Pipe) extends Thread.UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, t: Throwable) : Unit = {
      val threadName = Thread.currentThread().getName
      t match {
        case e: IOException => {
          log.error("[" + threadName + "] UncaughtExceptionHandler Received IOException in %s".format(e.getMessage))
          log.error("[" + threadName + "] Closing pipe: " + pipe.name)
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
      val monitorLog: Logger = LoggerFactory.getLogger(getClass)
    }
}

