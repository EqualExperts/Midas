/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.utils

import java.io.{FileWriter, File}
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.{After, Before, Test}
import java.nio.file.StandardWatchEventKinds._
import java.nio.file.{StandardOpenOption, Files}

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

  private val os = System.getProperty("os.name")

  def waitForWatcherToStart(watcher: DirectoryWatcher, millis: Long) =
    while(!watcher.isActive)  {
      Thread.sleep(millis)
    }

  def waitForWatcherToCaptureEvent(millis: Long) = {
    /*
    Mac uses PollingWatcherService and not native and is very slow even after increasing
    Sensitivity to HIGH.  so increase delay time for Macs
     */
    if (os.contains("Mac"))
      Thread.sleep(millis * 10)
    else
      Thread.sleep(millis)
  }

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
    waitForWatcherToStart(watcher, 100)

    //when: there is a create event
    val file = new File(path + "/createFile.txt")
    file.createNewFile()
    file.deleteOnExit()
    waitForWatcherToCaptureEvent(200)

    //then: it was captured by the watcher
    watcher.stopWatching
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
    waitForWatcherToStart(watcher, 100)

    //when: there is a modify event
    val data = "add some data."
    Files.write(file.toPath, data.getBytes, StandardOpenOption.APPEND)
    waitForWatcherToCaptureEvent(300)

    //then: it was captured by the watcher
    watcher.stopWatching
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
    waitForWatcherToStart(watcher, 100)

    //when: there is a modify event
    file.delete()
    waitForWatcherToCaptureEvent(200)

    //then: it was captured by the watcher
    watcher.stopWatching
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
    waitForWatcherToStart(watcher, 100)

    //when: there are multiple create events
    val file1 = new File(path + "/createFile1.txt")
    file1.createNewFile()
    file1.deleteOnExit()
    waitForWatcherToCaptureEvent(200)
    val file2 = new File(path + "/createFile2.txt")
    file2.createNewFile()
    file2.deleteOnExit()
    waitForWatcherToCaptureEvent(200)
    val file3 = new File(path + "/createFile3.txt")
    file3.createNewFile()
    file3.deleteOnExit()
    waitForWatcherToCaptureEvent(200)

    //then: they are captured by the watcher
    watcher.stopWatching
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
    waitForWatcherToStart(watcher, 100)

    //when: there is a create event
    file1.delete()
    waitForWatcherToCaptureEvent(200)
    file2.delete()
    waitForWatcherToCaptureEvent(200)
    file3.delete()
    waitForWatcherToCaptureEvent(200)

    //then: it was captured by the watcher
    watcher.stopWatching
    watchedDeleted === 3
  }

  @Test
  def itStopsWatchingADirectoryWhenRequested() {
    //Given
    var watching: Boolean = false
    val watcher = new DirectoryWatcher(path, List(ENTRY_CREATE, ENTRY_DELETE))(watchEvent => {
        watching = true
      }
    )
    watcher.start
    waitForWatcherToStart(watcher, 100)

    //When
    watcher.stopWatching

    //Then
    watcher.isActive must beFalse
  }

  @Test
  def itStopsWatchingADirectoryInCaseOfAnException() {
    //Given
    val watcher = new DirectoryWatcher(path, List(ENTRY_CREATE, ENTRY_DELETE), 0)(watchEvent => {
        println("Throwing exception")
        throw new Exception("Exception forcibly throw from within a test case.")
      }
    )
    watcher.start
    waitForWatcherToStart(watcher, 100)

    //When
    val file = new File(path + "/exceptionFile.txt")
    file.createNewFile()
    file.deleteOnExit()

    waitForWatcherToCaptureEvent(200)

    //Then
    watcher.isActive must beFalse
  }
}
