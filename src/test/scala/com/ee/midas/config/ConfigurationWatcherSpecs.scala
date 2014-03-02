package com.ee.midas.config

import org.specs2.mutable.{After, BeforeAfter, Specification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import java.net.URL
import scala.util.Try
import java.io.File
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ConfigurationWatcherSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val configFile = new File("ConfigurationWatcherSpecs" + File.separator + Configuration.filename)
    val deltasDirURI = new File("ConfigurationWatcherSpecs")
  }

  "Configuration watcher" should {

    "start watching a config file for changes" in new Setup {

      //given
      val configuration = mock[Configuration]
      val newConfiguration = mock[Configuration]
      val configWatcher = new ConfigurationWatcher(configuration, deltasDirURI.toURI) {
        override def parse(url: URL, fileName: String) = Try {
          newConfiguration
        }
      }

      //when
      configWatcher.startWatching

      //then
      configWatcher.isActive must beTrue
    }

    /*"trigger an update of configuration when the config file is modified" in new Setup with After {
      //given
      deltasDirURI.mkdirs()
      configFile.createNewFile()
      def after(): Unit = {
        configFile.delete()
        deltasDirURI.delete()
      }
      val configuration = mock[Configuration]
      val newConfiguration = mock[Configuration]
      val configWatcher = new ConfigurationWatcher(configuration, deltasDirURI.toURI) {
        override def parse(url: URL, fileName: String) = Try {
          newConfiguration
        }
      }
      configWatcher.startWatching

      //when
      write("{ app1 }", configFile)
      Thread.sleep(4000)

      //then
      configWatcher.isActive must beTrue
      there was one(configuration).update(newConfiguration)
    }*/

    "stop watching the config file for changes" in new Setup {
      //given
      val configuration = mock[Configuration]
      val newConfiguration = mock[Configuration]

      val configWatcher = new ConfigurationWatcher(configuration, deltasDirURI.toURI) {
        override def parse(url: URL, fileName: String) = Try {
          newConfiguration
        }
      }
      configWatcher.startWatching
      assert(configWatcher.isActive)

      //when
      configWatcher.stopWatching

      //then
      configWatcher.isActive must beFalse
    }
  }

}
