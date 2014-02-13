package com.ee.midas.interceptor

import org.bson.io.Bits
import com.mongodb.{DefaultDBDecoder, DBDecoder, DBCollection}
import com.ee.midas.transform.TransformType
import org.bson.BSONObject
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.transform.TransformType._

sealed trait Request {
  val CSTRING_TERMINATION_DELIM = 0
  val delimiterLength = 1

  def addVersion(document: BSONObject, transformType: TransformType): BSONObject = transformType match {
    case EXPANSION => document + ("_expansionVersion", 0d, false)
    case CONTRACTION => document + ("_contractionVersion", 0d, false)
  }

  def extractFullCollectionName(data: Array[Byte]): String = {
    val result : Array[Byte] = data.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
  }

  protected val payloadStartIndex: Int
  def extractPayload(data: Array[Byte]) = (data.take(payloadStartIndex), data.drop(payloadStartIndex))
  def versioned(transformType: TransformType): Array[Byte]
}

case class Update(data: Array[Byte]) extends Request {
  private val updateFlagLength = 4
  override protected val payloadStartIndex = extractFullCollectionName(data).length + updateFlagLength + delimiterLength
  val (initialBytes, payload) = extractPayload(data)

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

  def versioned(transformType: TransformType): Array[Byte] = {
    val (selector,updator) = getSelectorUpdator
    val versionedUpdator = addVersion(updator, transformType)
    val versionedPayload = selector.toBytes ++ versionedUpdator.toBytes
    initialBytes ++ versionedPayload
  }
}

case class Insert(data: Array[Byte]) extends Request {

  override protected val payloadStartIndex = extractFullCollectionName(data).length + delimiterLength
  val (initialBytes, payload) = extractPayload(data)

  private def getDocument(): BSONObject = {
    val decoder: DBDecoder = new DefaultDBDecoder
    val ignoringCollection: DBCollection = null
    decoder.decode(payload, ignoringCollection)
  }

  def versioned(transformType: TransformType): Array[Byte] = {
    val document = getDocument()
    val versionedDocument = addVersion(document, transformType)
    initialBytes ++ versionedDocument.toBytes
  }
}