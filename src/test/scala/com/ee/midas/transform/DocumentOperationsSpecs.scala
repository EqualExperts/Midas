package com.ee.midas.transform

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.bson.{BasicBSONEncoder, BSONEncoder, BasicBSONObject, BSONObject}
import org.specs2.specification.Scope
import com.mongodb.util.JSON
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._

@RunWith(classOf[JUnitRunner])
class DocumentOperationsSpecs extends Specification {

    "Document Operations" should {

      "Decode documents from input stream" in new setup {
        val encodedDocumentStream = new ByteArrayInputStream(encoder.encode(document))
        val decodedDocument : BSONObject = encodedDocumentStream
        decodedDocument mustEqual document
      }


      "Encode documents to bytes" in new setup {

        //When
        val expectedEncodedDocument = document toBytes

        //Then
        expectedEncodedDocument == encoder.encode(document)
      }


      "Add a single field to the document" in new setup {

        //When
        val transformedDocument = documentOperations + ("version","1.0")

        //Then
        transformedDocument.containsField("version")
      }

      "Remove a single field from the document" in new setup {

        //When
        val transformedDocument = documentOperations - "removeSingle"

        //Then
        !transformedDocument.containsField("removeSingle")
      }

      "Add multiple fields to the document" in new setup {

        val deltas = new BasicBSONObject()
        deltas.put("version", 1)
        deltas.put("id", 1)

        //When
        val transformedDocument = documentOperations ++ deltas

        //Then
        transformedDocument.containsField("version")
        transformedDocument.containsField("id")
      }

      "Remove multiple fields from the document" in new setup {
        val deltas = JSON.parse("[\"removeSingle\", \"removeMultiple\"]").asInstanceOf[BSONObject]

        //When
        val transformedDocument = documentOperations -- deltas

        //Then
        !transformedDocument.containsField("removeSingle") && !transformedDocument.containsField("removeMultiple")
      }
    }

  trait setup extends Scope {
    val document = new BasicBSONObject("name" , "midas")
    document.put("removeSingle", "field")
    document.put("removeMultiple", "fields")

    val documentOperations = DocumentOperations(document)
    val encoder : BSONEncoder = new BasicBSONEncoder()
//    val decoder : DBDecoder = new DefaultDBDecoder()
  }
}
