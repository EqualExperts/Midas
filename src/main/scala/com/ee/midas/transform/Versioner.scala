package com.ee.midas.transform

import org.bson.BSONObject
import DocumentOperations._

trait Versioner {

  val VERSION_FIELD = "_version"

  def getVersion(document: BSONObject) =
    if(document.containsField(VERSION_FIELD)) {
      val version = document.get(VERSION_FIELD).asInstanceOf[Int]
      Some(version)
    } else {
      None
    }

  def version (document: BSONObject) : BSONObject = {
    getVersion(document) match {
      case Some(version) => {
        println("Current Version %d of Document %s".format(version, document))
        val nextVersion = version + 1
        document + (VERSION_FIELD, nextVersion)
        println("Updated Version to %d on Document %s\n".format(nextVersion, document))
        document
      }
      case None => {
        println("No Versioning found on Document %s".format(document))
        val version = 1
        document + (VERSION_FIELD, version)
        println("Added Version %d to Document %s\n".format(version, document))
        document
      }
    }
    document
  }
}
