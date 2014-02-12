package com.ee.midas.interceptor

import java.io.{InputStream}
import com.ee.midas.utils.Loggable
import com.ee.midas.transform.TransformType

class RequestInterceptor (tracker: MessageTracker, transformType: TransformType) extends MidasInterceptable with Loggable {

  private val CSTRING_TERMINATION_DELIM = 0

  private def extractFullCollectionName(bytes: Array[Byte]): String = {
    val result : Array[Byte] = bytes.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
 }

  def read(src: InputStream, header: BaseMongoHeader): Array[Byte] = {
    val remaining = new Array[Byte](header.payloadSize)

    src.read(remaining)
    import BaseMongoHeader.OpCode._
    header.opCode match {
      case OP_QUERY | OP_GET_MORE => {
        val fullCollectionName = extractFullCollectionName(remaining)
        tracker.track(header.requestID, fullCollectionName)
      }

      case OP_UPDATE | OP_INSERT => {
        val payload: Request = if(header.opCode == OP_UPDATE) Update(remaining) else Insert(remaining)
        val versionedPayload = payload.versioned(transformType)
        val newLength = versionedPayload.length
        header.updateLength(newLength)
        return header.bytes ++ versionedPayload
      }

      case _ => ""
    }
    header.bytes ++ remaining
  }

  def readHeader(src: InputStream): BaseMongoHeader = {
    val header = BaseMongoHeader(src)
    logInfo(header.toString)
    header
  }
}