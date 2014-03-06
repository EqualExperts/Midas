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

import com.ee.midas.utils.Loggable


trait PipesMonitorComponent extends Startable with Stoppable with Loggable {
  pipe: Pipe =>

  val checkEveryMillis: Long
  val pipesMonitor = new PipesMonitor

  abstract override def start: Unit = {
    logInfo("Starting Pipe..." + pipe.name)
    //Start Target First
    super.start
    logInfo("Starting PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.start
  }

  abstract override def stop: Unit = {
    logInfo("Stopping Pipe..." + pipe.name)
    //Stop Target First
    super.stop
    logInfo("Stopping PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.close
  }

  abstract override def forceStop: Unit = {
    logInfo("Forcing Stop on Pipe..." + pipe.name)
    //ForceStop Target First
    super.forceStop
    logInfo("Stopping PipesMonitor..." + pipesMonitor.toString)
    pipesMonitor.close
  }

  class PipesMonitor extends Thread(pipe.name + "-Monitor-Thread") {
    private var keepRunning = true

    def close : Unit = keepRunning = false

    override def run = {
      while (keepRunning) {
        try {
          pipe.inspect
          Thread.sleep(checkEveryMillis)
          if (!pipe.isActive) {
            val threadName = Thread.currentThread().getName()
            logError("[" + threadName + "] Detected Broken Pipe...Initiating Pipe Closure")
            pipe.forceStop
            keepRunning = false
            logError("[" + threadName + "] Shutting down Monitor")
          }
        } catch {
          case e: InterruptedException => logError("Status Thread Interrupted")
            keepRunning = false
        }
      }
    }

    override def toString = getName
  }
}
