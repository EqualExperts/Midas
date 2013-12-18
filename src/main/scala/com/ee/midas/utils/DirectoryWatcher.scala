package com.ee.midas.utils

import java.nio.file.{WatchEvent, FileSystems}
import java.nio.file.StandardWatchEventKinds._
import scala.collection.JavaConverters._

class DirectoryWatcher(dirURL: String)(onEvent: WatchEvent[_] => Unit) extends Loggable with Runnable {
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

  path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
  log.info(s"Will Watch dir ${dirURL} for Creation, Modification and Deletion of Files...")
  
  var isRunning = true
  
  def stopWatching = {
    log.info(s"Stopping Watch on ${dirURL}")
    isRunning = false
    watcher.close()
  }

  def start : Unit = {
     dirWatcherThread.start()
  }
  
  def run: Unit = {
    var valid = true
    while(isRunning && valid) {
      try {
        log.info(s"Watching ${dirURL}...")
        val watchKey = watcher.take()
        val events = watchKey.pollEvents().asScala
        events.foreach { e =>
          log.info(s"Detected ${e.kind()}, Context = ${e.context()}}")
          onEvent(e)
        }
        valid = watchKey.reset()
      } catch {
        case e: Exception =>
          log.error(s"Closing it due to ${e.getMessage}")
          stopWatching
//          watcher.close()
      }
    }
    isRunning = false
    log.info(s"Completed Watch on ${dirURL}")
  }
}
