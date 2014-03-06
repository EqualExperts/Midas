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

     "Return End Of File while reading a byte when limit is zero" in {
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
