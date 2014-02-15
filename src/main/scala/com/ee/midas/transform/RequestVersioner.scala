package com.ee.midas.transform

import org.bson.{BasicBSONObject, BSONObject}
import com.ee.midas.transform.TransformType._
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.utils.Loggable


//todo: check for other operators
trait RequestVersioner extends Loggable {
  def addExpansionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = EXPANSION.versionFieldName()
    val documentToBeVersioned: BSONObject = extractDocumentToBeVersioned(document)
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), documentToBeVersioned))
    documentToBeVersioned + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, documentToBeVersioned))
    assembleVersionedDocument(documentToBeVersioned)
  }

  def addContractionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = CONTRACTION.versionFieldName()
    val documentToBeVersioned: BSONObject = extractDocumentToBeVersioned(document)
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), documentToBeVersioned))
    documentToBeVersioned + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, documentToBeVersioned))
    assembleVersionedDocument(documentToBeVersioned)
  }

  private def extractDocumentToBeVersioned(document: BSONObject): BSONObject = {
    document.containsField("$set") match {
      case false => document
      case true => document.get("$set").asInstanceOf[BSONObject]
    }
  }

  private def assembleVersionedDocument(versionedDocument: BSONObject): BSONObject = {
    new BasicBSONObject("$set",versionedDocument)
  }
}
