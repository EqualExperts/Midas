package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.{BSONObject, BasicBSONObject}
import com.ee.midas.transform.DocumentOperations._

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

    "Extract Updator Document" in {
      //Given
      val expectedUpdator : BasicBSONObject = new BasicBSONObject()
      expectedUpdator.put("name", "midas")

      //When
      val updator = updateRequest.extractDocument

     //Then
      updator mustEqual expectedUpdator
    }

    "Get reassembled Update request with modified updator" in {
       //Given
       val updateFlagLength = 4
       val payloadStartsAt = updateRequest.extractFullCollectionName(updatePayload).length + updateFlagLength + 1
       val modifiedDocument : BSONObject = new BasicBSONObject()
       modifiedDocument.put("name", "midas")
       modifiedDocument.put("_expansionVersion", 0)
       val expectedmodifiedpayload = updatePayload.take(payloadStartsAt) ++ updateRequest.selector.toBytes ++
                                     modifiedDocument.toBytes

       //When
       val modifiedPayload = updateRequest.reassemble(modifiedDocument)

       //Then
       modifiedPayload mustEqual expectedmodifiedpayload
     }

   }

  "Insert Request" should {

    "Extract Document" in {
      //Given
      val expectedDocument : BSONObject = new BasicBSONObject()
      expectedDocument.put("_id", new BasicBSONObject( "$oid", "52f86f75e007b742f73b8b7f") )
      expectedDocument.put("document", 1.0000000013969839)

      //When
      val document = insertRequest.extractDocument

      //Then
      document.toString mustEqual expectedDocument.toString
    }

    "Get reassembled Insert request with modified document" in {
      //Given
      val payloadStartsAt = insertRequest.extractFullCollectionName(insertPayload).length + 1
      val modifiedDocument : BSONObject = new BasicBSONObject()
      modifiedDocument.put("name", "midas")
      modifiedDocument.put("_expansionVersion", 0)
      val expectedmodifiedpayload = insertPayload.take(payloadStartsAt) ++ modifiedDocument.toBytes

      //When
      val modifiedPayload = insertRequest.reassemble(modifiedDocument)

      //Then
      modifiedPayload mustEqual expectedmodifiedpayload
    }
  }

}
