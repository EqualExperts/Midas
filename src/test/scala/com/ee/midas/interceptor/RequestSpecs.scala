package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.BasicBSONObject

@RunWith(classOf[JUnitRunner])
class RequestSpecs extends Specification {
   "Update Request" should {
     val data: Array[Byte] = Array(0x6d.toByte, 0x79.toByte, 0x64.toByte, 0x62.toByte, 0x2e.toByte, 0x6d.toByte,
         0x79.toByte, 0x43.toByte, 0x6f.toByte, 0x6c.toByte, 0x6c.toByte, 0x65.toByte, 0x63.toByte, 0x74.toByte,
         0x69.toByte, 0x6f.toByte, 0x6e.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
         0x17.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x64.toByte, 0x6f.toByte, 0x63.toByte,
         0x75.toByte, 0x6d.toByte, 0x65.toByte, 0x6e.toByte, 0x74.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0xf0.toByte, 0x3f.toByte, 0x00.toByte, 0x1d.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte, 0x03.toByte, 0x24.toByte, 0x73.toByte, 0x65.toByte, 0x74.toByte,
         0x00.toByte, 0x12.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x61.toByte, 0x67.toByte,
         0x65.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
         0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)

     val updateRequest = Update(data)
      "Give Full Collection Name" in {
        updateRequest.fullCollectionName mustEqual "mydb.myCollection"
      }

     "Give Update Flag" in {
       updateRequest.updateFlag mustEqual 0
     }

     "Give Selector Document" in {
       val expectedSelector = new BasicBSONObject()
       expectedSelector.put("document", 1)
       updateRequest.selector  mustEqual expectedSelector
     }

     

   }

}
