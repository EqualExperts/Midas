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
import DocumentOperations._
import com.ee.midas.utils.Loggable

trait ResponseVersioner extends Loggable {

  def getVersion(document: BSONObject)(transformType: TransformType) = {
    val versionFieldName = transformType.versionFieldName()
    if(document.containsField(versionFieldName)) {
      val version = document.get(versionFieldName).asInstanceOf[Double]
      Some(version)
    } else {
      None
    }
  }

  def version (document: BSONObject)(transformType: TransformType): BSONObject = {
    val versionFieldName = transformType.versionFieldName()
    getVersion(document)(transformType) match {
      case Some(version) => {
        logDebug("Current Version %f of Document %s".format(version, document))
        val nextVersion = version + 1d
        document + (versionFieldName, nextVersion)
        logDebug("Updated Version to %f on Document %s\n".format(nextVersion, document))
        document
      }
      case None => {
        logDebug("No Versioning found on Document %s".format(document))
        val version = 1d
        document + (versionFieldName, version)
        logDebug("Added Version %f to Document %s\n".format(version, document))
        document
      }
    }
  }
}
