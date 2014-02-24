package org.apache.log4j.helpers

import java.io.File
import scala.concurrent.duration._

trait FileWatcher {
  def watch(input: File, watchEvery: Int, watchUnit: TimeUnit)(actionOnModify: => Any): FileWatchdog = {
    val fileName = input.getAbsolutePath
    val watcher = new FileWatchdog(fileName: String) {
      delay = watchUnit.toMillis(watchEvery)
      setName(s"FileWatcherThread-$fileName")
      this.file = input

      LogLog.debug(s"watching file $fileName for any modifications.")
      def doOnChange = {
        LogLog.debug(s"Change detected for $fileName. Performing the specified action.")
        actionOnModify
        LogLog.debug(s"Action performed successfully.")
      }
    }
    watcher.start()
    watcher
  }
}
