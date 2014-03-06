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

import org.bson.{BasicBSONObject, BSONObject}
import com.ee.midas.transform.TransformType._
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.utils.Loggable
import scala.util.matching.Regex
import java.util.Set

trait RequestVersioner extends Loggable {

  def addExpansionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = EXPANSION.versionFieldName()
    val documentToBeVersioned: BSONObject = extractDocumentToBeVersioned(document)
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), documentToBeVersioned))
    documentToBeVersioned + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, documentToBeVersioned))
    assembleVersionedDocument(document, documentToBeVersioned)
  }

  def addContractionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = CONTRACTION.versionFieldName()
    val documentToBeVersioned: BSONObject = extractDocumentToBeVersioned(document)
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), documentToBeVersioned))
    documentToBeVersioned + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, documentToBeVersioned))
    assembleVersionedDocument(document, documentToBeVersioned)
  }

  private def extractDocumentToBeVersioned(document: BSONObject): BSONObject = {
    checkForOperator(document) match {
      case false => document
      case true => {
        if(document.containsField("$set"))
          document.get("$set").asInstanceOf[BSONObject]
        else
          new BasicBSONObject()
      }
    }
  }

  private def assembleVersionedDocument(document:BSONObject, versionedDocument: BSONObject): BSONObject = {
    checkForOperator(document) match {
      case false => versionedDocument
      case true => {
        if(document.containsField("$set"))
          new BasicBSONObject("$set",versionedDocument)
        else {
          document.put("$set",versionedDocument)
          document
        }
      }
    }
  }

  private def checkForOperator(document: BSONObject): Boolean = {
    val pattern = new Regex("^\\$[a-zA-Z]+")
    val keys: Set[String] = document.keySet
    keys.toArray.exists(key => (pattern findFirstIn key.asInstanceOf[String]) == Option(key))
  }

}
