package com.ee.midas.interceptor

import org.bson.io.Bits
import com.mongodb.{DefaultDBDecoder, DBDecoder, DBCollection}
import com.ee.midas.transform.TransformType
import org.bson.{BasicBSONEncoder, BSONObject}
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._

abstract class Request(data: Array[Byte]) {
  val CSTRING_TERMINATION_DELIM = 0
  val delimiterLength = 1
  val fullCollectionName: String = getFullCollectionName()

  val decoder: DBDecoder = new DefaultDBDecoder
  val encoder: BasicBSONEncoder = new BasicBSONEncoder

  def appendVersion(document: BSONObject, transformType: TransformType) = {
    if (transformType.compareTo(TransformType.EXPANSION) == 0)
        document + ("_expansionVersion", 0)
    else if(transformType.compareTo(TransformType.CONTRACTION) == 0)
            document + ("_contractionVersion", 0)
    }

  private def getFullCollectionName(): String = {
    val result : Array[Byte] = data.takeWhile( _ != CSTRING_TERMINATION_DELIM)
    (result map (_.toChar) mkString)
  }

  def getVersionedData(transformType: TransformType): Array[Byte]
}

case class Update(data: Array[Byte]) extends Request(data) {

  private val updateFlagLength = 4
  private val payloadStartsAt = fullCollectionName.length + updateFlagLength + delimiterLength

  val updateFlag: Int = getUpdateFlag()
  val (selector: BSONObject, updator: BSONObject) = getSelectorUpdator()

  private def getUpdateFlag(): Int = {
    val result = data.dropWhile(_ != CSTRING_TERMINATION_DELIM)
    val startPosIgnoringDelimiter = 1
    val flag = Bits.readInt(result, startPosIgnoringDelimiter)
    flag
  }

  private def getSelectorUpdator(): (BSONObject, BSONObject) = {
    val payload: Array[Byte] = new Array[Byte](data.length - payloadStartsAt)
    Array.copy(data, payloadStartsAt, payload, 0, payload.length)

    val stream = new ByteArrayInputStream(payload)
    val collection: DBCollection = null
    val selector = decoder.decode(stream, collection)
    val updator = decoder.decode(stream, collection)
    (selector, updator)
  }

  def getVersionedData(transformType: TransformType): Array[Byte] = {
    appendVersion(updator, transformType)
    val versionedPayload = encoder.encode(selector) ++ encoder.encode(updator)
    val initialBytes: Array[Byte] = new Array[Byte](payloadStartsAt)
    Array.copy(data, 0, initialBytes, 0, initialBytes.length)
    initialBytes ++ versionedPayload
  }

}

case class Insert(data: Array[Byte]) extends Request(data) {

  private val payloadStartsAt = fullCollectionName.length + delimiterLength

  val document: BSONObject = getDocument()


  private def getDocument(): BSONObject = {
    val payload: Array[Byte] = new Array[Byte](data.length - payloadStartsAt)
    Array.copy(data, payloadStartsAt, payload, 0, payload.length)

    val collection: DBCollection = null
    val document = decoder.decode(payload, collection)
    document
  }

  def getVersionedData(transformType: TransformType): Array[Byte] = {
    appendVersion(document , transformType)
    val versionedPayload = encoder.encode(document)
    val initialBytes: Array[Byte] = new Array[Byte](payloadStartsAt)
    Array.copy(data, 0, initialBytes, 0, initialBytes.length)
    initialBytes ++ versionedPayload
  }
}
