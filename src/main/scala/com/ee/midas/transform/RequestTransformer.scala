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

import com.ee.midas.config.{ChangeSet}
import TransformType._
import org.bson.BSONObject

trait RequestTransformer extends RequestTypes with RequestVersioner {
  var requestExpansions: Map[ChangeSetCollectionKey, Double]
  var requestContractions: Map[ChangeSetCollectionKey, Double]

  var transformType: TransformType

  def transformRequest(document: BSONObject, changeSet: Long, fullCollectionName: String): BSONObject = {
    val key = (changeSet, fullCollectionName)
    transformType match {
      case EXPANSION => {
        if(requestExpansions.isDefinedAt(key)) {
          val version = requestExpansions(key)
          addExpansionVersion(document, version)
        }
        document
      }

      case CONTRACTION => {
        if (requestExpansions.isDefinedAt(key)) {
          val expansionVersion = requestExpansions(key)
          addExpansionVersion(document, expansionVersion)
        }
        if (requestContractions.isDefinedAt(key)) {
          val contractionVersion = requestContractions(key)
          addContractionVersion(document, contractionVersion)
        }
        document
      }
    }
  }
}
