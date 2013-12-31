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
        expectedEncodedDocument mustEqual encoder.encode(document)
      }


      "Add a single field to the document" in new setup {

        //When
        val transformedDocument = documentOperations + ("version","1.0")

        //Then
        transformedDocument.containsField("version") must beTrue
      }

      "Remove a single field from the document" in new setup {

        //When
        val transformedDocument = documentOperations - "removeSingle"

        //Then
        transformedDocument.containsField("removeSingle") must beFalse
      }

      "Add multiple fields to the document" in new setup {

        val deltas = new BasicBSONObject()
        deltas.put("version", 1)
        deltas.put("id", 1)

        //When
        val transformedDocument = documentOperations ++ deltas

        //Then
        transformedDocument.containsField("version") must beTrue
        transformedDocument.containsField("id") must beTrue
      }

      "Add a field to nested document" in new setup {

        val deltas = new BasicBSONObject("nestedField.innerField2","innerValue2")

        //When
        val transformedDocument = documentOperations ++ deltas

        //Then
        transformedDocument.containsField("nestedField") must beTrue
        val nestedDoc = transformedDocument.get("nestedField").asInstanceOf[BasicBSONObject]
        nestedDoc.containsField("innerField2") must beTrue
        nestedDoc.get("innerField2").equals("innerValue2") must beTrue
      }

      "Add fields to deeply nested document" in new setup {

        val deltas = new BasicBSONObject().
            append("addSingle", "singleValue").
            append("addMultiple", "multipleValue").
            append("nestedField.innerField2", "innerValue2").
            append("nestedField.innerField3", "innerValue3").
            append("deeplyNestedField.level1.level2.level3","deeplyNestedValue")

        //When
        val transformedDocument = documentOperations ++ deltas

        //Then
        val expectedDocument = new BasicBSONObject("name" , "midas").
          append("removeSingle", "field").
          append("removeMultiple", "fields").
          append("nestedField", new BasicBSONObject("innerField1", "innerValue1").append("removeNestedField", "field").append("innerField2", "innerValue2").append("innerField3", "innerValue3")).
          append("addSingle", "singleValue").
          append("addMultiple", "multipleValue").
          append("deeplyNestedField", new BasicBSONObject("level1", new BasicBSONObject("level2", new BasicBSONObject("level3", "deeplyNestedValue"))))

        expectedDocument mustEqual transformedDocument

      }

      "Remove multiple fields from the document" in new setup {
        val deltas = JSON.parse("[\"removeSingle\", \"removeMultiple\"]").asInstanceOf[BSONObject]

        //When
        val transformedDocument = documentOperations -- deltas

        //Then
        transformedDocument.containsField("removeSingle") must beFalse
        transformedDocument.containsField("removeMultiple") must beFalse
      }

      "Remove a field from nested document" in new setup {

        val deltas = JSON.parse("[\"nestedField.removeNestedField\"]").asInstanceOf[BSONObject]

        //When
        val transformedDocument = documentOperations -- deltas

        //Then
        transformedDocument.containsField("nestedField") must beTrue
        val nestedDoc = transformedDocument.get("nestedField").asInstanceOf[BasicBSONObject]
        nestedDoc.containsField("removeNestedField") must beFalse
      }

      "Remove fields from deeply nested document" in new setup {
        document.append("deeplyNestedField", new BasicBSONObject("level1", new BasicBSONObject("level2",
                    new BasicBSONObject("level3", 3).append("removeNestedField", "field"))))

        val deltas = JSON.parse("[\"removeSingle\", \"removeMultiple\", \"nestedField.removeNestedField\", \"deeplyNestedField.level1.level2.removeNestedField\"]").asInstanceOf[BSONObject]

        //When
        val transformedDocument = documentOperations -- deltas

        //Then
        val expectedDocument = new BasicBSONObject("name" , "midas").
          append("nestedField", new BasicBSONObject("innerField1", "innerValue1")).
          append("deeplyNestedField", new BasicBSONObject("level1", new BasicBSONObject("level2",
          new BasicBSONObject("level3", 3))))

        expectedDocument mustEqual transformedDocument

      }
    }

  trait setup extends Scope {
    val document = new BasicBSONObject("name" , "midas").
      append("removeSingle", "field").
      append("removeMultiple", "fields").
      append("nestedField", new BasicBSONObject("innerField1", "innerValue1").append("removeNestedField", "field"))

    val documentOperations = DocumentOperations(document)
    val encoder : BSONEncoder = new BasicBSONEncoder()
  }
}
