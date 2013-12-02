package com.ee.midas.interceptor

import java.io.InputStream
import com.ee.midas.utils.Loggable
import scala.util.control.Breaks.break

class RequestInterceptor (tracker: MessageTracker) extends MidasInterceptable with Loggable {

  private val CSTRING_TERMINATION_DELIM = 0

  private def toFullCollectionName(bytes: Array[Byte]): String = {
    val result : Array[Byte] = bytes.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
 }


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
    log.info(header.toString)
    header
  }
}