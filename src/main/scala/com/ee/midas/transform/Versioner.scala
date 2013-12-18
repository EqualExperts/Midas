package com.ee.midas.transform

import org.bson.BSONObject
import DocumentOperations._
import com.ee.midas.utils.Loggable

trait Versioner extends Loggable {

  def getVersion(document: BSONObject)(implicit transformType: TransformType) = {
    val versionFieldName = transformType.versionFieldName()
    if(document.containsField(versionFieldName)) {
      val version = document.get(versionFieldName).asInstanceOf[Double]
      Some(version)
    } else {
      None
    }
  }

  def version (document: BSONObject)(implicit transformType: TransformType): BSONObject = {
    val versionFieldName = transformType.versionFieldName()
    getVersion(document) match {
      case Some(version) => {
        log.debug("Current Version %f of Document %s".format(version, document))
        val nextVersion = version + 1d
        document + (versionFieldName, nextVersion)
        log.debug("Updated Version to %f on Document %s\n".format(nextVersion, document))
        document
      }
      case None => {
        log.debug("No Versioning found on Document %s".format(document))
        val version = 1d
        document + (versionFieldName, version)
        log.debug("Added Version %f to Document %s\n".format(version, document))
        document
      }
    }
  }
}
