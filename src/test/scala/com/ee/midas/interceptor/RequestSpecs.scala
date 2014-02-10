package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject
import com.ee.midas.transform.TransformType

@RunWith(classOf[JUnitRunner])
class RequestSpecs extends Specification {
   sequential
   "Update Request" should {
     //Given
     val data: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
         0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
         0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
         0x17.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte,
         0x75.toByte, 0x6d.toByte, 0x65.toByte, 0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte, 0x15.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte, 0x02.toByte, 0x6e.toByte, 0x61.toByte, 0x6d.toByte, 0x65.toByte,
         0x00.toByte, 0x06.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x6d.toByte, 0x69.toByte, 0x64.toByte,
         0x61.toByte, 0x73.toByte, 0x00.toByte, 0x00.toByte)

     val updateRequest = Update(data)

      "Give Full Collection Name" in {
        //When
        //Then
        updateRequest.fullCollectionName mustEqual "mydb.myCollection"
      }

     "Give Update Flag" in {
       //When
       //Then
       updateRequest.updateFlag mustEqual 0
     }

     "Give Selector Document" in {
       //Given
       val expectedSelector = new BasicBSONObject()
       expectedSelector.put("document", 1)
       //When
       //Then
       updateRequest.selector  mustEqual expectedSelector
     }

     "Give Updator Document" in {
       //Given
       val expectedUpdator = new BasicBSONObject()
       expectedUpdator.put("name", "midas")
       //When
       //Then
       updateRequest.updator mustEqual expectedUpdator
     }

     "Give Versioned data after appending version to Updator Document" in {
       //Given
       val expectedUpdatorDoc : BasicBSONObject = new BasicBSONObject()
       expectedUpdatorDoc.put("name", "midas")
       expectedUpdatorDoc.put("_expansionVersion", 0)

       //When
       val versionedData = updateRequest.getVersionedData(TransformType.EXPANSION)
       val versionedUpdateRequest = Update(versionedData)

       //Then
      versionedUpdateRequest.updator mustEqual expectedUpdatorDoc
     }

   }

  "Insert Request" should {
    //Given
    val data: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
      0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
      0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x28.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
      0x07.toByte, 0x5f.toByte, 0x69.toByte, 0x64.toByte, 0x00.toByte, 0x52.toByte, 0xf8.toByte, 0x6f.toByte,
      0x75.toByte, 0xe0.toByte, 0x07.toByte, 0xb7.toByte, 0x42.toByte, 0xf7.toByte, 0x3b.toByte, 0x8b.toByte,
      0x7f.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte, 0x75.toByte, 0x6d.toByte, 0x65.toByte,
      0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x60.toByte, 0x00.toByte, 0x00.toByte,
      0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte)

    val insertRequest = Insert(data)

    "Give Full Collection Name" in {
      //When
      //Then
      insertRequest.fullCollectionName mustEqual "mydb.myCollection"
    }

    "Give Insert Document" in {
      //When
      val document = insertRequest.document
      //Then
      document.containsField("document")  mustEqual true
    }

    "Give Versioned data after appending version to Insert Document" in {
      //When
      val versionedData = insertRequest.getVersionedData(TransformType.EXPANSION)
      val versionedInsertRequest = Insert(versionedData)
      val versionedDocument = versionedInsertRequest.document

      //Then
      versionedDocument.containsField("document") mustEqual true
      versionedDocument.containsField("_expansionVersion") mustEqual true
      versionedDocument.get("_expansionVersion") mustEqual 0
    }

  }

}
