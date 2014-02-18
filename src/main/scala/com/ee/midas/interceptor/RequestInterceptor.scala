package com.ee.midas.interceptor

import java.io.{InputStream}
import com.ee.midas.utils.Loggable
import java.net.{InetAddress}
import com.ee.midas.config.{ApplicationListener, Application}

class RequestInterceptor (tracker: MessageTracker, application: Application, ip: InetAddress)
  extends MidasInterceptable with Loggable with ApplicationListener {

  private val CSTRING_TERMINATION_DELIM = 0

  private def extractFullCollectionName(bytes: Array[Byte]): String = {
    val result : Array[Byte] = bytes.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
 }

  def readHeader(request: InputStream): BaseMongoHeader = {
    val header = BaseMongoHeader(request)
    logInfo(header.toString)
    header
  }

  def read(request: InputStream, header: BaseMongoHeader): Array[Byte] = {
    if (header.hasPayload) {
      val requestWithoutHeader = new Array[Byte](header.payloadSize)
      request.read(requestWithoutHeader)
      modifyIfRequired(requestWithoutHeader, header)
    }
    else
      header.bytes
  }

  import BaseMongoHeader.OpCode._
  private def modifyIfRequired(request: Array[Byte], header: BaseMongoHeader): Array[Byte] = {
    val fullCollectionName = extractFullCollectionName(request)
    header.opCode match {
      case OP_INSERT => return modify(Insert(request), fullCollectionName, header)
      case OP_UPDATE => return modify(Update(request), fullCollectionName, header)
      case OP_QUERY | OP_GET_MORE => tracker.track(header.requestID, fullCollectionName)
      case _ =>
    }
    header.bytes ++ request
  }

  private def modify(request: Request, fullCollectionName: String, header: BaseMongoHeader): Array[Byte] = {
    val document = request.extractDocument
    val modifiedDocument = application.transformRequest(document, fullCollectionName, ip)
    val modifiedPayload = request.reassemble(modifiedDocument)
    val newLength = modifiedPayload.length
    header.updateLength(newLength)
    header.bytes ++ modifiedPayload
  }

  def onUpdate(application: Application): Unit = ???
}