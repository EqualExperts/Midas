package com.ee.midas.interceptor

import java.io.InputStream

class RequestInterceptor private extends MidasInterceptable {
  def read(src: InputStream): Array[Byte] = {
    val header = BaseMongoHeader(src)
    println(s"REQUEST $header")
    println(s"REQUEST MESSAGE LENGTH ${header.length}")
    println(s"REQUEST PAYLOAD SIZE ${header.payloadSize}")
    println(s"REQUEST ID ${header.requestID}")
    println(s"REQUEST ResponseTo ${header.responseTo}")
    println(s"REQUEST OPCODE ${header.opCode}")

    val remaining = new Array[Byte](header.payloadSize)
    src.read(remaining)
    println(s"Remaining = ${remaining map (_.toChar) mkString}")
    import BaseMongoHeader.OpCode._
    val fullCollectionName = header.opCode match {
      case OP_QUERY => toFullCollectionName(remaining)
      case OP_GET_MORE => toFullCollectionName(remaining)
      case _ => ""
    }
    header.bytes ++ remaining
  }



  private def toFullCollectionName(bytes: Array[Byte]): String =
    (bytes map (_.toChar) mkString) trim
}

object RequestInterceptor {
  def apply(): RequestInterceptor = new RequestInterceptor()
}

