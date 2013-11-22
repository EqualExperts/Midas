package com.ee.midas.transform

import org.bson.BSONObject
import DocumentOperations._
import com.ee.midas.utils.Loggable

trait Versioner extends Loggable {

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
        log.info("Current Version %d of Document %s".format(version, document))
        val nextVersion = version + 1
        document + (versionFieldName, nextVersion)
        log.info("Updated Version to %d on Document %s\n".format(nextVersion, document))
        document
      }
      case None => {
        log.info("No Versioning found on Document %s".format(document))
        val version = 1
        document + (versionFieldName, version)
        log.info("Added Version %d to Document %s\n".format(version, document))
        document
      }
    }
  }
}
