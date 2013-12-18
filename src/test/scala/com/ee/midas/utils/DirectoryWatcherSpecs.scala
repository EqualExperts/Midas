package com.ee.midas.utils

import org.specs2.mutable.Specification
import java.io.File
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
object DirectoryWatcherSpecs extends Specification{

  def waitForWatcherToStart(millis: Long) = Thread.sleep(millis)

  "Directory watcher" should {
    "watch an event for a directory" in {
      val path = "/" + System.getProperty("user.dir")
      var watching: Boolean = false
      val watcher = new DirectoryWatcher(path)(watchEvent => {
          watching = true
        }
      )
      watcher.start
      val newFile = new File("dummyFile.txt")
      newFile.createNewFile()
      newFile.deleteOnExit()
      waitForWatcherToStart(200)
      watcher.stopWatching
      watching must beTrue
    }

    "stop watching a directory when requested" in {
      val path = "/" + System.getProperty("user.dir")
      var watching: Boolean = false
      val watcher = new DirectoryWatcher(path)(watchEvent => {
          watching = true
        }
      )
      watcher.start
      watcher.stopWatching
      watcher.isRunning must beFalse
    }

    "stop watching a directory in case of an exception" in {
      val path = "/" + System.getProperty("user.dir")
      val watcher = new DirectoryWatcher(path)(watchEvent => {
          throw new Exception()
        }
      )
      watcher.start
      waitForWatcherToStart(200)
      watcher.isRunning must beFalse
    }
  }
}
