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

import org.bson.BasicBSONObject
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class RequestTransformerSpecs extends Specification {

  trait Transformations extends Transformer with Scope {
    var responseExpansions : Map[String, VersionedSnippets] = Map()
    var responseContractions : Map[String, VersionedSnippets] = Map()

    var requestExpansions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 4d))
    var requestContractions: Map[ChangeSetCollectionKey, Double] = Map(((1L,"validCollection"), 2d))
  }

  "Request transformer" should {
    "add expansion version in EXPANSION mode" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 4d
    }

    "Do not override expansion version if already exists in EXPANSION mode" in new Transformations {
      //given
      override var transformType = TransformType.EXPANSION
      val document = new BasicBSONObject("name", "dummy")
      document.put(TransformType.EXPANSION.versionFieldName, 3d)

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 3d
    }

    "add contraction and expansion version for CONTRACTION mode" in new Transformations {
      //given
      val document = new BasicBSONObject("name", "dummy")
      override var transformType = TransformType.CONTRACTION

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 4d
      transformedDocument.get("_contractionVersion") mustEqual 2d
    }

    "Do not override contraction version if already exists in CONTRACTION mode" in new Transformations {
      //given
      override var transformType = TransformType.CONTRACTION
      val document = new BasicBSONObject("name", "dummy")
      document.put("_contractionVersion", 1d)

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 4d
      transformedDocument.get("_contractionVersion") mustEqual 1d
    }


    "Do not override expansion version if already exists in CONTRACTION mode" in new Transformations {
      //given
      override var transformType = TransformType.CONTRACTION
      val document = new BasicBSONObject("name", "dummy")
      document.put("_expansionVersion", 3d)

      //when
      val transformedDocument = transformRequest(document, 1, "validCollection")

      //then
      transformedDocument.get("_expansionVersion") mustEqual 3d
      transformedDocument.get("_contractionVersion") mustEqual 2d
    }
  }
}
