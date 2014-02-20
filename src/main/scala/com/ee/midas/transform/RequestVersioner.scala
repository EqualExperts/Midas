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
