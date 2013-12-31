package com.ee.midas.utils

import java.nio.file.{WatchEvent, FileSystems}
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._

class DirectoryWatcher(dirURL: String, events: Seq[WatchEvent.Kind[_]], waitBeforeProcessing: Long = 1000,
                       timeUnit: TimeUnit = MILLISECONDS)(onEvents: Seq[WatchEvent[_]] => Unit) extends Loggable with Runnable {
  private val dirWatcherThread = new Thread(this, getClass.getSimpleName + "-Thread")
  private val fileSystem = FileSystems.getDefault
  private val watcher = fileSystem.newWatchService()
  private val os = System.getProperty("os.name")

  log.info(s"Dir to Watch = $dirURL, OS = ${os}")
  //Compensate for the bug that causes fileSystem.getPath to crash in Windows for dirURL
  private val path = if (os.contains("Win"))
                        fileSystem.getPath(dirURL.substring(1))
                     else
                        fileSystem.getPath(dirURL)

  path.register(watcher, events.toArray)
  log.info(s"Will Watch dir ${dirURL} for ${events} of Files...")
  
  var isRunning = true
  
  def stopWatching = {
    log.info(s"Stopping Watch on ${dirURL}")
    isRunning = false
    watcher.close()
  }

  def start : Unit = {
     dirWatcherThread.start()
  }

  def forMoreEvents(waitTime: Long) = {
    Thread.sleep(waitTime)
  }

  def run: Unit = {
    var valid = true
    while(isRunning && valid) {
      try {
        log.info(s"Watching ${dirURL}...")
        val watchKey = watcher.take()
        forMoreEvents(waitBeforeProcessing)
        val events = watchKey.pollEvents().asScala
        events.foreach { e =>
          log.info(s"Detected ${e.kind()}, Context = ${e.context()}}")
        }
        onEvents(events)
        valid = watchKey.reset()
      } catch {
        case e: Exception =>
          log.error(s"Closing it due to ${e.getMessage}")
          stopWatching
      }
    }
    isRunning = false
    log.info(s"Completed Watch on ${dirURL}")
  }
}
