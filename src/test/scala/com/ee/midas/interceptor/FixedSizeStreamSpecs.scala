package com.ee.midas.interceptor

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import java.io.{ByteArrayInputStream, InputStream}
import org.specs2.mock.Mockito
import org.specs2.matcher.DataTables
import java.lang.RuntimeException

@RunWith(classOf[JUnitRunner])
class FixedSizeStreamSpecs extends Specification with Mockito {

   "FixedSizeStream " should {
       "Return available bytes in InputStream " in {
         //Given
          val inputStream : InputStream = mock[InputStream]
          val limit = 0
          val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

         //When
          fixedStream.available()

         //Then
          there was one(inputStream).available()
       }

     "Return EOF while reading a byte when limit is zero" in {
       //Given
       val inputStream : InputStream = mock[InputStream]
       val limit = 0
       val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

       //When
       val byte=fixedStream.read()

       //Then
       byte mustEqual -1
     }

     "Read a single byte " in {
       //Given
       val inputStream : InputStream = mock[InputStream]
       val limit = 1
       val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

       //When
       fixedStream.read()

       //Then
       there was one(inputStream).read()
     }

     "Reads an array of bytes" in {
       //Given
       val inputStream : InputStream = mock[InputStream]
       val bytes: Array[Byte] = new Array[Byte](10)
       val limit = 10
       val (offset, len) = (0, 10)
       val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

       //When
       fixedStream.read(bytes, offset, len)

       //Then
       there was one(inputStream).read(bytes, offset, len)
     }

     "Return bytes read as -1 when limit is zero" in {
       //Given
       val inputStream : InputStream = mock[InputStream]
       val bytes: Array[Byte] = new Array[Byte](10)
       val limit = 0
       val (offset, len) = (0, 10)
       val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

       //When
       val bytesRead=fixedStream.read(bytes, offset, len)

       //Then
       bytesRead mustEqual -1
     }

     "Return bytes read" in {
       //Given
       val bytes: Array[Byte] = new Array[Byte](10)
       val inputStream : InputStream = new ByteArrayInputStream(bytes)
       val limit = 5
       val (offset, len) = (0, 10)
       val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

       //When
       val bytesRead=fixedStream.read(bytes, offset, len)

       //Then
       bytesRead mustEqual (if(limit > len) len else limit)
     }

     "Throw exception when closed" in {
       //given
       val inputStream : InputStream = mock[InputStream]
       val limit = 1
       val fixedStream : FixedSizeStream = new FixedSizeStream(inputStream, limit)

       //when
       //then
       fixedStream.close() must throwA[RuntimeException]
     }

   }

}
