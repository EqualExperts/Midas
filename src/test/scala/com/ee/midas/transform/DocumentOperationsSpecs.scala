package com.ee.midas.transform

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.bson.{BasicBSONObject, BSONObject}
import org.specs2.specification.Scope
import com.mongodb.util.JSON

@RunWith(classOf[JUnitRunner])
class DocumentOperationsSpecs extends Specification {

    "Document Operations" should {

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
  }
}
