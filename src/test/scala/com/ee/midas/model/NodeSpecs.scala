/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.model

import java.net.{Socket, InetAddress}
import org.specs2.mock.Mockito
import com.ee.midas.transform.Transformer
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit._
import com.ee.midas.utils.SynchronizedHolder
import java.io.StringReader

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
      node.startDuplexPipe(clientSocket, mongoSocket, SynchronizedHolder(mockTransformer))

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
      val duplexPipe = node.startDuplexPipe(clientSocket, mongoSocket, SynchronizedHolder(mockTransformer))
      duplexPipe.forceStop

      //when
      node.cleanDeadPipes

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
    val duplexPipe1 = node.startDuplexPipe(clientSocket1, mongoSocket1, SynchronizedHolder(mockTransformer))
    val duplexPipe2 = node.startDuplexPipe(clientSocket2, mongoSocket2, SynchronizedHolder(mockTransformer))

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
    val duplexPipe1 = node.startDuplexPipe(clientSocket1, mongoSocket1, SynchronizedHolder(mockTransformer))

    val clientSocket2 = new Socket(host, NodeSpecs.servers.midasServerPort)
    val mongoSocket2 = new Socket(host, NodeSpecs.servers.mongoServerPort)
    val duplexPipe2 = node.startDuplexPipe(clientSocket2, mongoSocket2, SynchronizedHolder(mockTransformer))

    //when: The node is stopped
    node.stop

    //then: All the active pipes must be stopped as well
    duplexPipe1.isActive must beFalse
    duplexPipe2.isActive must beFalse
  }

  @Test
  def toStringReturnsParseableNode: Unit = {
    //Given
    val name = "testNode"
    val ip = InetAddress.getByName("127.0.0.3")
    val changeSet = ChangeSet()
    val aNode = new Node(name, ip, changeSet)

    //When
    val input = new StringReader(aNode.toString)

    //Then
    new ApplicationParsers {
      parseAll(node, input) match {
        case NoSuccess(msg, _) => throw new AssertionError(msg)
        case Success(parsedNode, _) => parsedNode mustEqual aNode
      }
    }
  }
}

object NodeSpecs {

  val servers = new ServerSetup

  @BeforeClass
  def setup() {
    servers.start
  }

  @AfterClass
  def cleanup() {
    servers.stop
  }
}
