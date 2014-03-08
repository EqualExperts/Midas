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

import org.bson.BSONObject
import scala.collection.immutable.TreeMap
import TransformType._

trait ResponseTransformer extends ResponseTypes with ResponseVersioner {
  var responseExpansions: Map[String, VersionedSnippets]
  var responseContractions: Map[String, VersionedSnippets]

  var transformType: TransformType

  def transformResponse(document: BSONObject, fullCollectionName: String) : BSONObject =  {
    transformType match {
      case EXPANSION => {
         if(responseExpansions.isDefinedAt(fullCollectionName))
            transform(document, fullCollectionName)
         document
      }
      case CONTRACTION => {
        if(responseContractions.isDefinedAt(fullCollectionName))
          transform(document, fullCollectionName)
        document
      }
    }
  }

  private def transform(document: BSONObject, fullCollectionName: String) : BSONObject = {
    versionedSnippets(fullCollectionName) match {
      case map if map.isEmpty => document
      case vs =>
        val version = getVersion(document)(transformType) match {
          case Some(version) => version + 1
          case None => 1
        }
        val snippets = snippetsFrom(version, vs)
        applySnippets(snippets, document)
    }
  }

  private def versionedSnippets(fullCollectionName: String): VersionedSnippets =
    if(transformType == EXPANSION)
      responseExpansions(fullCollectionName)
    else if(transformType == CONTRACTION)
      responseContractions(fullCollectionName)
    else TreeMap.empty

  private def snippetsFrom(version: Double, versionedSnippets: VersionedSnippets) =
    versionedSnippets.filterKeys(v => v >= version).unzip._2

  private def applySnippets(snippets: Snippets, document: BSONObject): BSONObject =
    snippets.foldLeft(document) {
      case (document, snippet) => (snippet andThen versionDocument)(document)
//        throw new Exception("Just for Fun")
    }

  private def versionDocument(document: BSONObject): BSONObject = version(document)(transformType)
}

