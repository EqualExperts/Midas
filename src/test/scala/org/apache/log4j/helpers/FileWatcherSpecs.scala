package com.ee.midas.utils

import org.specs2.mutable.Specification
import java.io.File
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito

@RunWith(classOf[JUnitRunner])
class FileWatcherSpecs extends Specification with Mockito {

  def waitForWatcherToWatch(millis: Int) = Thread.sleep(millis)

  "File Watcher" should {

    "start watching file and execute the given action when file is modified" in {
      //given
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified() returns 1 thenReturn 2

      var timesExecuted = 0
      val watcher = new FileWatcher(file, 10, TimeUnit.MILLISECONDS)(
        timesExecuted += 1
      )

      //when
      watcher.start

      //then: the action was performed once
      waitForWatcherToWatch(50)
      watcher.stop
      timesExecuted mustEqual 1
    }

    "watch file and execute given action twice as the file is modified twice" in {
      //given: A file
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified returns 1 thenReturns 2 thenReturns 3

      //and: a FileWatcher running on it
      var timesExecuted = 0
      val watcher = new FileWatcher(file, 10, TimeUnit.MILLISECONDS)(
        timesExecuted += 1
      )

      //when
      watcher.start

      //then
      waitForWatcherToWatch(50)
      watcher.stop
      timesExecuted mustEqual 2
    }

    "watch file and never execute given action when file is never modified" in {
      //given: A file
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified returns 1

      //and: a FileWatcher running on it
      var timesExecuted = 0
      val watcher = new FileWatcher(file, 10, TimeUnit.MILLISECONDS)(
        timesExecuted += 1
      )

      //when
      watcher.start

      //then
      waitForWatcherToWatch(50)
      watcher.stop
      timesExecuted mustEqual 0
    }

    "watch file and never execute again if given action throws an exception" in {
      //given: A file
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified returns 1 thenReturns 2 thenReturns 3

      //and: a FileWatcher running on it
      var timesExecuted = 0
      val watcher = new FileWatcher(file, 10, TimeUnit.MILLISECONDS) ({
        timesExecuted += 1
        throw new IllegalArgumentException("on purpose")
      })

      //when
      watcher.start

      //then
      waitForWatcherToWatch(50)
      watcher.stop
      timesExecuted mustEqual 1
    }

    "watch file and continue execution even if given action throws an exception" in {
      //given: A file
      val file = mock[File]
      file.exists() returns true
      file.getAbsolutePath returns "/some/path"
      file.lastModified returns 1 thenReturns 2 thenReturns 3

      //and: a FileWatcher running on it
      var timesExecuted = 0
      val watcher = new FileWatcher(file, 10, TimeUnit.MILLISECONDS, stopOnException = false) ({
        timesExecuted += 1
        throw new IllegalArgumentException("on purpose")
      })

      //when
      watcher.start

      //then
      waitForWatcherToWatch(50)
      watcher.stop
      timesExecuted mustEqual 2
    }
  }
}
