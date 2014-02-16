package com.ee.midas.transform

import org.bson.{BasicBSONObject, BSONObject}
import com.ee.midas.transform.TransformType._
import com.ee.midas.transform.DocumentOperations._
import com.ee.midas.utils.Loggable
import scala.util.matching.Regex
import java.util.Set


trait RequestVersioner extends Loggable {
  var operatorPresent: Option[String] = None

  def addExpansionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = EXPANSION.versionFieldName()
    operatorPresent = None
    val documentToBeVersioned: BSONObject = extractDocumentToBeVersioned(document)
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), documentToBeVersioned))
    documentToBeVersioned + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, documentToBeVersioned))
    assembleVersionedDocument(document, documentToBeVersioned)
  }

  def addContractionVersion(document: BSONObject, version: Double): BSONObject = {
    val versionFieldName = CONTRACTION.versionFieldName()
    operatorPresent = None
    val documentToBeVersioned: BSONObject = extractDocumentToBeVersioned(document)
    logDebug("Current Version %s of Document %s".format(document(versionFieldName), documentToBeVersioned))
    documentToBeVersioned + (versionFieldName, version, false)
    logDebug("Updated Version to %f on Document %s\n".format(version, documentToBeVersioned))
    assembleVersionedDocument(document, documentToBeVersioned)
  }

  private def extractDocumentToBeVersioned(document: BSONObject): BSONObject = {
    val pattern = new Regex("^\\$[a-zA-Z]+")
    val keySet: Set[String] = document.keySet
    val iterator = keySet.iterator

    while(iterator.hasNext) {
      val key = iterator.next
      operatorPresent = (pattern findFirstIn key)
    }

    operatorPresent match {
      case None => document
      case _:Option[String] => {
        if(document.containsField("$set"))
          document.get("$set").asInstanceOf[BSONObject]
        else
          new BasicBSONObject()
      }
    }
  }

  private def assembleVersionedDocument(document:BSONObject, versionedDocument: BSONObject): BSONObject = {
    operatorPresent match {
      case None => versionedDocument
      case _:Option[String] => {
        if(document.containsField("$set"))
          new BasicBSONObject("$set",versionedDocument)
        else {
          document.put("$set",versionedDocument)
          document
        }
      }
    }
  }

}
