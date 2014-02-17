package com.ee.midas.utils

import java.nio.file.{Path, FileSystems, WatchEvent}
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._
import scala.collection.JavaConverters._
import java.io.File

class FileWatcher(fileURL: String, events: Seq[WatchEvent.Kind[_]], waitBeforeProcessing: Long = 1000,
                       timeUnit: TimeUnit = MILLISECONDS, stopWatchingOnException: Boolean = true)(onEvents: Seq[WatchEvent[_]] => Unit)
  extends Loggable with Runnable {

  private val fileWatcherThread = new Thread(this, getClass.getSimpleName + "-Thread")
  private val fileSystem = FileSystems.getDefault
  private val watcher = fileSystem.newWatchService()
  private val os = System.getProperty("os.name")

  val fileWatchMsg = s"file to Watch = $fileURL, OS = ${os}"
  println(fileWatchMsg)
  logInfo(fileWatchMsg)
  //Compensate for the bug that causes fileSystem.getPath to crash in Windows for dirURL
  val indexOfSeparator = fileURL.lastIndexOf(File.separator)
  private val dirPath = if (os.contains("Win"))
    fileSystem.getPath(fileURL.substring(1, indexOfSeparator))
  else
    fileSystem.getPath(fileURL.substring(0, indexOfSeparator))

  dirPath.register(watcher, events.toArray)
  println("dirrrrrrrrrrrrrr path == "+dirPath)
  private val file: Path = fileSystem.getPath(fileURL.substring(indexOfSeparator+1))

  logInfo(s"Will Watch file ${fileURL} for ${events} of Files...")

  var isRunning = true

  def stopWatching = {
    val stopWatchMsg = s"Stopping Watch on ${fileURL}"
    println(stopWatchMsg)
    logInfo(stopWatchMsg)
    watcher.close()
    isRunning = false
  }

  def start : Unit = {
    fileWatcherThread.start()
  }

  def forMoreEvents(waitTime: Long) = {
    Thread.sleep(waitTime)
  }

  def run: Unit = {
    var valid = true
    while(isRunning && valid) {
      try {
        logInfo(s"Watching ${fileURL}...")
        val watchKey = watcher.take()
        forMoreEvents(waitBeforeProcessing)
        val events = watchKey.pollEvents().asScala
        events.foreach { e =>
          logInfo(s"myyyyyy  Detected ${e.kind()}, Context = ${e.context()}}")
          println("fileeeeeeeeeeeeeeee  === "+file)
          val context: Path = (e.context).asInstanceOf[Path]
          if(context.endsWith(file)) {
            onEvents(events)
            valid = watchKey.reset()
          }
        }
      } catch {
        case e: Exception =>
          logError(s"Closing it due to ${e.getMessage}")
          if(stopWatchingOnException)
            stopWatching
      }
    }
    stopWatching
    logInfo(s"Completed Watch on ${fileURL}")
  }
}
