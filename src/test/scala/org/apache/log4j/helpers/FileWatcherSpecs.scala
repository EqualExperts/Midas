package org.apache.log4j.helpers

import org.specs2.mutable.Specification
import java.io.File
import java.util.concurrent.TimeUnit
import org.specs2.specification.Scope
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.apache.log4j.helpers.FileWatcher

@RunWith(classOf[JUnitRunner])
class FileWatcherSpecs extends Specification with Mockito with FileWatcher {

  def waitForWatcherToWatch(millis: Int) ={
    Thread.sleep(millis)
  }

  "File Watcher" should {

    "watch file and execute given action once during initialization" in {
      //given
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified() returns 1

      var timesExecuted = 0

      //when
      watch(file, 10, TimeUnit.MILLISECONDS) {
        timesExecuted += 1
      }

      //then: the action was performed once
      waitForWatcherToWatch(50)
      timesExecuted mustEqual 1
    }

    "watch file and execute given action whenever the file is modified" in {
      //given: A file
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified() returns 1 thenReturns 2

      //and: a FileWatcher running on it
      var timesExecuted = 0
      watch(file, 10, TimeUnit.MILLISECONDS) {
        timesExecuted += 1
      }

      //then: the action was performed twice
      waitForWatcherToWatch(50)
      timesExecuted mustEqual 2
    }
  }
}
