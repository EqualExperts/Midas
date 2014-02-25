package com.ee.midas.utils

import java.nio.file.{WatchEvent, FileSystems}
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._

class DirectoryWatcher(dirURL: String, events: Seq[WatchEvent.Kind[_]], waitBeforeProcessing: Long = 1000,
                       timeUnit: TimeUnit = MILLISECONDS, stopWatchingOnException: Boolean = true)(onEvents: Seq[WatchEvent[_]] => Unit)
  extends Loggable with Runnable {

  private val dirWatcherThread = new Thread(this, getClass.getSimpleName + "-Thread-" + dirURL)
  private val fileSystem = FileSystems.getDefault
  private val watcher = fileSystem.newWatchService()
  private var isWatchServiceRunning = true
  private val os = System.getProperty("os.name")

  val dirWatchMsg = s"Dir to Watch = $dirURL, OS = ${os}"
  println(dirWatchMsg)
  logInfo(dirWatchMsg)
  //Compensate for the bug that causes fileSystem.getPath to crash in Windows for dirURL
  private val path = if (os.contains("Win"))
                        fileSystem.getPath(dirURL.substring(1))
                     else
                        fileSystem.getPath(dirURL)

  path.register(watcher, events.toArray)
  logInfo(s"Will Watch dir ${dirURL} for ${events} of Files...")
  
  var isRunning = true
  
  def stopWatching = {
    val stopWatchMsg = s"Stopping Watch on ${dirURL}"
    println(stopWatchMsg)
    logInfo(stopWatchMsg)
    isRunning = false
  }

  def start : Unit = {
     dirWatcherThread.start()
  }

  def isActive: Boolean = isRunning || isWatchServiceRunning

  def forMoreEvents(waitTime: Long) = {
    Thread.sleep(waitTime)
  }

  def run: Unit = {
    var valid = true
    while(isRunning && valid) {
      try {
        logInfo(s"Watching ${dirURL}...")
        val watchKey = watcher.take()
        forMoreEvents(waitBeforeProcessing)
        val events = watchKey.pollEvents().asScala
        events.foreach { e =>
          logInfo(s"Detected ${e.kind()}, Context = ${e.context()}}")
        }
        onEvents(events)
        valid = watchKey.reset()
      } catch {
        case e: Exception =>
          logError(s"Closing it due to ${e.getMessage}. ${e.getStackTraceString}")
          if(stopWatchingOnException)
             stopWatching
      }
    }
    stopWatching
    watcher.close()
    isWatchServiceRunning = false
    logInfo(s"Completed Watch on ${dirURL}")
  }
}
