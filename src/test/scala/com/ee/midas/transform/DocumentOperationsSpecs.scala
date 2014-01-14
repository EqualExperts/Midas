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

      "Adding a new level-2 nested field in the document" in {
        //Given
        val document = new BasicBSONObject()

        //When
        val transformedDocument = DocumentOperations(document) + ("address.line", "Some Road")

        //Then
        val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
        level1Document.get("line") must beEqualTo("Some Road")
      }

      "Adding a new field at level-2 nested field in the document" in {
        //Given
        val document = new BasicBSONObject("address", new BasicBSONObject("line1", "Some Road"))

        //When
        val transformedDocument = DocumentOperations(document) + ("address.line2", "Near Some Landmark")

        //Then
        val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
        level1Document.get("line1") must beEqualTo("Some Road")
        level1Document.get("line2") must beEqualTo("Near Some Landmark")
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

      "Adding a new field at level-2 nested field in the document using multiple fields add" in {
        //Given
        val document = new BasicBSONObject("address", new BasicBSONObject("line1", "Some Road"))
        val deltas = new BasicBSONObject("address.line2", "Near Some Landmark")

        //When
        val transformedDocument = DocumentOperations(document) ++ deltas

        //Then
        val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
        level1Document.get("line1") must beEqualTo("Some Road")
        level1Document.get("line2") must beEqualTo("Near Some Landmark")
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

      "Splits a field on the basis of regex supplied " in {
        //Given: A document with "name" field
        val document = new BasicBSONObject().append("name", "Mr Joe Shmo")

        //When: "name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).<~>("name",
          Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
          """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

        //Then: Split operation was successful
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
        //Given: A document with "name" and "title" fields
        val document = new BasicBSONObject()
                      .append("name", "Mr Bob Martin")
                      .append("title", "Mr. ")

        //When: "name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).<~>("name",
          Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
          """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

        //Then: Split was successful and "title" field was over-ridden
        actualDocument.containsField("title") must beTrue
        actualDocument.get("title") mustEqual "Mr"

        actualDocument.containsField("fullName") must beTrue
        val expectedNestedDocument = new BasicBSONObject()
          .append("firstName", "Bob")
          .append ("lastName", "Martin")
        actualDocument.get("fullName") mustEqual expectedNestedDocument


        val nestedDocument = actualDocument.get("fullName").asInstanceOf[BSONObject]
        nestedDocument.containsField("firstName") must beTrue
        nestedDocument.containsField("lastName") must beTrue
      }

      "Splits a nested field based on supplied regex" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("details", new BasicBSONObject("name", "Mr Vivek Dhapola"))

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).<~>("details.name",
          Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
          """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")
        println("actual doc is: " + actualDocument)
        //Then: Split was successful
        actualDocument.containsField("title") must beTrue
        actualDocument.get("title") mustEqual "Mr"

        actualDocument.containsField("fullName") must beTrue
        val expectedNestedDocument = new BasicBSONObject()
          .append("firstName", "Vivek")
          .append ("lastName", "Dhapola")
        actualDocument.get("fullName") mustEqual expectedNestedDocument


        val nestedDocument = actualDocument.get("fullName").asInstanceOf[BSONObject]
        nestedDocument.containsField("firstName") must beTrue
        nestedDocument.containsField("lastName") must beTrue
      }

      "Merge 2 simple fields into a new field using the provided separator" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("fName", "Rafael")
          .append("lName", "Nadal")

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).>~<("name", " ", List("fName","lName"))

        //Then: Split was successful
        actualDocument.containsField("name") must beTrue
        actualDocument.get("name") mustEqual "Rafael Nadal"

        val expectedNestedDocument = new BasicBSONObject()
          .append("fName", "Rafael")
          .append ("lName", "Nadal")
          .append("name", "Rafael Nadal")
        actualDocument mustEqual expectedNestedDocument

      }

      "Merge 3 simple fields into a new field using the provided separator" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("title", "Mr.")
          .append("fName", "Rafael")
          .append("lName", "Nadal")

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).>~<("name", " ", List("title","fName","lName"))

        //Then: Split was successful
        actualDocument.containsField("name") must beTrue
        actualDocument.get("name") mustEqual "Mr. Rafael Nadal"

        val expectedNestedDocument = new BasicBSONObject()
          .append("fName", "Rafael")
          .append ("lName", "Nadal")
          .append("title", "Mr.")
          .append("name", "Mr. Rafael Nadal")
        actualDocument mustEqual expectedNestedDocument
      }

      "Merging 1 simple fields falls back to simple copy" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("fName", "Andy")

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).>~<("name", " ", List("fName"))

        //Then: Split was successful
        actualDocument.containsField("name") must beTrue
        actualDocument.get("name") mustEqual "Andy"

        val expectedNestedDocument = new BasicBSONObject()
          .append("fName", "Andy")
          .append("name", "Andy")
        actualDocument mustEqual expectedNestedDocument
      }

      "Merge 2 nested fields into a new field using given separator" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("details", new BasicBSONObject("firstName","Roger"))
          .append("lastName","Federer")

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).>~<("name", " ", List("details.firstName", "lastName"))

        //Then: Split was successful
        actualDocument.containsField("name") must beTrue
        actualDocument.get("name") mustEqual "Roger Federer"

        val expectedNestedDocument = new BasicBSONObject()
          .append("details", new BasicBSONObject("firstName","Roger"))
          .append("lastName","Federer")
          .append("name", "Roger Federer")
        actualDocument mustEqual expectedNestedDocument
      }

      "Merge 2 fields into a new nested field using given separator" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("firstName","Mike")
          .append("lastName","Russo")

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).>~<("details.name", " ", List("firstName", "lastName"))

        //Then: Split was successful
        actualDocument.containsField("details") must beTrue
        val details = actualDocument.get("details").asInstanceOf[BSONObject]
        details.get("name") mustEqual "Mike Russo"

        val expectedNestedDocument = new BasicBSONObject()
          .append("details", new BasicBSONObject("name", "Mike Russo"))
          .append("lastName","Russo")
          .append("firstName","Mike")
        actualDocument mustEqual expectedNestedDocument
      }

      "Merge 2 fields into a new nested field using given separator" in {
        //Given: A document with nested field "details.name"
        val document = new BasicBSONObject()
          .append("firstName","Mike")
          .append("lastName","Russo")

        //When: "details.name" field is split into "title", "fullName.firstName" and "fullName.lastName"
        val actualDocument = DocumentOperations(document).>~<("details.name", " ", List("firstName", "lastName"))

        //Then: Split was successful
        actualDocument.containsField("details") must beTrue
        val details = actualDocument.get("details").asInstanceOf[BSONObject]
        details.get("name") mustEqual "Mike Russo"

        val expectedNestedDocument = new BasicBSONObject()
          .append("details", new BasicBSONObject("name", "Mike Russo"))
          .append("lastName","Russo")
          .append("firstName","Mike")
        actualDocument mustEqual expectedNestedDocument
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
