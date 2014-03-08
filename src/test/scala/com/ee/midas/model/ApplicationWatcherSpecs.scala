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

package com.ee.midas.model

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.URL
import scala.util.Try
import java.util.concurrent.TimeUnit
import java.io.File

@RunWith(classOf[JUnitRunner])
class ApplicationWatcherSpecs extends Specification with Mockito {

  trait ApplicationWatcherSetup extends EntireConfigurationSetup {

    def createChangeSet(appName: String) = {
      val appChangeSet01 = new File(s"${deltasDir.getAbsolutePath}/${appName}/001-ChangeSet")
      val appChangeSet01Expansion = new File(appChangeSet01.getAbsolutePath + "/expansion")
      val appChangeSet01Contraction = new File(appChangeSet01.getAbsolutePath + "/contraction")
      val appChangeSet01ExpansionDeltaFile = new File(appChangeSet01Expansion.getPath + "/01-expansion.delta")
      val appChangeSet01ContractionDeltaFile = new File(appChangeSet01Contraction.getPath + "/01-contraction.delta")

      appChangeSet01Expansion.mkdirs()
      appChangeSet01Contraction.mkdirs()
      appChangeSet01ExpansionDeltaFile.createNewFile()
      appChangeSet01ContractionDeltaFile.createNewFile()
      (appChangeSet01ExpansionDeltaFile, appChangeSet01ContractionDeltaFile)
    }

  }

  sequential
  "Application Watcher" should {
    "start watching the deltas of given application" in new ApplicationWatcherSetup {

      //Given
      val deltasDir = new File("test-data/appWatcherSpecs/startWatching")
      val (application, _, _) = createApplications
      val (expansionDeltaFile, contractionDeltaFile) = createChangeSet(appName1)
      override def after = {
        appWatcher.stopWatching
        super.after
      }

      var updateWasInvoked = 0
      val appWatcher = new ApplicationWatcher(application, watchEvery = 1, unit = TimeUnit.SECONDS) {
        override def parse(url: URL) = Try {
          updateWasInvoked += 1
          application
        }
      }

      //When
      appWatcher.startWatching
      Thread.sleep(2000)

      //And
      write("use someDb", expansionDeltaFile)
      Thread.sleep(2000)
      appWatcher.isActive mustEqual true

      //Then
      updateWasInvoked mustEqual 1
    }

    "stop watching the deltas of given application" in new ApplicationWatcherSetup {

      //Given
      val deltasDir = new File("test-data/appWatcherSpecs/stopWatching")
      val (application, _, _) = createApplications
      val (expansionDeltaFile, contractionDeltaFile) = createChangeSet(appName1)

      val appWatcher = new ApplicationWatcher(application)
      appWatcher.startWatching
      Thread.sleep(500)

      //When
      appWatcher.stopWatching

      //Then
      Thread.sleep(500)
      appWatcher.isActive must beFalse
    }

  }

}
