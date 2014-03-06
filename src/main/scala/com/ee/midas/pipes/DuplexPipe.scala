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

package com.ee.midas.pipes

import java.io.IOException
import com.ee.midas.utils.Loggable

class DuplexPipe private (val id: Long, private val request: SimplexPipe, private val response: SimplexPipe)
extends Pipe with Loggable {
  val name = classOf[DuplexPipe].getSimpleName + "-%d".format(id)
  private val duplexGroup = new ThreadGroup(name)
  private val exceptionHandler = new UncaughtExceptionHandler(this)

  private val requestThread = new Thread(duplexGroup, request, threadName(request.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private val responseThread = new Thread(duplexGroup, response, threadName(response.toString)) {{
    setUncaughtExceptionHandler(exceptionHandler)
  }}

  private def threadName(name: String) = duplexGroup.getName + "-" + name + "-Thread"

  override def start: Unit = {
    request.start
    response.start
    logInfo("Starting " +  toString)
    requestThread.start
    responseThread.start
  }

  def inspect : Unit = {

    logInfo("Pipe Name = " + duplexGroup.getName())
    logInfo("Active Threads = " + duplexGroup.activeCount())
    if(requestThread.isAlive) {
      logInfo("Request Thread Id = " + requestThread.getId)
      logInfo("Request Thread Name = " + requestThread.getName)
    }
    if(responseThread.isAlive) {
      logInfo("Response Thread Name = " + responseThread.getName)
      logInfo("Response Thread Id = " + responseThread.getId)
    }
  }
  def isActive = request.isActive && response.isActive

  def forceStop : Unit = {
    request.forceStop
    response.forceStop
  }
  def stop : Unit = {
    request.stop
    response.stop
  }

  override def toString = name

  class UncaughtExceptionHandler(pipe: Pipe) extends Thread.UncaughtExceptionHandler {
    def uncaughtException(thread: Thread, t: Throwable) : Unit = {
      val threadName = Thread.currentThread().getName
      t match {
        case e: IOException =>
          logError(s"[ $threadName UncaughtExceptionHandler Received IOException in ${e.getMessage}", e)
        case _ =>
          logError(s"[ $threadName ] UncaughtExceptionHandler Received Exception:${t.getClass.getName} in ${t.getMessage}", t)
      }
      logError(s"[ $threadName ] Closing pipe: ${pipe.name}")
      pipe.forceStop
    }
  }
}

object DuplexPipe {
  private val id = new java.util.concurrent.atomic.AtomicLong(1)

  private def nextId = id.getAndIncrement

  def apply(request: SimplexPipe, response: SimplexPipe) =
    new DuplexPipe(nextId, request, response) with PipesMonitorComponent {
      logFor(classOf[DuplexPipe])
      val checkEveryMillis: Long = 3000
    }
}

