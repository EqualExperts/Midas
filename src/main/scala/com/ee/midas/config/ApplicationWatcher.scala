package com.ee.midas.config

import com.ee.midas.utils.{DirectoryWatcher, Loggable}
import java.nio.file.StandardWatchEventKinds._
import java.util.concurrent.TimeUnit

class ApplicationWatcher (application: Application, val watchEvery: Long = 100, val unit : TimeUnit = TimeUnit.MILLISECONDS) extends Watcher[Application] with Loggable
with ApplicationParsers {
  val configDir = application.configDir

  private val watcher: DirectoryWatcher = {
    val dirWatchMsg = s"Setting up Directory Watcher for Application in ${configDir}..."
    println(dirWatchMsg)
    logInfo(dirWatchMsg)
    new DirectoryWatcher(application.configDir.getPath, List(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY),
      watchEvery, stopWatchingOnException = false)(watchEvents => {
      watchEvents.foreach { watchEvent =>
        logInfo(s"Received ${watchEvent.kind()}, Context = ${watchEvent.context()}")
      }

      parse(application.configDir) match {
        case scala.util.Success(newlyParsedApp) => application.update(newlyParsedApp)
        case scala.util.Failure(t) =>
          logError(s"Failed to parse Application Config because ${t.getMessage}")
          logError(s"Will Continue To Use Old Application Config")
      }
    })
  }

  def startWatching = watcher.start

  def stopWatching = watcher.stopWatching

  def isActive = watcher.isActive
}
