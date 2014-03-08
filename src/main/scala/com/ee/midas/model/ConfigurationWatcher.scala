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

import java.io.File
import java.util.concurrent.TimeUnit
import java.net.URI
import com.ee.midas.utils.FileWatcher

class ConfigurationWatcher (private val configuration: Configuration, baseDeltasDir: URI) extends Watcher[Configuration]
with ConfigurationParser {

  private val midasConfigFile = new File(baseDeltasDir.getPath + File.separator + Configuration.filename)

  private val watcher = new FileWatcher(midasConfigFile, 3, TimeUnit.SECONDS, stopOnException = false)({
    println("updating the configuration")
    val deltasDir = new File(baseDeltasDir.getPath).toURI.toURL
    parse(deltasDir, Configuration.filename) match {
      case scala.util.Failure(t) => throw new IllegalArgumentException(t)
      //todo: revisit this
      case scala.util.Success(newConfiguration) => configuration.update(newConfiguration)
    }
  })
  
  def startWatching: Unit = watcher.start

  def stopWatching: Unit = watcher.stop

  def isActive = watcher.isActive
}
