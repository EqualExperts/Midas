package com.ee.midas.config

import java.net.{URL, Socket, InetAddress}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.{After, Specification}
import com.ee.midas.transform.TransformType
import java.io._
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import scala.Some

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

    val node1 = new Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val node2 = new Node(node2Name, node2Ip, ChangeSet(changeSet2))
    val nodes = Set(node1, node2)
    val application = new Application(appConfigDir.toURI.toURL, appName, TransformType.EXPANSION, nodes)
    val configuration = new Configuration(deltasDir.toURI.toURL, List(appName, nonExistentAppName))
  }

  "Configuration" should {

    //todo: revisit
    "Manage Applications" in {
      "By allowing a Application to be retrieved by IP" in new Setup {
        //When-Then
        configuration.getApplication(node1Ip) mustEqual Some(application)
      }

      "By giving no result when Application with that IP is not present" in new Setup {
        //Given
        val ip = InetAddress.getByName("127.0.0.9")

        //When
        val app = configuration.getApplication(ip)

        //Then
        app mustEqual None
      }
    }

    "Give all applications" in new Setup {
      //When-Then
      configuration.applications mustEqual List(application)
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

    "update itself from a new configuration" in new Setup {
      //Given: A configuration from setup
      val updatedMidasConfigText =  s"""
                                      |apps {
                                      |  App1
                                      |}
                                     """.stripMargin
      write(updatedMidasConfigText, midasConfigFile)
      val newAppName = "App1"
      val newAppNode = "newAppNode1"
      val newAppConfigDir = new File(deltasDir + File.separator + newAppName)
      newAppConfigDir.mkdirs()
      newAppConfigDir.deleteOnExit()
      val newAppConfigFile = new File(deltasDir + File.separator + newAppName + File.separator + s"$newAppName.midas")
      newAppConfigFile.createNewFile()
      newAppConfigFile.deleteOnExit()
      val newNodeIp = "127.0.0.3"
      val newNodeInetAddress = InetAddress.getByName(newNodeIp)
      val newChangeSetNo = 2
      val newChangeSet = ChangeSet(2)
      val newAppConfigText = s"""
                              |$newAppName {
                              |  mode = expansion
                              |  $newAppNode {
                              |    ip = $newNodeIp
                              |    changeSet = $newChangeSetNo
                              |  }
                              |}
                             """.stripMargin
      write(newAppConfigText, newAppConfigFile)
      println(s"${newAppConfigDir.getAbsolutePath} exists: " + newAppConfigDir.exists())
      val newConfiguration = new Configuration(deltasDir.toURI.toURL, List("App1"))
      println("new config is: " + newConfiguration)

      //When
      configuration.update(newConfiguration)

      //Then
      val node = new Node(newAppName, newNodeInetAddress, newChangeSet)
      val expectedApplication = new Application(newAppConfigDir.toURI.toURL, newAppName, TransformType.EXPANSION, Set(node))
      configuration.applications mustEqual List(expectedApplication)
    }

    "Update" in {

      "By adding a new application from new configuration" in new UpdateSetup {
        //given
        override val deltasDir = new File("/" + System.getProperty("user.dir") + "/" + "addsNewAppSpec")
        val (application1, application2, _) = createApplications
        val oldConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1))
        val newConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1, appName2))

        //when
        oldConfiguration.update(newConfiguration)

        //then
        oldConfiguration.applications must contain(application1)
        oldConfiguration.applications must contain(application2)
      }

      "By adding two or more new applications and keeping the common ones from new configuration" in new UpdateSetup {
        //given
        override val deltasDir = new File("/" + System.getProperty("user.dir") + "/" + "addsTwoNewAppSpec")
        val (application1, application2, application3) = createApplications
        val oldConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1))
        val newConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1, appName2, appName3))

        //when
        oldConfiguration.update(newConfiguration)

        //then
        oldConfiguration.applications must contain(application1)
        oldConfiguration.applications must contain(application2)
        oldConfiguration.applications must contain(application3)
      }

      "By removing an application that is not present in new configuration" in new UpdateSetup {
        //given
        val deltasDir = new File("/" + System.getProperty("user.dir") + "/" + "removesAppSpec")
        val (application1, application2, _) = createApplications
        val oldConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1, appName2))
        val newConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1))

        //when
        oldConfiguration.update(newConfiguration)

        //then
        oldConfiguration.applications must contain(application1)
        oldConfiguration.applications contains application2 must beFalse
      }

      "By removing an application and stopping it, if not present in new configuration" in new UpdateSetup {
        //given: a configuration with an active application
        val deltasDir = new File("/" + System.getProperty("user.dir") + "/" + "removesAndStopsAppSpec")
        val (clientSocket, mongoSocket) = createServerSockets

        override def after = {
          stopServerSockets
          delete(deltasDir)
        }

        val (application1, application2, _) = createApplications
        val oldConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName1, appName2))
        val newConfiguration = createNewConfiguration(deltasDir.toURI.toURL, List(appName2))

        val app1FromConfig = oldConfiguration.getApplication(ipAddress1).get

        //when
        oldConfiguration.processNewConnection(clientSocket, mongoSocket)

        //then
        app1FromConfig.isActive must beTrue

        //when
        oldConfiguration.update(newConfiguration)

        //then
        oldConfiguration.applications must contain(application2)
        oldConfiguration.applications contains application1 must beFalse
        app1FromConfig.isActive must beFalse
      }

    }
  }

  trait UpdateSetup extends After {

      val deltasDir: File

      val appName1 = "app1"
      val ipAddress1 = InetAddress.getByName("127.0.0.1")
      val nodeApp1 = new Node("node1", ipAddress1, ChangeSet(1))

      val appName2 = "app2"
      val ipAddress2 = InetAddress.getByName("127.0.0.2")
      val nodeApp2 = new Node("node2", ipAddress2, ChangeSet(1))

      val appName3 = "app3"
      val ipAddress3 = InetAddress.getByName("127.0.0.3")
      val nodeApp3 = new Node("node3", ipAddress3, ChangeSet(2))
      val servers = new ServerSetup()

      def createApplications = {
        val application1 = createNewApplication(deltasDir.toURI.toURL, appName1, TransformType.EXPANSION, Set[Node](nodeApp1))
        val application2 = createNewApplication(deltasDir.toURI.toURL, appName2, TransformType.EXPANSION, Set[Node](nodeApp2))
        val application3 = createNewApplication(deltasDir.toURI.toURL, appName3, TransformType.EXPANSION, Set[Node](nodeApp3))
        (application1, application2, application3)
      }

      def createServerSockets = {
        servers.start
        val midas = new Socket("localhost", servers.midasServerPort)
        val mongo = new Socket("localhost", servers.mongoServerPort)
        (midas, mongo)
      }

      def stopServerSockets = {
        servers.stop
      }

      val oldConfiguration: Configuration
      val newConfiguration: Configuration

      def after = {
        delete(deltasDir)
      }

      def delete(directory: File): Unit = {
        directory.listFiles() foreach { file =>
          if(file.isFile) {
            println(s"deleting file: ${file.getAbsolutePath}")
            file.delete()
          }
          else if(file.isDirectory){
            delete(file)
          }
        }
        println(s"deleting directory: ${directory.getAbsolutePath}")
        directory.delete()
      }

      def createNewApplication(deltasDirURL: URL, appName: String, transformType: TransformType, nodes: Set[Node]) = {
        val appConfigDir = new File(s"${deltasDirURL.toURI.getPath}/$appName")
        appConfigDir.mkdirs()
        val appConfigFile = new File(s"${appConfigDir.getAbsolutePath}/$appName.midas")
        appConfigFile.createNewFile()
        val nodeText = nodes map { node =>
                            s"""${node.name} {
                             | ip = ${node.ip.toString.substring(1)}
                             | changeSet = ${node.changeSet.number}
                             |}
                            """.stripMargin
                       } mkString (NEW_LINE).trim
        val appConfigText = s"""
                            |$appName {
                            |  mode = ${transformType.name().toLowerCase}
                            |  ${nodeText}
                            |}
                           """.stripMargin
        println(s"writing to file: $appConfigText")
        write(appConfigText, appConfigFile)
        new Application(appConfigDir.toURI.toURL, appName, transformType, nodes)
      }


      val NEW_LINE = System.getProperty("line.separator")


      def createNewConfiguration(deltasDirURL: URL, appNames: List[String]) = {
        val configDir = new File(s"${deltasDirURL.toURI.getPath}")
        configDir.mkdirs()
        val configFile = new File(s"${configDir.getAbsolutePath}/midas.config")
        configFile.createNewFile()

        val configFileText = s"""
                              |apps {
                              |  ${appNames.mkString(NEW_LINE)}
                              |}
                             """.stripMargin
        write(configFileText, configFile)
        new Configuration(deltasDirURL, appNames)
      }

  }

  def write(text: String, toFile: File) = {
    val writer = new PrintWriter(toFile, "utf-8")
    writer.write(text)
    writer.flush()
    writer.close()
  }

}
