package com.ee.midas.utils

import java.io.File
import java.util.concurrent.TimeUnit

class FileWatcher(file: File, watchEvery: Long = 1000, unit: TimeUnit, stopOnException: Boolean = true)(onModification: => Unit)
extends Loggable {
  private var isRunning = false

  private val watch = new Runnable {
    def run = {
      var lastModified = file.lastModified
      while(isRunning) {
        unit.sleep(watchEvery)
        val currentModification = file.lastModified
        if(lastModified != currentModification) {
          lastModified = currentModification
          try {
            onModification
          } catch {
            case t: Throwable if (!stopOnException) =>
          }
        }
      }
    }
  }

  private val exceptionHandler = new UncaughtExceptionHandler(this)

  private val watcherThread = new Thread(watch, "FileWatcherThread") {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  class UncaughtExceptionHandler(watcher: FileWatcher) extends Thread.UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, t: Throwable) : Unit = {
      val threadName = Thread.currentThread().getName
      t match {
        case t: Throwable =>
          logError(s"[ $threadName UncaughtExceptionHandler Received Exception in ${t.getMessage}", t)
          if(stopOnException)
            watcher.stop
      }
    }
  }


  def start = {
    isRunning = true
    watcherThread.start
  }

  def stop = isRunning = false
}
