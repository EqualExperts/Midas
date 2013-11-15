package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.{BasicBSONObject, BSONObject, BasicBSONEncoder, BSONEncoder}
import com.mongodb.{DefaultDBDecoder, DBDecoder}
import java.io.{InputStream, ByteArrayInputStream}
import org.specs2.specification.Scope
import com.ee.midas.interceptor.DocumentConverter._
import com.ee.midas.transform.DocumentOperations._

@RunWith(classOf[JUnitRunner])
class DocumentConverterSpecs extends Specification {

    "Converter" should {
        "Encode documents" in new setup {
          var document = new BasicBSONObject("name", "midas")
<<<<<<< HEAD
          val encodedDocument = toBytes(document)
          val expectedEncodedDocument = encoder.encode(document)
          encodedDocument mustEqual expectedEncodedDocument
        }
=======
          val encodedDocuments : InputStream = new ByteArrayInputStream(document toBytes)
          val decodedDocument : BSONObject = toDocument(encodedDocuments)
>>>>>>> integrateDSL

        "Decode documents" in new setup {
          var document = new BasicBSONObject("name", "midas")
          val encodedDocumentStream = new ByteArrayInputStream(encoder.encode(document))
          val decodedDocument = toDocument(encodedDocumentStream)
          decodedDocument mustEqual document
        }
    }
}

trait setup extends Scope {
    val encoder : BSONEncoder = new BasicBSONEncoder()
    val decoder : DBDecoder = new DefaultDBDecoder()
}
