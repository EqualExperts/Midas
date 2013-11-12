package com.ee.midas.applier

import java.io.InputStream
import org.bson.{BSONObject, BasicBSONEncoder, BSONEncoder}
import com.mongodb.{DBDecoder, DefaultDBDecoder}


class Applier {

  def applySchema(payloadStream : InputStream, header : MongoHeader) : Array[Byte] = {
    val encoder : BSONEncoder = new BasicBSONEncoder()
    val decoder : DBDecoder = new DefaultDBDecoder()
    val encoderDecoder : EncoderDecoder = new EncoderDecoder(decoder, encoder)

    val documents : List[BSONObject]  = encoderDecoder.decode(payloadStream, header.getNumOfDocuments)
    val newPayload : Array[Byte] = encoderDecoder.encode(new Transformer().transform(documents))
    header.updateLength(newPayload.length)
    Assembler.assemble[Byte](header.getArray, newPayload)
  }

}
