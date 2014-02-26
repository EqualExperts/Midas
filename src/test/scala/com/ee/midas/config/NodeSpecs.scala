package com.ee.midas.config

import java.net.{Socket, InetAddress}
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
    val changeSet = ChangeSet()
    val host = "localhost"

    @Test
    def nodeIsInactiveWhenCreated() {
      //Given
      val name = "test-node"
      val ipAddress = InetAddress.getByName("127.0.0.3")
      val changeSet = ChangeSet()

      //When
      val node = new Node(name, ipAddress, changeSet)

      //Then
      node.isActive mustEqual false
    }

    @Test
    def nodeMustBeIdentifiedByIP() {
      //Given (2 nodes with same IP, but different name and changeSet)
      val ipAddress = InetAddress.getByName("127.0.0.4")
      val node1 = new Node("node1", ipAddress, ChangeSet(1))
      val node2 = new Node("node2", ipAddress, ChangeSet(2))

      //Then: both nodes are same
      node1 mustEqual node2
    }

    @Test
    def nodeBecomesActiveWhenADuplexPipeIsStarted() {
      //given
      val ipAddress = InetAddress.getByName("127.0.0.5")
      val node = new Node(name, ipAddress, changeSet)
      val clientSocket = new Socket(host, NodeSpecs.servers.midasServerPort)
      val mongoSocket = new Socket(host, NodeSpecs.servers.mongoServerPort)
      val mockTransformer = mock[Transformer]

      //when
      node.startDuplexPipe(clientSocket, mongoSocket, mockTransformer)

      //then
      node.isActive must beTrue
    }

    @Test
    def nodeCleansDeadPipes() {
      //given
      val ipAddress = InetAddress.getByName("127.0.0.6")
      val node = new Node(name, ipAddress, changeSet)
      val clientSocket = new Socket(host, NodeSpecs.servers.midasServerPort)
      val mongoSocket = new Socket(host, NodeSpecs.servers.mongoServerPort)
      val mockTransformer = mock[Transformer]

      //and given
      val duplexPipe = node.startDuplexPipe(clientSocket, mongoSocket, mockTransformer)
      duplexPipe.forceStop

      //when
      node.clean

      //then
      node.isActive must beFalse
    }

  @Test
  def nodeStartsMultiplePipes() {
    //given: Multiple client and mongo sockets
    val ipAddress = InetAddress.getByName("127.0.0.7")
    val node = new Node(name, ipAddress, changeSet)

    val clientSocket1 = new Socket(host, NodeSpecs.servers.midasServerPort)
    val mongoSocket1 = new Socket(host, NodeSpecs.servers.mongoServerPort)

    val clientSocket2 = new Socket(host, NodeSpecs.servers.midasServerPort)
    val mongoSocket2 = new Socket(host, NodeSpecs.servers.mongoServerPort)
    val mockTransformer = mock[Transformer]

    //when: A node starts a duplex pipe for each pair of sockets
    val duplexPipe1 = node.startDuplexPipe(clientSocket1, mongoSocket1, mockTransformer)
    val duplexPipe2 = node.startDuplexPipe(clientSocket2, mongoSocket2, mockTransformer)

    //then: The duplex pipes were started successfully
    duplexPipe1.isActive must beTrue
    duplexPipe2.isActive must beTrue
  }

  @Test
  def nodeStopsActivePipes() {
    //given: A node with few active pipes
    val ipAddress = InetAddress.getByName("127.0.0.8")
    val node = new Node("stop-node", ipAddress, changeSet)
    val mockTransformer = mock[Transformer]

    val clientSocket1 = new Socket(host, NodeSpecs.servers.midasServerPort)
    val mongoSocket1 = new Socket(host, NodeSpecs.servers.mongoServerPort)
    val duplexPipe1 = node.startDuplexPipe(clientSocket1, mongoSocket1, mockTransformer)

    val clientSocket2 = new Socket(host, NodeSpecs.servers.midasServerPort)
    val mongoSocket2 = new Socket(host, NodeSpecs.servers.mongoServerPort)
    val duplexPipe2 = node.startDuplexPipe(clientSocket2, mongoSocket2, mockTransformer)

    //when: The node is stopped
    node.stop

    //then: All the active pipes must be stopped as well
    duplexPipe1.isActive must beFalse
    duplexPipe2.isActive must beFalse
  }

}

object NodeSpecs {

  val servers = new ServerSetup()
  @BeforeClass
  def setup() {
    servers.setUpSockets()
  }

  @AfterClass
  def cleanup() {
    servers.shutdownSockets()
  }
}
