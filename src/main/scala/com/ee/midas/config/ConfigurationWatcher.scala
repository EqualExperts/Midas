package com.ee.midas.config

import java.io.File
import java.util.concurrent.TimeUnit
import org.apache.log4j.helpers.FileWatcher
import java.net.URI

class ConfigurationWatcher (private val configuration: Configuration, baseDeltasDir: URI) extends Watcher[Configuration]
with ConfigurationParser with FileWatcher {

  private val midasConfigFile = new File(baseDeltasDir.getPath + File.separator + Configuration.filename)
  
  def startWatching: Unit = watch(midasConfigFile, 2, TimeUnit.SECONDS) {
      val deltasDir = new File(baseDeltasDir.getPath).toURI.toURL
      parse(deltasDir, Configuration.filename) match {
      case scala.util.Failure(t) => throw new IllegalArgumentException(t)
      //todo: revisit this
      case scala.util.Success(newConfiguration) => configuration.update(newConfiguration)
    }
  }

  def stopWatching: Unit =
    throw new UnsupportedOperationException("Stop is not supported on configuration Watcher")
}
