package com.ee.midas.interceptor

import java.io.{OutputStream, InputStream}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.ee.midas.config.{ChangeSet, Node, Application}
import com.ee.midas.transform.TransformType
import org.specs2.specification.Scope
import java.net.{URL, InetAddress}

@RunWith(classOf[JUnitRunner])
class MidasInterceptableSpecs extends Specification with Mockito {

  trait Setup extends Scope {
    val appName = "testApp"
    val ip1 = "127.0.0.1"
    val (node1Name, node1Ip, changeSet1) = ("node1", InetAddress.getByName(ip1), 1)
    val node1 = Node(node1Name, node1Ip, ChangeSet(changeSet1))
    val nodes = List(node1)
    val ignoreConfigDir: URL = null
    val application = Application(ignoreConfigDir, appName, TransformType.EXPANSION, nodes)

    val midasInterceptable = new MidasInterceptable {
      def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
        sourceReadWasInvoked = true
        Array[Byte]()
      }

      def readHeader(src: InputStream): BaseMongoHeader = {
        readHeaderWasInvoked = true
        mock[BaseMongoHeader]
      }
    }

    var readHeaderWasInvoked = false
    var sourceReadWasInvoked = false
  }


  "MidasInterceptable" should {
    "Read header" in new Setup {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      midasInterceptable.intercept(src, tgt)

      //then
      readHeaderWasInvoked
    }

    "Read from inputstream" in new Setup {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      midasInterceptable.intercept(src, tgt)

      //then
      sourceReadWasInvoked
    }

    "Write to outputStream" in new Setup {
      //given
      val src = mock[InputStream]
      val tgt = mock[OutputStream]

      //when
      midasInterceptable.intercept(src, tgt)

      //then
      there was one(tgt).write(any[Array[Byte]], anyInt, anyInt)
    }
  }

}
