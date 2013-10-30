package com.ee.midas.pipes

import org.slf4j.LoggerFactory


trait PipesMonitorComponent extends Startable with Stoppable {
  pipe: Pipe =>

  val checkEveryMillis: Long
  def log = LoggerFactory.getLogger(getClass)
  abstract override def start: Unit = {
    println("Starting Pipe..." + pipe.name)
    //Start Target First
    super.start
    val pipesMonitor = new PipesMonitor
    println("Starting PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.start
  }

  abstract override def stop: Unit = {
    log.info("Stopping Pipe..." + pipe.name)
    //Start Target First
    super.stop
    val pipesMonitor = new PipesMonitor
    log.info("Stopping PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.close
  }

  abstract override def forceStop: Unit = {
    log.info("Forcing Stop on Pipe..." + pipe.name)
    //Start Target First
    super.forceStop
    val pipesMonitor = new PipesMonitor
    log.info("Stopping PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.close
  }

  class PipesMonitor extends Thread(pipe.name + "-Monitor-Thread") {
    private var keepRunning = true

    def close = keepRunning = false

    override def run = {
      while (keepRunning) {
        try {
          pipe.inspect
          if (!pipe.isActive) {
            val threadName = Thread.currentThread().getName()
            println("[" + threadName + "] Detected Broken Pipe...Initiating Pipe Closure")
            pipe.forceStop
            keepRunning = false
            println("[" + threadName + "] Shutting down Monitor")
          }
          Thread.sleep(checkEveryMillis)
        } catch {
          case e: InterruptedException => log.error("Status Thread Interrupted")
            keepRunning = false
        }
      }
    }

    override def toString = getName
  }
}
