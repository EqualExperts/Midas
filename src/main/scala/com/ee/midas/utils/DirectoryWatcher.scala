/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.utils

import java.nio.file._
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._
import java.nio.file.StandardWatchEventKinds._
import java.io.File
import com.sun.nio.file.SensitivityWatchEventModifier

class DirectoryWatcher(dirURL: String, watchEvents: Seq[WatchEvent.Kind[_]], watchEvery: Long = 1000,
                       unit: TimeUnit = MILLISECONDS, stopWatchingOnException: Boolean = true)(onEvents: Seq[WatchEvent[_]] => Unit)
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
  
  private var isRunning = false
  
  def stopWatching = {
    val stoppingWatchMsg = s"Stopping Watch on ${dirURL}"
    println(stoppingWatchMsg)
    logInfo(stoppingWatchMsg)
    isRunning = false
    watcher.close()
    val stoppedWatchMsg = s"Stopped Watch on ${dirURL}"
    println(stoppedWatchMsg)
    logInfo(stoppedWatchMsg)

  }

  def start: Unit = dirWatcherThread.start

  def isActive: Boolean = isRunning

  def waitForMoreEventsToAccumulate = unit.sleep(watchEvery)

  def run: Unit = {
    isRunning = true
    while(isRunning) {
      try {
        logInfo(s"Watching ${dirURL}...")
        if(isRunning) {
          logInfo(s"Waiting for Events..")
          val watchKey = watcher.take()
          waitForMoreEventsToAccumulate
          logInfo(s"Polling for Events...")
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
    dir.register(watcher, watchEvents.toArray, SensitivityWatchEventModifier.HIGH)
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
