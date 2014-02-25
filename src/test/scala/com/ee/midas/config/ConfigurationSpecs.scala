package com.ee.midas.config

import java.net.{Socket, InetAddress}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{Specification}
import com.ee.midas.transform.TransformType
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, PrintWriter, File}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

//todo: revisit
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

    val node1 = new Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val node2 = new Node(node2Name, node2Ip, ChangeSet(changeSet2))
    val nodes = Set(node1, node2)
    val application = new Application(appConfigDir.toURI.toURL, appName, TransformType.EXPANSION, nodes)
    val configuration = Configuration(deltasDir.toURI.toURL, List(appName, nonExistentAppName))
  }

  isolated
  "Configuration" should {

    //todo: revisit
    "Manage Applications" in {
      "By allowing a Application to be retrieved by IP" in new Setup {
        //When-Then
        configuration.getApplication(node1Ip) == Some(application)
      }

      "By giving no result when Application with that IP is not present" in new Setup {
        //Given
        val ip = InetAddress.getByName("127.0.0.9")

        //When
        val app = configuration.getApplication(ip)

        //Then
        app mustEqual None
      }

      "By Adding a non-existing application when updated" in new Setup {
        //Given
        val ip = "127.0.0.3"
        val nodeIp = InetAddress.getByName(ip)
        val node = new Node("newAppNode1", nodeIp, ChangeSet(changeSet1))
        val newAppName = "newApp"
        val newAppConfigDir = new File(deltasDir + File.separator + newAppName)

        newAppConfigDir.mkdirs()
        newAppConfigDir.deleteOnExit()
        val newApplication = new Application(newAppConfigDir.toURI.toURL, newAppName, TransformType.EXPANSION, Set(node))
        configuration.getApplication(nodeIp) mustEqual None

        //When
        configuration.update(newApplication)

        //Then
        configuration.getApplication(nodeIp) == Some(newApplication)
      }

      "By Updating an existing application" in new Setup {
        //Given
        configuration.getApplication(node2Ip) == Some(application)

        //When
        val newIP = InetAddress.getByName("192.2.1.27")
        val newChangeSet = ChangeSet(3)
        val node2WithNewIPAndChangeSet = new Node(node2Name, newIP, newChangeSet)

        val newNodes = Set(node1, node2WithNewIPAndChangeSet)
        val applicationWithNewIP = new Application(appConfigDir.toURI.toURL, appName, TransformType.EXPANSION, newNodes)
        configuration.update(applicationWithNewIP)

        //Then
        configuration.getApplication(newIP) == Some(applicationWithNewIP)
      }

    }

    "Give all applications" in new Setup {
      //When-Then
      configuration.applications == List(application)
    }

    "update itself from new configuration" in new Setup {
      //Given
      val ip = "127.0.0.3"
      val nodeIp = InetAddress.getByName(ip)
      val node = new Node("newAppNode1", nodeIp, ChangeSet(changeSet1))
      val newAppName = "App1"
      val newAppConfigDir = new File(deltasDir + File.separator + newAppName)

      newAppConfigDir.mkdirs()
      newAppConfigDir.deleteOnExit()
      val newApplication = new Application(newAppConfigDir.toURI.toURL, newAppName, TransformType.EXPANSION, Set(node))
      val newConfiguration = Configuration(deltasDir.toURI.toURL, List(newAppName))

      //When
      configuration.update(newConfiguration)

      //Then
      configuration.applications == List(newApplication)
    }

    "accept new authorized connection" in new Setup {
      //Given
      val appSocket = mock[Socket]
      val mongoSocket = mock[Socket]
      appSocket.getInetAddress returns InetAddress.getByName("127.0.0.1")
      val data: Array[Byte] = Array(0.toByte)
      mongoSocket.getInputStream returns new ByteArrayInputStream(data)
      mongoSocket.getOutputStream returns new ByteArrayOutputStream
      appSocket.getInputStream returns new ByteArrayInputStream(data)
      appSocket.getOutputStream returns new ByteArrayOutputStream

      //When
      configuration.processNewConnection(appSocket, mongoSocket)

      //Then
      there was no(appSocket).close()
    }

    "reject unauthorized connection" in new Setup {
      //Given
      val appSocket = mock[Socket]
      val mongoSocket = mock[Socket]
      appSocket.getInetAddress returns InetAddress.getByName("127.0.0.9")

      //When
      configuration.processNewConnection(appSocket, mongoSocket)

      //Then
      there was one(appSocket).close
    }
  }
}
