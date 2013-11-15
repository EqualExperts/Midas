package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.{BasicBSONObject, BSONObject, BasicBSONEncoder, BSONEncoder}
import com.mongodb.{DefaultDBDecoder, DBDecoder}
import java.io.{InputStream, ByteArrayInputStream}
import org.specs2.specification.Scope
import com.ee.midas.interceptor.DocumentConverter._

@RunWith(classOf[JUnitRunner])
class DocumentConverterSpecs extends Specification {

    "Converter" should {
        "Encode and Decode documents" in new setup {
          var document = new BasicBSONObject("name", "midas")
          val encodedDocuments : InputStream = new ByteArrayInputStream(toBytes(document))
          val decodedDocument : BSONObject = toDocument(encodedDocuments)

          document mustEqual decodedDocument
        }
    }
}

trait setup extends Scope {
  val encoder : BSONEncoder = new BasicBSONEncoder()
  val decoder : DBDecoder = new DefaultDBDecoder()
}
