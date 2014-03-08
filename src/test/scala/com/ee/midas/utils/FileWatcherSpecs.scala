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
