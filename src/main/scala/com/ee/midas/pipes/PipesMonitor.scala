package com.ee.midas.pipes

import org.slf4j.Logger


trait PipesMonitorComponent extends Startable with Stoppable {
  pipe: Pipe =>

  val checkEveryMillis: Long
  val monitorLog : Logger
  abstract override def start: Unit = {
    monitorLog.info("Starting Pipe..." + pipe.name)
    //Start Target First
    super.start
    val pipesMonitor = new PipesMonitor
    monitorLog.info("Starting PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.start
  }

  abstract override def stop: Unit = {
    monitorLog.info("Stopping Pipe..." + pipe.name)
    //Start Target First
    super.stop
    val pipesMonitor = new PipesMonitor
    monitorLog.info("Stopping PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.close
  }

  abstract override def forceStop: Unit = {
    monitorLog.info("Forcing Stop on Pipe..." + pipe.name)
    //Start Target First
    super.forceStop
    val pipesMonitor = new PipesMonitor
    monitorLog.info("Stopping PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.close
  }

  class PipesMonitor extends Thread(pipe.name + "-Monitor-Thread") {
    private var keepRunning = true

    def close : Unit = keepRunning = false

    override def run = {
      while (keepRunning) {
        try {
          pipe.inspect
          if (!pipe.isActive) {
            val threadName = Thread.currentThread().getName()
            monitorLog.error("[" + threadName + "] Detected Broken Pipe...Initiating Pipe Closure")
            pipe.forceStop
            keepRunning = false
            monitorLog.error("[" + threadName + "] Shutting down Monitor")
          }
          Thread.sleep(checkEveryMillis)
        } catch {
          case e: InterruptedException => monitorLog.error("Status Thread Interrupted")
            keepRunning = false
        }
      }
    }

    override def toString = getName
  }
}
