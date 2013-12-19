package com.ee.midas.utils

import java.io.{FileWriter, File}
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.{Before, Test}

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
  val path: String = "/" + System.getProperty("user.dir")

  @Test
  def itWatchesACreateEventForADirectory() {
    //given: a watcher with a directory to watch
    var watching: Boolean = false
    val watcher = new DirectoryWatcher(path)(watchEvent => {
        watching = true
      }
    )
    watcher.start

    //when: there is a create event
    val file = new File("createFile.txt")
    file.createNewFile()
    file.deleteOnExit()
    waitForWatcherToStart(200)
    watcher.stopWatching

    //then: it was captured by the watcher
    watching must beTrue
  }

  @Test
  def itWatchesAModifyEventForADirectory() {
    //given: a watcher with a directory to watch, and an existing file in the directory
    val file = new File("modifyFile.txt")
    file.createNewFile()
    file.deleteOnExit()
    var watching: Boolean = false
    val watcher = new DirectoryWatcher(path)(watchEvent => {
      watching = true
    })
    watcher.start

    //when: there is a modify event
    val writer = new FileWriter(file)
    writer.write("add some data.")
    writer.close()

    waitForWatcherToStart(200)
    watcher.stopWatching

    //then: it was captured by the watcher
    watching must beTrue
  }

  @Test
  def itWatchesADeleteEventForADirectory() {
    //given: a watcher with a directory to watch, and an existing file in the directory
    val file = new File("deleteFile.txt")
    file.createNewFile()
    var watching: Boolean = false
    val watcher = new DirectoryWatcher(path)(watchEvent => {
      watching = true
    })
    watcher.start

    //when: there is a modify event
    file.delete()

    waitForWatcherToStart(200)
    watcher.stopWatching

    //then: it was captured by the watcher
    watching must beTrue
  }

  @Test
  def itWatchesEventsOnMultipleFilesForADirectory() {
    //given: a watcher with a directory to watch
    var watching: Int = 0
    val watcher = new DirectoryWatcher(path)(watchEvent => {
      watching += 1
    }
    )
    watcher.start

    //when: there is a create event
    val file1 = new File("createFile1.txt")
    file1.createNewFile()
    file1.deleteOnExit()
    val file2 = new File("createFile2.txt")
    file2.createNewFile()
    file2.deleteOnExit()
    val file3 = new File("createFile3.txt")
    file3.createNewFile()
    file3.deleteOnExit()
    waitForWatcherToStart(200)
    watcher.stopWatching

    //then: it was captured by the watcher
    watching === 3
  }

  @Test
  def itWatchesDeleteEventsOnMultipleFilesForADirectory() {
    //given: a watcher with a directory to watch
    var deleted: Int = 0
    val file1 = new File("deleteFile1.txt")
    file1.createNewFile()
    val file2 = new File("deleteFile2.txt")
    file2.createNewFile()
    val file3 = new File("deleteFile3.txt")
    file3.createNewFile()

    val watcher = new DirectoryWatcher(path)(watchEvent => {
      deleted += 1
    }
    )
    watcher.start

    //when: there is a create event
    file1.delete()
    file2.delete()
    file3.delete()
    waitForWatcherToStart(200)
    watcher.stopWatching

    //then: it was captured by the watcher
    deleted === 3
  }

  @Test
  def stopWatchingADirectoryWhenRequested() {
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
    val watcher = new DirectoryWatcher(path)(watchEvent => {
        throw new Exception("Exception forcibly throw from within a test case.")
      }
    )
    watcher.start
    val file = new File("exceptionFile.txt")
    file.createNewFile()
    file.deleteOnExit()
    waitForWatcherToStart(200)
    watcher.isRunning must beFalse
  }
}
