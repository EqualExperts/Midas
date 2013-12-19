package com.ee.midas.utils

import java.io.File
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.Test

/**
 * IMPORTANT NOTE:
 *
 * Specs are written in JUnit style because
 * 1) Conventional style causes the test cases to run multiple times, as described in the link below.
 * https://groups.google.com/forum/#!topic/play-framework/4Fz5TsOKPio
 * 2) This causes numerous problems while testing systems with multi-threaded environment.
 *
 * Also, @RunWith uses MockitoJUnitRunner because the conventional JUnitRunner provided by Specs2
 * is not compatible with JUnit style test cases written here.
 */
@RunWith(classOf[MockitoJUnitRunner])
class DirectoryWatcherSpecs extends JUnitMustMatchers{

  def waitForWatcherToStart(millis: Long) = Thread.sleep(millis)

  @Test
  def watchAnEventForADirectory() {
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

  @Test
  def stopWatchingADirectoryWhenRequested() {
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

  @Test
  def stopWatchingADirectoryInCaseOfAnException() {
    val path = "/" + System.getProperty("user.dir")
    val watcher = new DirectoryWatcher(path)(watchEvent => {
        throw new Exception("Exception forcibly throw from within a test case.")
      }
    )
    watcher.start
    val file = new File("exception.txt")
    file.createNewFile()
    file.deleteOnExit()
    waitForWatcherToStart(200)
    watcher.isRunning must beFalse
  }
}
