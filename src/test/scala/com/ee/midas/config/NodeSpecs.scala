package com.ee.midas.config

import java.net.{ServerSocket, Socket, InetAddress}
import org.specs2.mock.Mockito
import com.ee.midas.transform.Transformer
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit._

/**
 * IMPORTANT NOTE:
 *
 * Specs are written in JUnit style because
 * 1) Conventional style causes the test cases to run multiple times, as described in the link below.
 * https://groups.google.com/forum/#!topic/play-framework/4Fz5TsOKPio
 * 2) This causes numerous problems while testing systems with multi-threaded environment.
 *
 * Also, @RunWith uses MockitoJUnitRunner because the conventional JUnitRunner provided by Specs2
 * is not compatible with JUnit style test cases written here.
 */
@RunWith(classOf[MockitoJUnitRunner])
class NodeSpecs extends JUnitMustMatchers with Mockito {

    val name = "test-node"
    val ipAddress = InetAddress.getByName("127.0.0.3")
    val changeSet = ChangeSet()
    val host = "localhost"

    @Test
    def nodeStartInactive() {
      //Given
      val name = "test-node"
      val ipAddress = InetAddress.getByName("127.0.0.3")
      val changeSet = ChangeSet()

      //When
      val node = new Node(name, ipAddress, changeSet)

      //Then
      node.isActive mustEqual false
    }

     /*"be identified by it's IP" in {
       //Given (2 nodes with same IP, but different name and changeSet)
     }*/

    @Test
    def nodeBecomesActiveWhenADuplexPipeIsStarted() {
      //given
      val node = new Node(name, ipAddress, changeSet)
      val clientSocket = new Socket(host, ServerSetup.appServerPort)
      val mongoSocket = new Socket(host, ServerSetup.mongoServerPort)

      val mockTransformer = mock[Transformer]

      //when
      val duplexPipe = node.startDuplexPipe(clientSocket, mongoSocket, mockTransformer)
//      while(!duplexPipe.isActive)
      Thread.sleep(2000)

      //then
      node.isActive must beTrue
    }

    /*def waitForDuplexPipeToStop(pipe: DuplexPipe) = {
      while(pipe.isActive) {
        Thread.sleep(1000)
      }
    }

    def waitForDuplexPipeToStart(pipe: DuplexPipe) = {
      while(!pipe.isActive) {
        Thread.sleep(1000)
      }
    }*/

    @Test
    def nodeCleanDeadPipes() {
      //given
      val node = new Node(name, ipAddress, changeSet)

      val clientSocket = new Socket(host, ServerSetup.appServerPort)
      val mongoSocket = new Socket(host, ServerSetup.mongoServerPort)
      val mockTransformer = mock[Transformer]

      //and given
      val duplexPipe = node.startDuplexPipe(clientSocket, mongoSocket, mockTransformer)
      Thread.sleep(1000)
      duplexPipe.forceStop
      while(duplexPipe.isActive)

      //when
      node.clean

      //then
      node.isActive must beFalse
    }

}

object NodeSpecs {
  @BeforeClass
  def setup() {
    ServerSetup.setUpSockets()
  }

  @AfterClass
  def cleanup() {
    ServerSetup.shutdownSockets()
  }
}

object ServerSetup {
  var appServerPort = 27020
  var mongoServerPort = 27017
  var appServer: ServerSocket = null
  var mongoServer: ServerSocket = null

  def setUpSockets() {
    appServer = new ServerSocket(0)
    appServerPort = appServer.getLocalPort
    mongoServer = new ServerSocket(0)
    mongoServerPort = mongoServer.getLocalPort
    new Thread(new Runnable {
      def run() = {
        while(!appServer.isClosed) {
          appServer.accept()
        }
      }
    }).start()
    new Thread(new Runnable {
      def run() = {
        while(!mongoServer.isClosed) {
          mongoServer.accept()
        }
      }
    }).start()
  }

  def shutdownSockets() {
    appServer.close()
    mongoServer.close()
  }
}
