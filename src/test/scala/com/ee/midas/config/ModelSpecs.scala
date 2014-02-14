package com.ee.midas.config

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import java.net.{Inet4Address, InetAddress}
import com.ee.midas.transform.TransformType

@RunWith(classOf[JUnitRunner])
class ModelSpecs extends Specification {
  //Given
  val node1 = Node("node1", InetAddress.getByName("127.0.0.1"), ChangeSet(1))
  val node2 = Node("node2", InetAddress.getByName("127.0.0.0"), ChangeSet(2))
  val nodes = List(node1, node2)
  val application = Application("App1", TransformType.EXPANSION, nodes)
  val configuration = Configuration(List(application))

  "Application" should {

    "Get Node by IP" in {
       //Given
       val ip = InetAddress.getByName("127.0.0.0")

       //When
       val node = application.getNode(ip)

       //Then
       node mustEqual Some(node2)
    }

    "Give None if Node with that IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When
      val node = application.getNode(ip)

      //Then
      node mustEqual None
    }

    "Get Change Set by IP" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.0")

      //When
      val actualChangeSet = application.changeSet(ip)

      //Then
      actualChangeSet mustEqual Some(ChangeSet(2))
    }

    "Give None if Change Set for the IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.10")

      //When
      val actualChangeSet = application.changeSet(ip)

      //Then
      actualChangeSet mustEqual None
    }

    "Returns true if IP is present in application" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.0")

      //When
      val nodePresent = application.hasNode(ip)

      //Then
      nodePresent mustEqual true
    }

    "Returns false if IP is not present in application" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.6")

      //When
      val nodePresent = application.hasNode(ip)

      //Then
      nodePresent mustEqual false
    }

  }

  "Configuration" should {

     "Return True if any of the Application has given IP" in {
        //Given
        val ip = InetAddress.getByName("127.0.0.0")

       //When
       val appPresent = configuration.hasApplication(ip)

       //Then
       appPresent mustEqual true
     }

    "Return False if any of the Application has given IP" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When
      val appPresent = configuration.hasApplication(ip)

      //Then
      appPresent mustEqual false
    }

    "Get Application by IP" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.0")

      //When
      val app = configuration.getApplication(ip)

      //Then
      app mustEqual Some(application)
    }

    "Give None if Application with that IP is not present" in {
      //Given
      val ip = InetAddress.getByName("127.0.0.9")

      //When
      val app = configuration.getApplication(ip)

      //Then
      app mustEqual None
    }
  }
}
