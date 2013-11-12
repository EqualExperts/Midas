package com.ee.midas.applier

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.{BasicBSONObject, BSONObject, BasicBSONEncoder, BSONEncoder}
import com.mongodb.{DBObject, DBCollection, DefaultDBDecoder, DBDecoder}
import java.io.{InputStream, ByteArrayInputStream}
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class EncoderDecoderSpecs extends Specification {

    "EncoderDecoder " should {


        "Encode a document" in new setup {

          val document : BSONObject  = new BasicBSONObject("name", "midas")
          val encodedDocument : Array[Byte] = encoderDecoder.encode(document)
          val collection: DBCollection = null
          val decodedDocument : DBObject = decoder.decode(encodedDocument, collection)

          document mustEqual decodedDocument
        }

        "Decode a Document" in new setup {
          val document : BSONObject = new BasicBSONObject("name", "midas")
          val encodedDocument : InputStream = new ByteArrayInputStream(encoder.encode(document))
          val decodedDocument : List[BSONObject] = encoderDecoder.decode(encodedDocument,1)
          document mustEqual  decodedDocument(0)
        }

        "Encode and Decode Multiple documents" in new setup {
          var documents:List[BSONObject] = List[BSONObject]()
          documents ::= new BasicBSONObject("name", "midas-spike")
          documents ::= new BasicBSONObject("name", "midas-interceptor")
          documents ::= new BasicBSONObject("name", "midas")

          val encodedDocuments : InputStream = new ByteArrayInputStream(encoderDecoder.encode(documents))
          val decodedDocuments : List[BSONObject] = encoderDecoder.decode(encodedDocuments,documents.length)

          documents mustEqual decodedDocuments
        }
    }
}

trait setup extends Scope {
  val encoder : BSONEncoder = new BasicBSONEncoder()
  val decoder : DBDecoder = new DefaultDBDecoder()
  val encoderDecoder : EncoderDecoder =  new EncoderDecoder(decoder, encoder)
}
