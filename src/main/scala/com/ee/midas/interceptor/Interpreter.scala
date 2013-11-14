package com.ee.midas.interceptor

import java.io.InputStream
import org.bson.{BSONEncoder, BSONObject, BasicBSONEncoder}
import com.mongodb.{DBCollection, DBDecoder, DefaultDBDecoder}

class Interpreter {

  val encoder: BSONEncoder = new BasicBSONEncoder()
  val decoder: DBDecoder = new DefaultDBDecoder()
  val collection: DBCollection = null

  def documentsFrom(payloadStream: InputStream, numOfDocuments: Int): List[BSONObject] =
              ((1 to numOfDocuments) map (num => decoder.decode(payloadStream, collection))).toList

  def asBytes(documents: List[BSONObject]): Array[Byte] =
              (documents flatMap (document => encoder.encode(document))).toArray

}
