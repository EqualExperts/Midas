package com.ee.midas.interceptor

import org.bson.io.Bits
import com.mongodb.{DefaultDBDecoder, DBDecoder, DBCollection}
import org.bson.{BasicBSONObject, BSONObject}
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._


//todo: Design changes for later
// Request really needs to be composed of BaseMongoHeader and Transformer
// Current scenario is like anemic domain model where we have header and transformer
// both outside. RequestInterceptor, the client, co-ordinates header, sucks out info from
// request, transforms it, and puts it back in the request.
sealed trait Request {
  val CSTRING_TERMINATION_DELIM = 0
  val delimiterLength = 1

  def extractFullCollectionName(data: Array[Byte]): String = {
    val result : Array[Byte] = data.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
  }

  protected val payloadStartIndex: Int
  def extractPayload(data: Array[Byte]) = (data.take(payloadStartIndex), data.drop(payloadStartIndex))

  def extractDocument(): BSONObject
  def reassemble(modifiedDocument: BSONObject): Array[Byte]
}

case class Update(data: Array[Byte]) extends Request {
  private val updateFlagLength = 4
  override protected val payloadStartIndex = extractFullCollectionName(data).length + updateFlagLength + delimiterLength
  val (initialBytes, payload) = extractPayload(data)
  val (selector, updator) = getSelectorUpdator

  def getUpdateFlag(): Int = {
    val result = data.dropWhile(_ != CSTRING_TERMINATION_DELIM)
    val startPosIgnoringDelimiter = 1
    val flag = Bits.readInt(result, startPosIgnoringDelimiter)
    flag
  }

  private def getSelectorUpdator(): (BSONObject, BSONObject) = {
    val decoder: DBDecoder = new DefaultDBDecoder
    val stream = new ByteArrayInputStream(payload)
    val ignoringCollection: DBCollection = null
    val selector = decoder.decode(stream, ignoringCollection)
    val updator = decoder.decode(stream, ignoringCollection)

    (selector, updator)
  }


  def extractDocument: BSONObject = {
    updator
  }

  def reassemble(modifiedDocument: BSONObject): Array[Byte] = {
    val modifiedPayload = selector.toBytes ++ modifiedDocument.toBytes
    initialBytes ++ modifiedPayload
  }
}

case class Insert(data: Array[Byte]) extends Request {

  override protected val payloadStartIndex = extractFullCollectionName(data).length + delimiterLength
  val (initialBytes, payload) = extractPayload(data)
  val document = getDocument()

  private def getDocument(): BSONObject = {
    val decoder: DBDecoder = new DefaultDBDecoder
    val ignoringCollection: DBCollection = null
    decoder.decode(payload, ignoringCollection)
  }

  def extractDocument: BSONObject = {
    document
  }

  def reassemble(modifiedDocument: BSONObject): Array[Byte] = {
    initialBytes ++ modifiedDocument.toBytes
  }
}
