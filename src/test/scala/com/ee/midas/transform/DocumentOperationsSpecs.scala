package com.ee.midas.transform

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.bson.{BasicBSONEncoder, BSONEncoder, BasicBSONObject, BSONObject}
import org.specs2.specification.Scope
import com.mongodb.util.JSON
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._
import java.util.regex.Pattern

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


      "Add a single field to the document" in {
        //Given
        val document = new BasicBSONObject("version" , "2.0")

        //When
        val transformedDocument = DocumentOperations(document) + ("version", "1.0")

        //Then
        transformedDocument.get("version") must beEqualTo("1.0")
      }

      "Does not override value of single field added to the document" in {
        //Given
        val document = new BasicBSONObject("version" , "1.0")

        //When
        val transformedDocument = DocumentOperations(document) + ("version", "2.0", false)

        //Then
        transformedDocument.get("version") must beEqualTo("1.0")
      }

      "Add a single level-1 nested field to the document" in {
        //Given
        val document = new BasicBSONObject("address" , "some address")

        //When
        val transformedDocument = DocumentOperations(document) + ("address.city", "Mumbai")

        //Then
        val innerDocument = transformedDocument.get("address").asInstanceOf[BSONObject]
        innerDocument.get("city") mustEqual "Mumbai"
      }

      "Add a single level-2 nested field to the document" in {
        //Given
        val document = new BasicBSONObject("address", "some address")

        //When
        val transformedDocument = DocumentOperations(document) + ("address.line.1", "Some Road")

        //Then
        val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
        val level2Document = level1Document.get("line").asInstanceOf[BSONObject]
        level2Document.get("1") mustEqual "Some Road"
      }

      "Add to a nested field an additional level in the document" in {
        //Given
        val document = new BasicBSONObject("address", new BasicBSONObject("line", "Some Road"))

        //When
        val transformedDocument = DocumentOperations(document) + ("address.line.1", "Some Road")

        //Then
        val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
        val level2Document = level1Document.get("line").asInstanceOf[BSONObject]
        level2Document.get("1") mustEqual "Some Road"
      }

      "Remove a single field from the document" in new setup {
        //When
        val transformedDocument = documentOperations - "removeSingle"

        //Then
        transformedDocument.containsField("removeSingle") must beFalse
      }

      "Add multiple fields to the document" in {
        //Given
        val document = new BasicBSONObject("address", new BasicBSONObject("line", "Some Road"))
        val deltas = new BasicBSONObject().append("version", 1).append("id", 1)

        //When
        val transformedDocument = DocumentOperations(document) ++ deltas

        //Then
        transformedDocument.get("version") must beEqualTo(1)
        transformedDocument.get("id") must beEqualTo(1)
      }


      "Don't override fields that already exist" in {
        //Given
        val document = new BasicBSONObject("address", new BasicBSONObject("line", "Some Road")).append("name", "midas")
        val deltas = new BasicBSONObject().append("name", "notMidas")


        //When
        val transformedDocument = DocumentOperations(document) ++ (deltas, false)

        //Then
        transformedDocument.get("name") mustEqual "midas"
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

      "Splits field on basis of regex supplied " in {
        val document = new BasicBSONObject().append("name", "Mr Joe Shmo")

        val actualDocument = DocumentOperations(document).<~>("name",
          Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
          """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

        actualDocument.containsField("title") must beTrue
        actualDocument.get("title") mustEqual "Mr"

        actualDocument.containsField("fullName") must beTrue
        val expectedNestedDocument = new BasicBSONObject()
                                      .append("firstName", "Joe")
                                      .append ("lastName", "Shmo")
        actualDocument.get("fullName") mustEqual expectedNestedDocument


        val nestedDocument = actualDocument.get("fullName").asInstanceOf[BSONObject]
        nestedDocument.containsField("firstName") must beTrue
        nestedDocument.containsField("lastName") must beTrue
      }

      "Splits field while overriding an existing field" in {
        val document = new BasicBSONObject()
                      .append("name", "Mr Joe Shmo")
                      .append("title", "Mr. ")

        val actualDocument = DocumentOperations(document).<~>("name",
          Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
          """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

        actualDocument.containsField("title") must beTrue
        actualDocument.get("title") mustEqual "Mr"

        actualDocument.containsField("fullName") must beTrue
        val expectedNestedDocument = new BasicBSONObject()
          .append("firstName", "Joe")
          .append ("lastName", "Shmo")
        actualDocument.get("fullName") mustEqual expectedNestedDocument


        val nestedDocument = actualDocument.get("fullName").asInstanceOf[BSONObject]
        nestedDocument.containsField("firstName") must beTrue
        nestedDocument.containsField("lastName") must beTrue
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
