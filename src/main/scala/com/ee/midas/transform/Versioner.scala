package com.ee.midas.transform

import org.bson.BSONObject
import DocumentOperations._

trait Versioner {

  def getVersion(document: BSONObject)(implicit transformType: TransformType) = {
    val versionFieldName = transformType.versionFieldName()
    if(document.containsField(versionFieldName)) {
      val version = document.get(versionFieldName).asInstanceOf[Int]
      Some(version)
    } else {
      None
    }
  }

  def version (document: BSONObject)(implicit transformType: TransformType): BSONObject = {
    val versionFieldName = transformType.versionFieldName()
    getVersion(document) match {
      case Some(version) => {
        println("Current Version %d of Document %s".format(version, document))
        val nextVersion = version + 1
        document + (versionFieldName, nextVersion)
        println("Updated Version to %d on Document %s\n".format(nextVersion, document))
        document
      }
      case None => {
        println("No Versioning found on Document %s".format(document))
        val version = 1
        document + (versionFieldName, version)
        println("Added Version %d to Document %s\n".format(version, document))
        document
      }
    }
  }
}
