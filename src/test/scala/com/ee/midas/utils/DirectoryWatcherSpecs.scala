package com.ee.midas.utils

import java.io.{FileWriter, File}
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.{After, Before, Test}
import java.nio.file.StandardWatchEventKinds._

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

//TODO: Get Rid of hard-coded sleeps, this Specs needs refactoring
@RunWith(classOf[MockitoJUnitRunner])
class DirectoryWatcherSpecs extends JUnitMustMatchers {

  def waitForWatcherToStart(millis: Long) = Thread.sleep(millis)
  val path: String = "/" + System.getProperty("user.dir") + "/testWatcherDir"
  val directory = new File(path)
  directory.deleteOnExit()

  @Before
  def setUp() {
    if(!directory.exists())
      directory.mkdir()
  }

  @Test
  def itWatchesACreateEventForADirectory() {
    //given: a watcher with a directory to watch
    var watchedCreateEvent: Boolean = false
    val watcher = new DirectoryWatcher(path, List(ENTRY_CREATE), 0)(watchEvent => {
        watchedCreateEvent = true
      }
    )
    watcher.start

    //when: there is a create event
    val file = new File(path + "/createFile.txt")
    file.createNewFile()
    file.deleteOnExit()
    waitForWatcherToStart(200)
    watcher.stopWatching

    //then: it was captured by the watcher
    watcher.stopWatching
    while(watcher.isRunning)
    watchedCreateEvent must beTrue
  }

  @Test
  def itWatchesAModifyEventForADirectory() {
    //given: a watcher with a directory to watch, and an existing file in the directory
    val file = new File(path + "/modifyFile.txt")
    file.createNewFile()
    file.deleteOnExit()
    var watchedModifyEvent: Boolean = false
    val watcher = new DirectoryWatcher(path, List(ENTRY_MODIFY), 0)(watchEvent => {
      watchedModifyEvent = true
    })
    watcher.start

    //when: there is a modify event
    val writer = new FileWriter(file)
    writer.write("add some data.")
    writer.close()

    watcher.stopWatching
    while(watcher.isRunning)

    //then: it was captured by the watcher
    watchedModifyEvent must beTrue
  }

  @Test
  def itWatchesADeleteEventForADirectory() {
    //given: a watcher with a directory to watch, and an existing file in the directory
    val file = new File(path + "/deleteFile.txt")
    file.createNewFile()
    var watchedDeleteEvent: Boolean = false
    val watcher = new DirectoryWatcher(path, List(ENTRY_DELETE), 0)(watchEvent => {
      watchedDeleteEvent = true
    })
    watcher.start

    //when: there is a modify event
    file.delete()


    //then: it was captured by the watcher
    watcher.stopWatching
    while(watcher.isRunning)
    watchedDeleteEvent must beTrue
  }

  @Test
  def itWatchesCreateEventsOnMultipleFilesForADirectory() {
    //given: a watcher with a directory to watch
    var watchedCreateEvents: Int = 0
    val watcher = new DirectoryWatcher(path, List(ENTRY_CREATE), 0)(watchEvent => {
      watchedCreateEvents += 1
    }
    )
    watcher.start

    //when: there are multiple create events
    val file1 = new File(path + "/createFile1.txt")
    file1.createNewFile()
    file1.deleteOnExit()
    Thread.sleep(100)
    val file2 = new File(path + "/createFile2.txt")
    file2.createNewFile()
    file2.deleteOnExit()
    Thread.sleep(100)
    val file3 = new File(path + "/createFile3.txt")
    file3.createNewFile()
    file3.deleteOnExit()
    Thread.sleep(100)

    //then: they are captured by the watcher
    watcher.stopWatching
    while(watcher.isRunning)
    watchedCreateEvents === 3
  }

  @Test
  def itWatchesDeleteEventsOnMultipleFilesForADirectory() {
    //given: a watcher with a directory to watch
    var watchedDeleted: Int = 0
    val file1 = new File(path + "/deleteFile1.txt")
    file1.createNewFile()
    val file2 = new File(path + "/deleteFile2.txt")
    file2.createNewFile()
    val file3 = new File(path + "/deleteFile3.txt")
    file3.createNewFile()

    val watcher = new DirectoryWatcher(path, List(ENTRY_DELETE),0)(watchEvent => {
        watchedDeleted += 1
      }
    )
    watcher.start

    //when: there is a create event
    file1.delete()
    Thread.sleep(100)
    file2.delete()
    Thread.sleep(100)
    file3.delete()
    Thread.sleep(100)

    //then: it was captured by the watcher
    watcher.stopWatching
    while(watcher.isRunning)
    watchedDeleted === 3
  }

  @Test
  def itStopsWatchingADirectoryWhenRequested() {
    var watching: Boolean = false
    val watcher = new DirectoryWatcher(path, List(ENTRY_CREATE, ENTRY_DELETE))(watchEvent => {
        watching = true
      }
    )
    watcher.start
    watcher.stopWatching
    watcher.isRunning must beFalse
  }

  @Test
  def itStopsWatchingADirectoryInCaseOfAnException() {
    val watcher = new DirectoryWatcher(path, List(ENTRY_CREATE, ENTRY_DELETE), 0)(watchEvent => {
        println("Throwing exception")
        throw new Exception("Exception forcibly throw from within a test case.")
      }
    )
    watcher.start

    val file = new File(path + "/exceptionFile.txt")
    file.createNewFile()
    file.deleteOnExit()

    while(watcher.isRunning)  {
      Thread.sleep(100)
    }

    watcher.isRunning must beFalse
  }
}
