package com.ee.midas.config

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class ApplicationWatcherSpecs extends Specification with Mockito {

  "Application Watcher" should {
    "start watching the deltas of given application" in new ConfigSetup {
      val application = mock[Application]
      val appConfigText = s"""
            |demoApp {
            |  mode = contraction
            |  nodeA {
            |    ip = 127.0.0.1
            |    changeSet = 2
            |  }
            |}
          """.stripMargin
      writeToFile(app1Config, appConfigText)

      application.configDir returns app1DirURL
      val appWatcher = new ApplicationWatcher(application)

      appWatcher.startWatching
      Thread.sleep(2000)
      println("last modified: " + app1ChangeSet01ExpansionDeltaFile.lastModified())
      writeToFile(app1ChangeSet01ExpansionDeltaFile, "use someDb")

      appWatcher.stopWatching
      while(appWatcher.isActive)

      there was one(application).update(any[Application])
    }

  }

}
