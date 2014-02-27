package com.ee.midas.config

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.transform.TransformType
import java.net.InetAddress
import java.io.File

@RunWith(classOf[JUnitRunner])
class ApplicationWatcherSpecs extends Specification with Mockito {

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
      writeToFile(app1Config, appConfigText)
      val appWatcher = new ApplicationWatcher(application)

      //When
      appWatcher.startWatching
      Thread.sleep(500)
      writeToFile(app1ChangeSet01ExpansionDeltaFile, "use someDb")
      Thread.sleep(500)

      //Then
      appWatcher.stopWatching
      application.hasNode(InetAddress.getByName("127.0.0.1"))
    }

    "stop watching the deltas of given application" in new MidasConfigurationSetup {

      //Given
      val application = new Application(app1DirURL, "app1", TransformType.EXPANSION, Set[Node]())
      val appWatcher = new ApplicationWatcher(application)

      appWatcher.startWatching
      Thread.sleep(500)

      //When
      appWatcher.stopWatching
      new File(s"${app1.getAbsolutePath}/dummyFile.txt").createNewFile()

      //Then
      Thread.sleep(500)
      new File(s"${app1.getAbsolutePath}/dummyFile.txt").delete()
      appWatcher.isActive must beFalse
    }

  }

}
