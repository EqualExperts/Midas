package com.ee.midas.utils

import org.specs2.mutable.{Specification}
import java.io.{FileWriter, File}
import java.util.concurrent.TimeUnit
import org.specs2.specification.{Scope}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileWatcherSpecs extends Specification {

  trait FileWatching extends FileWatcher with Scope {
    def createFile(fileName: String): File = {
      val file = new File(fileName)
      if (!file.exists()) file.createNewFile()
      file.deleteOnExit()
      file
    }
  }


  def waitForWatcherToWatch(millis: Int) ={
    Thread.sleep(millis)
  }

  sequential
  "File Watcher" should {

    "watch file and execute given action once during initialization" in new FileWatching {
      //given
      val file = createFile("sample1.config")
      var timesExecuted = 0

      //when
      watch(file.getAbsolutePath, 1, TimeUnit.SECONDS) {
        timesExecuted += 1
      }

      //then: the action was performed once
      timesExecuted mustEqual 1
    }

    "watch file and execute given action whenever the file is modified" in new FileWatching {
      //given: A file
      val file = createFile("sample2.config")
      var timesExecuted = 0

      //and: a FileWatcher running on it
      watch(file.getAbsolutePath, 30, TimeUnit.MILLISECONDS) {
        timesExecuted += 1
      }

      //when: the file is modified
      val writer = new FileWriter(file)
      writer.write("some random data.")
      writer.flush()
      writer.close()

      //then: the action was performed twice
      waitForWatcherToWatch(100)
      timesExecuted mustEqual 2
    }
  }
}
