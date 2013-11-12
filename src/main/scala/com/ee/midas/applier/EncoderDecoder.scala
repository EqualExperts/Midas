package com.ee.midas.applier

import com.mongodb.{DBObject, DBCollection, DBDecoder}
import org.bson.{BSONObject, BSONEncoder}
import java.nio.ByteBuffer
import java.io.InputStream


class EncoderDecoder (val decoder : DBDecoder, val encoder : BSONEncoder) {

  def encode(dbObject : BSONObject) : Array[Byte] = encoder.encode(dbObject)

  def encode(transformedDocuments : List[BSONObject] ) : Array[Byte] = {
    var encodedBuffers : List[Array[Byte]] = List[Array[Byte]]()
    var byteArray : Array[Byte] = null
    var totalLength : Int = 0
    for (document: BSONObject <- transformedDocuments) {
      byteArray = encode(document);
      encodedBuffers  = encodedBuffers.::(byteArray)
      totalLength += byteArray.length;
    }
    val output: ByteBuffer = ByteBuffer.wrap(new Array[Byte](totalLength));
    for (array: Array[Byte] <- encodedBuffers) {
      output.put(array);
    }
    output.array();
  }

  def  decode(payloadStream : InputStream, numOfDocuments :  Int) : List[BSONObject] = {
    var documents : List[DBObject] = List[DBObject]()
    for(documentNo <- 0 until numOfDocuments) {
      val collection : DBCollection = null
      documents = documents.::(decoder.decode(payloadStream, collection))
    }
    documents;
  }
}
