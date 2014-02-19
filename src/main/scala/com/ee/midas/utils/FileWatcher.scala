package com.ee.midas.utils

import org.apache.log4j.helpers.{FileWatchdog, LogLog}
import scala.concurrent.duration.TimeUnit

class FileWatcher(fileName: String, watchEvery: Int, watchUnit: TimeUnit)(actionOnModify: () => Unit) {

  val watcher = new FileWatchdog(fileName: String) {
    delay = watchUnit.toMillis(watchEvery)
    LogLog.debug(s"watching file $fileName for any modifications.")
    def doOnChange() = {
      LogLog.debug(s"Change detected for $fileName. Performing the specified action.")
      actionOnModify()
      LogLog.debug(s"Action performed successfully.")
    }
  }

  def start() = watcher.start()

}
