package com.ee.midas.config

import java.net.InetAddress
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{Specification}
import com.ee.midas.transform.TransformType
import java.io.{PrintWriter, File}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class ConfigurationSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val deltasDir = new File("/" + System.getProperty("user.dir"))
    val midasConfigFile = new File(deltasDir.getPath + "midas.config")
    midasConfigFile.createNewFile()
    midasConfigFile.deleteOnExit()
    val appName = "testApp"
    val nonExistentAppName = "nonExistentApp"

    val midasConfigText =
      s"""
      |apps {
      |  $appName
      |  $nonExistentAppName
      |}
     """.stripMargin
    write(midasConfigText, midasConfigFile)

    val ip1 = "127.0.0.1"
    val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)

    val ip2 = "127.0.0.2"
    val (node2Name, node2Ip, changeSet2) = ("node2", InetAddress.getByName(ip2), 2)
    val appConfigDir = new File(deltasDir + File.separator + appName)
    appConfigDir.mkdirs()
    appConfigDir.deleteOnExit()

    val appConfigFile = new File(appConfigDir.getPath + File.separator + s"$appName.midas")
    appConfigFile.deleteOnExit()
    val appConfigText =
      s"""
      |$appName {
      |  mode = expansion
      |  $node1Name {
      |    ip = $ip1
      |    changeSet = $changeSet1
      |  }
      |  $node2Name {
      |    ip = $ip2
      |    changeSet = $changeSet2
      |  }
      |}
     """.stripMargin

    write(appConfigText, appConfigFile)


    private def write(text: String, toFile: File) = {
      val writer = new PrintWriter(toFile, "utf-8")
      writer.write(text)
      writer.flush()
      writer.close()
    }

    val node1 = Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val node2 = Node(node2Name, node2Ip, ChangeSet(changeSet2))
    val nodes = List(node1, node2)
    val application = Application(appConfigDir.toURI.toURL, appName, TransformType.EXPANSION, nodes)
    val configuration = Configuration(deltasDir.toURI.toURL, List(appName, nonExistentAppName))
  }

  "Configuration" should {

    "Affirm presence of the Application with given IP" in new Setup {
      //When-Then
      configuration.hasApplication(node1Ip) mustEqual true
    }

    "Deny presence of the Application with given IP" in new Setup {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When-Then
      configuration.hasApplication(ip) mustEqual false
    }

    "Get Application by IP" in new Setup {
      //When-Then
      configuration.getApplication(node1Ip) mustEqual Some(application)
    }

    "Give no result when Application with that IP is not present" in new Setup {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When
      val app = configuration.getApplication(ip)

      //Then
      app mustEqual None
    }

    "Update application" in new Setup {
      //Given
      configuration.getApplication(node2Ip) mustEqual Some(application)

      //When
      val newIP = InetAddress.getByName("192.2.1.27")
      val newChangeSet = ChangeSet(3)
      val node2WithNewIPAndChangeSet = Node(node2Name, newIP, newChangeSet)

      val newNodes = node1 :: node2WithNewIPAndChangeSet :: Nil
      val applicationWithNewIP = Application(appConfigDir.toURI.toURL, appName, TransformType.EXPANSION, newNodes)
      configuration.update(applicationWithNewIP)

      //Then
      configuration.getApplication(newIP) mustEqual Some(applicationWithNewIP)
    }

    "Adds a non-existing application when updated" in new Setup {
      //Given
      val ip = "127.0.0.3"
      val nodeIp = InetAddress.getByName(ip)
      val node = Node("newAppNode1", nodeIp, ChangeSet(changeSet1))
      val newAppName = "newApp"
      val newAppConfigDir = new File(deltasDir + File.separator + newAppName)

      newAppConfigDir.mkdirs()
      newAppConfigDir.deleteOnExit()
      val newApplication = Application(newAppConfigDir.toURI.toURL, newAppName, TransformType.EXPANSION, List(node))
      configuration.getApplication(nodeIp) mustEqual None

      //When
      configuration.update(newApplication)

      //Then
      configuration.getApplication(nodeIp) mustEqual Some(newApplication)
    }

  }
}
