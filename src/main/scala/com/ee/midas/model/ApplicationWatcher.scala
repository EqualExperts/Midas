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

package com.ee.midas.model

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
