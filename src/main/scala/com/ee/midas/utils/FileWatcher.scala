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

  private val watcherThread = new Thread(watch, s"FileWatcherThread-${file.getName}") {{
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

  def isActive = isRunning
}
