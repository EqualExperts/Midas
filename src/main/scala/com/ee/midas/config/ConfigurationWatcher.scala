package com.ee.midas.config

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
