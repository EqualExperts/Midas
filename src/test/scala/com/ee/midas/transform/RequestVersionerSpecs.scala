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
import org.bson.{BSONObject, BasicBSONObject}

@RunWith(classOf[JUnitRunner])
class RequestVersionerSpecs extends Specification with RequestVersioner {
  "Request Versioner " should {

    "add expansion version field if it doesn't exist in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")

      //when
      addExpansionVersion(document, 4d)

      //then
      document.get(TransformType.EXPANSION.versionFieldName()) mustEqual 4d
    }

    "add contraction version field if it doesn't exist in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")

      //when
      addContractionVersion(document, 3d)

      //then
      document.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 3d
    }

    "Do not override expansion version if it exists in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")
      document.put(TransformType.EXPANSION.versionFieldName(), 1d)

      //when
      addExpansionVersion(document, 3d)

      //then
      document.get(TransformType.EXPANSION.versionFieldName()) mustEqual 1d
    }

    "Do not override contraction version if it exists in a document" in {
      //given
      val document = new BasicBSONObject("name", "midas")
      document.put(TransformType.CONTRACTION.versionFieldName(), 2d)

      //when
      addContractionVersion(document, 5d)

      //then
      document.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 2d
    }

    "add expansion version to a document with set update operator" in {
      //given
      val document = new BasicBSONObject("$set", new BasicBSONObject("name", "midas"))

      //when
      addExpansionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.EXPANSION.versionFieldName()) mustEqual 4d
      setDocument.get("name") mustEqual "midas"
    }

    "add expansion version to a document with update operators other than set" in {
      //given
      val document = new BasicBSONObject("$inc", new BasicBSONObject("value", 1))

      //when
      addExpansionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.EXPANSION.versionFieldName()) mustEqual 4d
    }

    "add contraction version to a document with set update operator" in {
      //given
      val document = new BasicBSONObject("$set", new BasicBSONObject("name", "midas"))

      //when
      addContractionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 4d
      setDocument.get("name") mustEqual "midas"
    }

    "add contraction version to a document with update operators other than set" in {
      //given
      val document = new BasicBSONObject("$inc", new BasicBSONObject("value", 1))

      //when
      addContractionVersion(document, 4d)

      //then
      val setDocument: BSONObject = document.get("$set").asInstanceOf[BSONObject]
      setDocument.get(TransformType.CONTRACTION.versionFieldName()) mustEqual 4d
    }
  }
}
