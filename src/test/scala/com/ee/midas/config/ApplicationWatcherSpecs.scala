package com.ee.midas.config

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.TransformType
import java.net.URL
import scala.util.Try
import java.util.concurrent.TimeUnit

@RunWith(classOf[JUnitRunner])
class ApplicationWatcherSpecs extends Specification with Mockito {

  sequential
  "Application Watcher" should {
    "start watching the deltas of given application" in new MidasConfigurationSetup {

      //Given
      val application = new Application(app1DirURL, "demoApp", TransformType.EXPANSION, Set[Node]())
      val appConfigText = s"""
            |demoApp {
            |  mode = contraction
            |  nodeA {
            |    ip = 127.0.0.1
            |    changeSet = 2
            |  }
            |}
          """.stripMargin
      override def after = {
        appWatcher.stopWatching
        super.after
      }
      write(appConfigText, app1Config)

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
      write("use someDb", app1ChangeSet01ExpansionDeltaFile)
      Thread.sleep(2000)
      appWatcher.isActive mustEqual true

      //Then
      updateWasInvoked mustEqual 1
    }

    "stop watching the deltas of given application" in new MidasConfigurationSetup {

      //Given
      val application = new Application(app2DirURL, "app2", TransformType.EXPANSION, Set[Node]())
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
