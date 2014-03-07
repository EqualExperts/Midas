/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.transform

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.bson.{BasicBSONEncoder, BSONEncoder, BasicBSONObject, BSONObject}
import com.mongodb.util.JSON
import java.io.ByteArrayInputStream
import com.ee.midas.transform.DocumentOperations._
import java.util.regex.Pattern
import java.lang.Exception

@RunWith(classOf[JUnitRunner])
class DocumentOperationsSpecs extends Specification {

    sequential
    "Document Operations" should {

      "Decode documents from input stream" in {
        //Given
        val document = new BasicBSONObject("name" , "midas")
        val encoder : BSONEncoder = new BasicBSONEncoder()

        //when
        val encodedDocumentStream = new ByteArrayInputStream(encoder.encode(document))
        val decodedDocument : BSONObject = encodedDocumentStream

        //Then
        decodedDocument mustEqual document
      }

      "Decode documents from sequence of bytes" in {
        //Given
        val document = new BasicBSONObject("name" , "midas")
        val encoder : BSONEncoder = new BasicBSONEncoder()

        //when
        val encodedDocumentBytes: Array[Byte] = encoder.encode(document)
        val decodedDocument : BSONObject = encodedDocumentBytes

        //Then
        decodedDocument mustEqual document
      }

      "Encode documents to bytes" in {
        //Given
        val document = new BasicBSONObject("name" , "midas")
        val encoder : BSONEncoder = new BasicBSONEncoder()

        //When
        val expectedEncodedDocument = document toBytes

        //Then
        expectedEncodedDocument mustEqual encoder.encode(document)
      }

      "returns document value using indexing by field name" in {
        val document = new BasicBSONObject("pin", 400001)
        document("pin") mustEqual Some(400001)
      }

      "Copy Operation" should {
        "assigns document value using indexing by field name" in {
          //Given
          val document = new BasicBSONObject("pin", 400001)
          val address = new BasicBSONObject("line1", "Some Address")

          //When
          document("address") = address

          //Then
          document("address") mustEqual Some(address)
        }

        "assigns document value using another document value" in {
          //Given
          val document = new BasicBSONObject("pin", 400001)
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("zip") = document("pin").get

          //Then
          targetDocument("zip") mustEqual Some(400001)
        }

        "assigns nested value of a document to another document value" in {
          //Given
          val document = new BasicBSONObject("address", new BasicBSONObject("pin", 400001))
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("zip") = document("address.pin").get

          //Then
          targetDocument("zip") mustEqual Some(400001)
        }

        "assigns nested value of a document to another document's nested field" in {
          //Given
          val document = new BasicBSONObject("address", new BasicBSONObject("pin", 400001))
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("address.zip") = document("address.pin").get

          //Then
          targetDocument("address.zip") mustEqual Some(400001)
        }

        "assigns simple value of a document to another document's nested field" in {
          //Given
          val document = new BasicBSONObject("pin", 400001)
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("address.zip") = document("pin").get

          //Then
          targetDocument("address.zip") mustEqual Some(400001)
        }

        "assigns array value from one field to another non-existent field" in {
          //Given
          val document = new BasicBSONObject("OrderList", "['one','two']")
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("YourCart") = document("OrderList").get

          //Then
          targetDocument("YourCart") mustEqual Some("['one','two']")
        }

        "assigns array value from one field to another existent field" in {
          //Given
          val document = new BasicBSONObject("OrderList", "['one','two']")
          val targetDocument = new BasicBSONObject("YourCart", "SomeValue")

          //When
          targetDocument("YourCart") = document("OrderList").get

          //Then
          targetDocument("YourCart") mustEqual Some("['one','two']")
        }

        "assigns array value from one field to level-1 nested field" in {
          //Given
          val document = new BasicBSONObject("OrderList", "['one','two']")
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("YourCart.order") = document("OrderList").get

          //Then
          targetDocument("YourCart.order") mustEqual Some("['one','two']")
        }

        "assigns array value from one field to level-3 nested field" in {
          //Given
          val document = new BasicBSONObject("OrderList", new BasicBSONObject("orders", "['one','two']"))
          val targetDocument = new BasicBSONObject()

          //When
          targetDocument("YourCart.category.order") = document("OrderList.orders").get

          //Then
          targetDocument("YourCart.category.order") mustEqual Some("['one','two']")
        }
      }

      "Add operation" should {
        "+ Adds a single field to the document" in {
          //Given
          val document = new BasicBSONObject("version" , "2.0")

          //When
          val transformedDocument = document + ("version", "1.0")

          //Then
          transformedDocument.get("version") must beEqualTo("1.0")
        }

        "+ Does not override value of single field added to the document" in {
          //Given
          val document = new BasicBSONObject("version" , "1.0")

          //When
          val transformedDocument = DocumentOperations(document) + ("version", "2.0", false)

          //Then
          transformedDocument.get("version") must beEqualTo("1.0")
        }

        "+ Adds a single level-1 nested field to the document" in {
          //Given
          val document = new BasicBSONObject("address" , "some address")

          //When
          val transformedDocument = DocumentOperations(document) + ("address.city", "Mumbai")

          //Then
          val innerDocument = transformedDocument.get("address").asInstanceOf[BSONObject]
          innerDocument.get("city") mustEqual "Mumbai"
        }

        "+ Adds a single level-2 nested field to the document" in {
          //Given
          val document = new BasicBSONObject("address", "some address")

          //When
          val transformedDocument = DocumentOperations(document) + ("address.line.1", "Some Road")

          //Then
          val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
          val level2Document = level1Document.get("line").asInstanceOf[BSONObject]
          level2Document.get("1") mustEqual "Some Road"
        }

        "+ Adds to a nested field an additional level in the document" in {
          //Given
          val document = new BasicBSONObject("address", new BasicBSONObject("line", "Some Road"))

          //When
          val transformedDocument = DocumentOperations(document) + ("address.line.1", "Some Road")

          //Then
          val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
          val level2Document = level1Document.get("line").asInstanceOf[BSONObject]
          level2Document.get("1") mustEqual "Some Road"
        }

        "+ Adds a new level-2 nested field in the document" in {
          //Given
          val document = new BasicBSONObject()

          //When
          val transformedDocument = DocumentOperations(document) + ("address.line", "Some Road")

          //Then
          val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
          level1Document.get("line") must beEqualTo("Some Road")
        }

        "+ Adds a new field at level-2 nested field in the document" in {
          //Given
          val document = new BasicBSONObject("address", new BasicBSONObject("line1", "Some Road"))

          //When
          val transformedDocument = DocumentOperations(document) + ("address.line2", "Near Some Landmark")

          //Then
          val level1Document = transformedDocument.get("address").asInstanceOf[BSONObject]
          level1Document.get("line1") must beEqualTo("Some Road")
          level1Document.get("line2") must beEqualTo("Near Some Landmark")
        }

        "+ Adds a new array field in a document" in {
          //Given
          val document = new BasicBSONObject("name", "James Tarver")
          val addresses = Array("addressOne", "addressTwo")

          //When
          val transformedDocument = DocumentOperations(document) + ("addresses", addresses)

          //Then
          transformedDocument.containsField("addresses") must beTrue
          transformedDocument.get("addresses").isInstanceOf[Array[String]] must beTrue
          val addressesField = transformedDocument.get("addresses").asInstanceOf[Array[String]]
          addressesField(0) mustEqual "addressOne"
          addressesField(1) mustEqual "addressTwo"
          val expectedDocument = new BasicBSONObject("name", "James Tarver")
            .append("addresses", addresses)
          transformedDocument mustEqual expectedDocument
        }

        "++ Adds multiple fields to the document" in {
          //Given
          val document = new BasicBSONObject("address", new BasicBSONObject("line", "Some Road"))
          val deltas = new BasicBSONObject().append("version", 1).append("id", 1)

          //When
          val transformedDocument = DocumentOperations(document) ++ deltas

          //Then
          transformedDocument.get("version") must beEqualTo(1)
          transformedDocument.get("id") must beEqualTo(1)
        }

        "++ Doesn't override fields that already exist" in {
          //Given
          val document = new BasicBSONObject("address", new BasicBSONObject("line", "Some Road")).append("name", "midas")
          val deltas = new BasicBSONObject().append("name", "notMidas")

          //When
          val transformedDocument = DocumentOperations(document) ++ (deltas, false)

          //Then
          transformedDocument.get("name") mustEqual "midas"
        }

        "++ Adds a new field at level-2 nested field in the document" in {
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

        "++ Adds a new array field in a document" in {
          //Given
          val document = new BasicBSONObject("name", "Julie Tarver")
          val addresses = Array("addressOne", "addressTwo")
          val deltas = new BasicBSONObject("addresses", addresses)

          //When
          val transformedDocument = DocumentOperations(document) ++ deltas

          //Then
          transformedDocument.containsField("addresses") must beTrue
          transformedDocument.get("addresses").isInstanceOf[Array[String]] must beTrue
          val addressesField = transformedDocument.get("addresses").asInstanceOf[Array[String]]
          addressesField(0) mustEqual "addressOne"
          addressesField(1) mustEqual "addressTwo"
          val expectedDocument = new BasicBSONObject("name", "Julie Tarver")
            .append("addresses", addresses)
          transformedDocument mustEqual expectedDocument
        }

        "adds array value to level-1 nested field" in {
          //Given
          val deltas = new BasicBSONObject("YourCart",  new BasicBSONObject("orders", "['one','two']"))
          val document = new BasicBSONObject()

          //When
          val transformedDocument = DocumentOperations(document) ++ deltas

          //Then
          transformedDocument.containsField("YourCart") must beTrue
          transformedDocument("YourCart.orders") mustEqual Some("['one','two']")
        }
      }

      "Remove Operation" should {
        "- Removes a single field from the document" in  {
          //Given
          val document = new BasicBSONObject("name" , "midas")
            .append("redundantField", "some value")

          //When
          val transformedDocument = DocumentOperations(document) - "redundantField"

          //Then
          transformedDocument.containsField("redundantField") must beFalse
        }

        "- Removes a single nested field from the document" in  {
          //Given
          val document = new BasicBSONObject("name" , new BasicBSONObject()
            .append("title", "Mr")
            .append("firstName", "Jerrin")
            .append("lastName", "James"))

          //When
          val transformedDocument = DocumentOperations(document) - "name.title"

          //Then
          val expectedDocument = new BasicBSONObject("name" , new BasicBSONObject()
            .append("firstName", "Jerrin")
            .append("lastName", "James"))
          transformedDocument mustEqual expectedDocument
        }

        "- Removes a single array field from a document" in {
          //Given
          val addresses = Array("addressOne", "addressTwo")
          val document = new BasicBSONObject("name", "Liz Tarver")
            .append("addresses", addresses)

          //When
          val transformedDocument = DocumentOperations(document) - ("addresses")

          //Then
          transformedDocument.containsField("addresses") must beFalse
          val expectedDocument = new BasicBSONObject("name", "Liz Tarver")
          transformedDocument mustEqual expectedDocument
        }

        "- leaves the document unchanged when the target field doesn't exist" in {
          //Given
          val document = new BasicBSONObject("name", "Sue Tarver")
            .append("address", "some road, some city")

          //When
          val transformedDocument = DocumentOperations(document) - ("lane")

          //Then
          transformedDocument.containsField("address") must beTrue
          val expectedDocument = new BasicBSONObject("name", "Sue Tarver")
            .append("address", "some road, some city")
          transformedDocument mustEqual expectedDocument
        }

        "-- Removes multiple fields from the document" in {
          //Given
          val document = new BasicBSONObject("name" , "midas")
            .append("redundantFieldOne", "some value")
            .append("redundantFieldTwo", "some other value")

          val deltas = JSON.parse("[\"redundantFieldOne\", \"redundantFieldTwo\"]").asInstanceOf[BSONObject]

          //When
          val transformedDocument = DocumentOperations(document) -- deltas

          //Then
          transformedDocument.containsField("redundantFieldOne") must beFalse
          transformedDocument.containsField("redundantFieldTwo") must beFalse
        }

        "-- Removes a field from nested document" in {
          //Given
          val document = new BasicBSONObject("name" , "midas")
            .append("details", new BasicBSONObject("name", "old-midas").append("dob", "12-12-1988"))
          val deltas = JSON.parse("[\"details.name\"]").asInstanceOf[BSONObject]

          //When
          val transformedDocument = DocumentOperations(document) -- deltas

          //Then
          transformedDocument.containsField("details") must beTrue
          val nestedDoc = transformedDocument.get("details").asInstanceOf[BasicBSONObject]
          nestedDoc.containsField("name") must beFalse
          nestedDoc.containsField("dob") must beTrue

          val expectedDocument = new BasicBSONObject("name" , "midas")
            .append("details", new BasicBSONObject().append("dob", "12-12-1988"))

          transformedDocument mustEqual expectedDocument
        }

        "-- Removes fields from deeply nested document" in {
          //Given
          val document = new BasicBSONObject()
            .append("details", new BasicBSONObject("name", new BasicBSONObject("firstName", "Paulo")
            .append("lastName", "Coelho"))
            .append("dob", "12-12-1988"))
          val deltas = JSON.parse("[\"details.dob\", \"details.name.lastName\"]").asInstanceOf[BSONObject]

          //When
          val transformedDocument = DocumentOperations(document) -- deltas

          //Then
          val expectedDocument = new BasicBSONObject().
            append("details", new BasicBSONObject("name", new BasicBSONObject("firstName", "Paulo")))

          transformedDocument mustEqual expectedDocument
        }

        "-- Removes an array field from a document" in {
          //Given
          val addresses = Array("addressOne", "addressTwo")
          val document = new BasicBSONObject("name", "Sarah Tarver")
            .append("addresses", addresses)

          //When
          val transformedDocument = DocumentOperations(document) -- JSON.parse("['addresses']").asInstanceOf[BSONObject]

          //Then
          transformedDocument.containsField("addresses") must beFalse
          val expectedDocument = new BasicBSONObject("name", "Sarah Tarver")
          transformedDocument mustEqual expectedDocument
        }

        "-- leaves the document unchanged when the target field doesn't exist" in {
          //Given
          val document = new BasicBSONObject("name", "Beth Tarver")
            .append("address", "some road, some city")

          //When
          val transformedDocument = DocumentOperations(document) -- JSON.parse("['lane.house','city']").asInstanceOf[BSONObject]

          //Then
          transformedDocument.containsField("address") must beTrue
          val expectedDocument = new BasicBSONObject("name", "Beth Tarver")
            .append("address", "some road, some city")
          transformedDocument mustEqual expectedDocument
        }

      }

      "Spilt Operation" should {
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

        "Throw an Exception if invalid regex is supplied" in {
          //Given: A document with "name" field
          val document = new BasicBSONObject().append("name", "Mr Joe Shmo")

          //When: invalid regex is suppiled
          //Then it throws an exception
          DocumentOperations(document).<~>("name",
            Pattern.compile("""^Mr|Mrs|Ms|Miss ([a-zA-Z]+) ([a-zA-Z]+)$"""),
            """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")  must throwAn[Exception]
        }

        "Splits field and assign empty string to extra output fields" in {
          //Given: A document with "name" and "title" fields
          val document = new BasicBSONObject()
            .append("name", "Mr Bob")

          //When: "name" field is split into "title", "fullName.firstName" and "fullName.lastName"
          val actualDocument = DocumentOperations(document).<~>("name",
            Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+)$"""),
            """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

          //Then: Split was successful and extra output field lastName is assigned empty string
          actualDocument.containsField("fullName") must beTrue
          val expectedNestedDocument = new BasicBSONObject()
            .append("firstName", "Bob")
            .append ("lastName", "")
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

        "Splitting a non-existent field results in no change in document" in {
          //Given: A document
          val document = new BasicBSONObject()
            .append("details", new BasicBSONObject("name", "Mr Non Existent"))

          //When: A non-existent field "name" is split into "title", "fullName.firstName" and "fullName.lastName"
          val actualDocument = DocumentOperations(document).<~>("name",
            Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
            """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

          //Then: Split was successful
          actualDocument.containsField("title") must beFalse
          actualDocument.containsField("fullName") must beFalse
          actualDocument mustEqual document
        }

        "Splitting a non-existent nested field results in no change in document" in {
          //Given: A document
          val document = new BasicBSONObject()
            .append("name", "Mr Non Existent")

          //When: A non-existent field "name" is split into "title", "fullName.firstName" and "fullName.lastName"
          val actualDocument = DocumentOperations(document).<~>("details.name",
            Pattern.compile("""^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$"""),
            """{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}""")

          //Then: Split was successful
          actualDocument.containsField("title") must beFalse
          actualDocument.containsField("fullName") must beFalse
          actualDocument mustEqual document
        }

        "Splits a field containing numeric data" in {
          //Given: A document
          val document = new BasicBSONObject()
            .append("details", new BasicBSONObject("telephone", "200-6666"))

          //When: A non-existent field "name" is split into "title", "fullName.firstName" and "fullName.lastName"
          val actualDocument = DocumentOperations(document).<~>("details.telephone",
            Pattern.compile("""^([0-9]+)-([0-9]+)$"""),
            """{"stdCode": "$1", "number": "$2"}""")

          //Then: Split was successful
          actualDocument.containsField("stdCode") must beTrue
          actualDocument.get("stdCode") mustEqual "200"

          actualDocument.containsField("number") must beTrue
          actualDocument.get("number") mustEqual "6666"

          val expectedDocument = new BasicBSONObject()
            .append("details", new BasicBSONObject("telephone", "200-6666"))
            .append("stdCode", "200")
            .append("number", "6666")

          actualDocument mustEqual expectedDocument
        }
      }

      "Merge Operation " should {
        "Merge 2 simple fields into a new field using the provided separator" in {
          //Given: A document
          val document = new BasicBSONObject()
            .append("fName", "Rafael")
            .append("lName", "Nadal")

          //When: fields "fName" and "lName" are merged into new field "name"
          val actualDocument = DocumentOperations(document).>~<("name", " ", List("fName","lName"))

          //Then: Merge was successful
          actualDocument.containsField("name") must beTrue
          actualDocument.get("name") mustEqual "Rafael Nadal"

          val expectedNestedDocument = new BasicBSONObject()
            .append("fName", "Rafael")
            .append ("lName", "Nadal")
            .append("name", "Rafael Nadal")
          actualDocument mustEqual expectedNestedDocument

        }

        "Merge 2 fields - one existent and one non-existent field into a new field" in {
          //Given: A document
          val document = new BasicBSONObject()
            .append("fName", "Rafael")
            .append("lName", "Nadal")

          //When: fields "fName" and "lastName" are merged into new field "name"
          val actualDocument = DocumentOperations(document).>~<("name", " ", List("fName","lastName"))

          //Then: Merge was successful with new field value as existent field's value
          actualDocument.containsField("name") must beTrue
          actualDocument.get("name") mustEqual "Rafael"

          val expectedNestedDocument = new BasicBSONObject()
            .append("fName", "Rafael")
            .append ("lName", "Nadal")
            .append("name", "Rafael")
          actualDocument mustEqual expectedNestedDocument

        }

        "Merge 3 simple fields into a new field using the provided separator" in {
          //Given: A document
          val document = new BasicBSONObject()
            .append("title", "Mr.")
            .append("fName", "Rafael")
            .append("lName", "Nadal")

          //When: fields "title", "fName" and "lName" are merged into new field "name"
          val actualDocument = DocumentOperations(document).>~<("name", " ", List("title","fName","lName"))

          //Then: Merge was successful
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
          //Given: A document with 1 field
          val document = new BasicBSONObject()
            .append("fName", "Andy")

          //When: field "fName" is merged into new field "name"
          val actualDocument = DocumentOperations(document).>~<("name", " ", List("fName"))

          //Then: Merge was successful
          actualDocument.containsField("name") must beTrue
          actualDocument.get("name") mustEqual "Andy"

          val expectedNestedDocument = new BasicBSONObject()
            .append("fName", "Andy")
            .append("name", "Andy")
          actualDocument mustEqual expectedNestedDocument
        }

        "Merge 2 fields with one nested into a new field using given separator" in {
          //Given: A document with nested field "details.firstName" and a simple field "lastName"
          val document = new BasicBSONObject()
            .append("details", new BasicBSONObject("firstName","Roger"))
            .append("lastName","Federer")

          //When: field "details.firstName" and "lastName" are merged into new simple field "name"
          val actualDocument = DocumentOperations(document).>~<("name", " ", List("details.firstName", "lastName"))

          //Then: Merge was successful
          actualDocument.containsField("name") must beTrue
          actualDocument.get("name") mustEqual "Roger Federer"

          val expectedNestedDocument = new BasicBSONObject()
            .append("details", new BasicBSONObject("firstName","Roger"))
            .append("lastName","Federer")
            .append("name", "Roger Federer")
          actualDocument mustEqual expectedNestedDocument
        }

        "Merge 2 fields into a new nested field using given separator" in {
          //Given: A document with 2 simple fields
          val document = new BasicBSONObject()
            .append("firstName","Mike")
            .append("lastName","Russo")

          //When: fields "firstName" and "lastName" are merged into new nested field "details.name"
          val actualDocument = DocumentOperations(document).>~<("details.name", " ", List("firstName", "lastName"))

          //Then: Merge was successful
          actualDocument.containsField("details") must beTrue
          val details = actualDocument.get("details").asInstanceOf[BSONObject]
          details.get("name") mustEqual "Mike Russo"

          val expectedNestedDocument = new BasicBSONObject()
            .append("details", new BasicBSONObject("name", "Mike Russo"))
            .append("lastName","Russo")
            .append("firstName","Mike")
          actualDocument mustEqual expectedNestedDocument
        }

        "Merging fields with no values results in new field with separator as its value" in {
          //Given
          val document = new BasicBSONObject()
            .append("stdCode", "")
            .append("number", "")
          val separator = ":"

          //When
          document >~< ("telephone", separator, List("stdCode", "number"))

          //Then
          document.containsField("telephone") must beTrue
          document.get("telephone").asInstanceOf[String] mustEqual separator
        }
      }
    }
}
