package com.ee.midas.interceptor

import java.io.InputStream
import com.ee.midas.utils.Loggable
import scala.util.control.Breaks.break
import org.bson.BasicBSONDecoder
import com.mongodb.{DefaultDBDecoder, DBDecoder}

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
      case OP_UPDATE => {
        val fullCollectionName = toFullCollectionName(remaining)
        println("In update query......... full collection name == "+fullCollectionName)
        println(" collection length == "+fullCollectionName.length+" remaining== "+remaining.length)
        val decoder: DBDecoder = new DefaultDBDecoder()
        var i = fullCollectionName.length
        var j = 0
        val payload:Array[Byte] = new Array[Byte](remaining.length - (fullCollectionName.length+5))
        for(i <- (fullCollectionName.length+5) until remaining.length )
        {
          payload(j) = remaining(i)
          j= j+1
        }

        val dbObject = decoder.decode(payload,null)
        println("Decoded object == "+dbObject)
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