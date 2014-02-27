package com.ee.midas.utils

import java.nio.file._
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._
import java.nio.file.StandardWatchEventKinds._
import java.io.File

class DirectoryWatcher(dirURL: String, watchEvents: Seq[WatchEvent.Kind[_]], waitBeforeProcessing: Long = 1000,
                       timeUnit: TimeUnit = MILLISECONDS, stopWatchingOnException: Boolean = true)(onEvents: Seq[WatchEvent[_]] => Unit)
  extends Loggable with Runnable {

  private val dirWatcherThread = new Thread(this, getClass.getSimpleName + "-Thread-" + dirURL)
  private val fileSystem = FileSystems.getDefault
  private val watcher = fileSystem.newWatchService()
  private val os = System.getProperty("os.name")

  val dirWatchMsg = s"Dir to Watch = $dirURL, OS = ${os}"
  println(dirWatchMsg)
  logInfo(dirWatchMsg)
  //Compensate for the bug that causes fileSystem.getPath to crash in Windows for dirURL
  private val path = if (os.contains("Win"))
                        fileSystem.getPath(dirURL.substring(1))
                     else
                        fileSystem.getPath(dirURL)

  registerAllDirectories(path)

  logInfo(s"Will Watch dir ${dirURL} for ${watchEvents} of Files...")
  
  var isRunning = true
  
  def stopWatching = {
    val stopWatchMsg = s"Stopping Watch on ${dirURL}"
    println(stopWatchMsg)
    logInfo(stopWatchMsg)
    watcher.close()
    isRunning = false
  }

  def start : Unit = {
     dirWatcherThread.start()
  }

  def isActive: Boolean = isRunning

  def forMoreEvents(waitTime: Long) = {
    Thread.sleep(waitTime)
  }

  def run: Unit = {
    while(isRunning) {
      try {
        logInfo(s"Watching ${dirURL}...")
        val watchKey = watcher.take()
        if(isRunning) {
            forMoreEvents(waitBeforeProcessing)

            val events = watchKey.pollEvents().asScala
            events.foreach { e =>
              logInfo(s"Detected ${e.kind()}, Context = ${e.context()}}")
              registerIfNewDirectoryCreated(e)
            }

            onEvents(events)
            val valid = watchKey.reset()
            if(!valid) {
              isRunning = Files.exists(path, LinkOption.NOFOLLOW_LINKS)
            }
          }
      } catch {
        case e: Exception =>
          logError(s"Closing it due to ${e.getMessage}. ${e.getStackTraceString}")
          if(stopWatchingOnException)
             stopWatching
      }
    }
    stopWatching
    logInfo(s"Closing Watch on ${dirURL}")
  }

  private def registerAllDirectories(dir: Path): Unit = {
    logInfo(s"Registering $dir with watcher.")
    dir.register(watcher, watchEvents.toArray)
    val subFolders = new File(dir.toUri).listFiles().filter(file => file.isDirectory) map { file => file.toPath}
    subFolders map registerAllDirectories
  }

  private def registerIfNewDirectoryCreated(event: WatchEvent[_]) = {
    if(event.kind().equals(ENTRY_CREATE)) {
      val newBornPath = path.resolve(event.context().asInstanceOf[Path])
      if(Files.isDirectory(newBornPath, LinkOption.NOFOLLOW_LINKS)) {
        registerAllDirectories(newBornPath)
      }
    }
  }

}
