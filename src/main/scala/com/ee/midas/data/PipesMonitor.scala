package com.ee.midas.data


trait PipesMonitorComponent extends Startable {
  pipe: Pipe =>

  val checkEveryMillis: Long

  abstract override def start: Unit = {
    println("Starting Pipe..." + pipe.name)
    //Start Target First
    super.start
    val pipesMonitor = new PipesMonitor
    println("Starting PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.start
  }

  class PipesMonitor extends Thread(pipe.name + "-Monitor-Thread") {
    private var keepRunning = true

    def close = keepRunning = false

    override def run = {
      while (keepRunning) {
        try {
          pipe.dump
          if (!pipe.isActive) {
            val threadName = Thread.currentThread().getName()
            println("[" + threadName + "] Detected Broken Pipe...Initiating Pipe Closure")
            pipe.forceStop
            keepRunning = false
            println("[" + threadName + "] Shutting down Monitor")
          }
          Thread.sleep(checkEveryMillis)
        } catch {
          case e: InterruptedException => println("Status Thread Interrupted")
            keepRunning = false
        }
      }
    }

    override def toString = getName
  }
}
