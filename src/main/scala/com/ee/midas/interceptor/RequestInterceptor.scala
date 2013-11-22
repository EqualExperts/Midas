package com.ee.midas.interceptor

import java.io.InputStream
import com.ee.midas.utils.Loggable

class RequestInterceptor (tracker: MessageTracker) extends MidasInterceptable with Loggable {

  def logHeader(header: BaseMongoHeader) = {
    log.info(s"REQUEST $header")
    log.info(s"REQUEST MESSAGE LENGTH ${header.length}")
    log.info(s"REQUEST PAYLOAD SIZE ${header.payloadSize}")
    log.info(s"REQUEST ID ${header.requestID}")
    log.info(s"REQUEST ResponseTo ${header.responseTo}")
    log.info(s"REQUEST OPCODE ${header.opCode}")
  }

  private def toFullCollectionName(bytes: Array[Byte]): String =
    (bytes map (_.toChar) mkString) trim

  def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
    val remaining = new Array[Byte](header.payloadSize)

    src.read(remaining)
    import BaseMongoHeader.OpCode._
    header.opCode match {
      case OP_QUERY | OP_GET_MORE => {
        val fullCollectionName = toFullCollectionName(remaining)
        tracker.track(header.requestID, fullCollectionName)
      }
      case _ => ""
    }
    header.bytes ++ remaining
  }

  def readHeader(src: InputStream): BaseMongoHeader = {
    val header = BaseMongoHeader(src)
    logHeader(header)
    header
  }
}