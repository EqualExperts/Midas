package com.ee.midas.utils

import org.specs2.mutable.Specification
import java.io.{FileWriter, File}
import java.util.concurrent.TimeUnit

class SingleFileWatcherSpecs extends Specification {

  sequential

  def waitForWatcherToWatch(millis: Int) ={
    Thread.sleep(millis)
  }

  "Single File Watcher" should {

    "perform given action once during initialization" in {
      //given: A file
      val file = new File("sample1.config")
      if(!file.exists()) file.createNewFile()
      file.deleteOnExit()
      var timesExecuted = 0
      def watchAction() {
        timesExecuted += 1
      }

      //when: A SingleFileWatcher is initialized
      new SingleFileWatcher(file.getAbsolutePath, 1, TimeUnit.SECONDS)(watchAction)

      //then: the action was performed once
      timesExecuted mustEqual 1
    }

    "perform given action whenever the file is modified" in {
      //given: A file
      val file = new File("sample2.config")
      if(!file.exists()) file.createNewFile()
      file.deleteOnExit()
      var timesExecuted = 0
      def watchAction() {
        timesExecuted += 1
      }

      //and: a SingleFileWatcher running on it
      val watcher = new SingleFileWatcher(file.getAbsolutePath, 5, TimeUnit.MILLISECONDS)(watchAction)
      watcher.start()

      //when: the file is modified
      val writer = new FileWriter(file)
      writer.write("some random data.")
      writer.flush()
      writer.close()

      //then: the action was performed twice
      waitForWatcherToWatch(20)
      timesExecuted mustEqual 2
    }

  }
}
