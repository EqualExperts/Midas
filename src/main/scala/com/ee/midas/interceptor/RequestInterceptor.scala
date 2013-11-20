package com.ee.midas.interceptor

import java.io.InputStream

class RequestInterceptor (tracker: MessageTracker) extends MidasInterceptable {

  def logHeader(header: BaseMongoHeader) = {
    println(s"REQUEST $header")
    println(s"REQUEST MESSAGE LENGTH ${header.length}")
    println(s"REQUEST PAYLOAD SIZE ${header.payloadSize}")
    println(s"REQUEST ID ${header.requestID}")
    println(s"REQUEST ResponseTo ${header.responseTo}")
    println(s"REQUEST OPCODE ${header.opCode}")
  }

  private def toFullCollectionName(bytes: Array[Byte]): String =
    (bytes map (_.toChar) mkString) trim

  def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
    val remaining = new Array[Byte](header.payloadSize)

    src.read(remaining)
    println(s"Remaining = ${remaining map (_.toChar) mkString}")
    import BaseMongoHeader.OpCode._
    header.opCode match {
      case OP_QUERY | OP_GET_MORE => {
        val fullCollectionName = toFullCollectionName(remaining)
        tracker.track(header.requestID, fullCollectionName)
      }
      /*case OP_GET_MORE => {
        val fullCollectionName = toFullCollectionName(remaining)
        tracker.track(header.requestID, fullCollectionName)
      }*/
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