package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import com.ee.midas.transform.TransformType
import java.io.ByteArrayInputStream
import com.mongodb.{DefaultDBDecoder, DBDecoder, DBCollection}

@RunWith(classOf[JUnitRunner])
class RequestSpecs extends Specification {
   sequential

  val updatePayload: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
    0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
    0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
    0x17.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte,
    0x75.toByte, 0x6d.toByte, 0x65.toByte, 0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte, 0x15.toByte,
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x02.toByte, 0x6e.toByte, 0x61.toByte, 0x6d.toByte, 0x65.toByte,
    0x00.toByte, 0x06.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x6d.toByte, 0x69.toByte, 0x64.toByte,
    0x61.toByte, 0x73.toByte, 0x00.toByte, 0x00.toByte)
  val updateRequest = Update(updatePayload)

  val insertPayload: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
    0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
    0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x28.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
    0x07.toByte, 0x5f.toByte, 0x69.toByte, 0x64.toByte, 0x00.toByte, 0x52.toByte, 0xf8.toByte, 0x6f.toByte,
    0x75.toByte, 0xe0.toByte, 0x07.toByte, 0xb7.toByte, 0x42.toByte, 0xf7.toByte, 0x3b.toByte, 0x8b.toByte,
    0x7f.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte, 0x75.toByte, 0x6d.toByte, 0x65.toByte,
    0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x60.toByte, 0x00.toByte, 0x00.toByte,
    0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte)

  val insertRequest = Insert(insertPayload)
  
  "Request" should {
    "extract FullCollection Name from update" in {
      updateRequest.extractFullCollectionName(updatePayload) mustEqual "mydb.myCollection"
    }
    
    "extract FullCollection Name from insert" in {
      insertRequest.extractFullCollectionName(insertPayload) mustEqual "mydb.myCollection"
    }
  }

  "Update Request" should {
     "Give Update Flag" in {
       //When
       //Then
       updateRequest.getUpdateFlag mustEqual 0
     }

     "Give Versioned data after adding version to Updator Document" in {
       //Given
       val updateFlagLength = 4
       val payloadStartsAt = updateRequest.extractFullCollectionName(updatePayload).length + updateFlagLength + 1
       val decoder: DBDecoder = new DefaultDBDecoder
       val expectedUpdatorDoc : BasicBSONObject = new BasicBSONObject()
       expectedUpdatorDoc.put("name", "midas")
       expectedUpdatorDoc.put("_expansionVersion", 0)

       //When
       val versionedData = updateRequest.versioned(TransformType.EXPANSION)
       val versionedPayload: Array[Byte] = versionedData.drop(payloadStartsAt)

       val stream = new ByteArrayInputStream(versionedPayload)
       val ignoringCollection: DBCollection = null
       val selector = decoder.decode(stream, ignoringCollection)
       val updator = decoder.decode(stream, ignoringCollection)

       //Then
       updator mustEqual expectedUpdatorDoc
     }

   }

  "Insert Request" should {
    "Give Versioned data after adding version to Insert Document" in {
      //Given
      val decoder: DBDecoder = new DefaultDBDecoder
      val ignoringCollection: DBCollection = null
      val payloadStartsAt = insertRequest.extractFullCollectionName(insertPayload).length + 1

      //When
      val versionedData = insertRequest.versioned(TransformType.EXPANSION)

      val versionedPayload: Array[Byte] = versionedData.drop(payloadStartsAt)
      val versionedDocument = decoder.decode(versionedPayload, ignoringCollection)

      //Then
      versionedDocument.containsField("document") mustEqual true
      versionedDocument.containsField("_expansionVersion") mustEqual true
      versionedDocument.get("_expansionVersion") mustEqual 0
    }

  }

}
