package com.ee.midas.transform

import org.bson.BSONObject
import com.ee.midas.transform.TransformType._
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.utils.Loggable


//todo: Introduce check for $set here
trait RequestVersioner extends Loggable {
  def addExpansionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = EXPANSION.versionFieldName()
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), document))
    document + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, document))
    document
  }

  def addContractionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = CONTRACTION.versionFieldName()
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), document))
    document + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, document))
    document
  }
}
