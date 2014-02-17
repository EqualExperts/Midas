package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{BeforeAfter, Specification}
import java.net.{URI, URL, Inet4Address, InetAddress}
import com.ee.midas.transform.TransformType
import java.io.{PrintWriter, File}

@RunWith(classOf[JUnitRunner])
class ModelSpecs extends Specification {

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

  val ip2 = "127.0.0.0"
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

  "Application" should {

    "Get Node by IP" in {
       //When-Then
       application.getNode(node2Ip) mustEqual Some(node2)
    }

    "Give no result if Node with that IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When-Then
      application.getNode(ip) mustEqual None
    }

    "Get Change Set by IP" in {
      //When
      val actualChangeSet = application.changeSet(node1Ip)

      //Then
      actualChangeSet mustEqual Some(ChangeSet(changeSet1))
    }

    "Give no result if Change Set for the IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.10")

      //When-Then
      application.changeSet(ip) mustEqual None
    }

    "Affirm Node's presence in the application by IP" in {
      //When-Then
      application.hasNode(node2Ip) mustEqual true
    }

    "Deny Node's presence in the application by IP" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.6")

      //When-Then
      application.hasNode(ip) mustEqual false
    }
  }

  "Configuration" should {

     "Affirm presence of the Application with given IP" in {
       //When-Then
       configuration.hasApplication(node1Ip) mustEqual true
     }

    "Deny presence of the Application with given IP" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When-Then
      configuration.hasApplication(ip) mustEqual false
    }

    "Affirm presence of the Application with given name" in {
      //When-Then
      configuration.hasApplication(appName) mustEqual true
    }

    "Deny presence of the Application with given name" in {
      //When-Then
      configuration.hasApplication(nonExistentAppName) mustEqual false
    }

    "Get Application by IP" in {
      //When-Then
      configuration.getApplication(node2Ip) mustEqual Some(application)
    }

    "Give no result when Application with that IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When
      val app = configuration.getApplication(ip)

      //Then
      app mustEqual None
    }
  }
}
