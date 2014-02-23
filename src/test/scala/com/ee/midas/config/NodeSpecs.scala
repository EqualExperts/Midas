package com.ee.midas.config

import org.specs2.mutable.Specification
import java.net.{Socket, InetAddress}
import com.ee.midas.utils.Accumulator
import org.specs2.mock.Mockito
import com.ee.midas.pipes.{PipesMonitorComponent, DuplexPipe}
import com.ee.midas.transform.Transformer
import java.io.{OutputStream, InputStream, ByteArrayInputStream}
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.Test

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
class DuplexPipeSpecs extends JUnitMustMatchers with Mockito {

    val name = "test-node"
    val ipAddress = InetAddress.getByName("127.0.0.3")
    val changeSet = ChangeSet()

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

    @org.junit.Ignore
    @Test
    def nodeBecomesActiveWhenADuplexPipeIsStarted() {
      //given
      val node = new Node(name, ipAddress, changeSet)
      val clientSocket = mock[Socket]
      val mongoSocket = mock[Socket]
      val mockInputStream = mock[InputStream]
      mockInputStream.read(Array()) returns 20
      clientSocket.getInputStream returns mockInputStream
      clientSocket.getOutputStream returns mock[OutputStream]
      mongoSocket.getInputStream returns mockInputStream
      mongoSocket.getOutputStream returns mock[OutputStream]

      val mockTransformer = mock[Transformer]

      //when
      val duplexPipe = node.startDuplexPipe(clientSocket, mongoSocket, mockTransformer)
      waitForDuplexPipeToStart(duplexPipe)

       //then
      node.isActive must beTrue
    }

    def waitForDuplexPipeToStop(pipe: DuplexPipe) = {
      while(pipe.isActive) {
        Thread.sleep(1000)
      }
    }

    def waitForDuplexPipeToStart(pipe: DuplexPipe) = {
      while(!pipe.isActive) {
        Thread.sleep(1000)
      }
    }

    @Test
    def nodeCleanDeadPipes() {
      //given
      val node = new Node(name, ipAddress, changeSet)
      val clientSocket = mock[Socket]
      val mongoSocket = mock[Socket]
      clientSocket.getInputStream returns mock[InputStream]
      clientSocket.getOutputStream returns mock[OutputStream]
      mongoSocket.getInputStream returns mock[InputStream]
      mongoSocket.getOutputStream returns mock[OutputStream]
      val mockTransformer = mock[Transformer]

      //and given
      val duplexPipe = node.startDuplexPipe(clientSocket, mongoSocket, mockTransformer)
      Thread.sleep(1000)
      duplexPipe.forceStop
      waitForDuplexPipeToStop(duplexPipe)

      //when
      node.clean

      //then
      node.isActive must beFalse
    }
}
