package com.ee.midas.fixtures

import com.mongodb._
import org.specs2.form.Form
import org.specs2.specification.Forms._
import org.bson.BSONObject
import java.util.ArrayList
import com.ee.midas.transform.DocumentOperations._

case class MongoShell(formName: String, host: String, port: Int) {
  var mongoClient: MongoClient = new MongoClient(host, port)
  var db: DB = null
  var shell = Form(formName)
  var documents: ArrayList[DBObject] = null

  def close = mongoClient.close()

  def useDatabase(dbName: String) = {
    db = mongoClient.getDB(dbName)
    shell = shell.tr(field(s">use $dbName"))
    this
  }

  def runCommand(query: String) = {
    try{
       val result = db.doEval(query)
       shell = shell.tr(prop(s">$query", result.ok(), true))
    } catch {
       case e: Exception => shell = shell.tr("Client cannot Connect")
    }
    this
  }

  def readDocuments(collection: String) = {
    documents = new ArrayList[DBObject]()
    val cursor = db.getCollection(collection).find()
    while(cursor.hasNext) {
      val document = cursor.next()
      documents.add(document)
    }
    this
  }

  def update(collection: String, findQuery: DBObject, updateDocument: DBObject) = {
     val result: WriteResult = db.getCollection(collection).update(findQuery, updateDocument)
     val query = collection + ".update(" + findQuery + ", " + updateDocument + ")"
     shell = shell.tr(prop(query, result.get.getField("updatedExisting"), true))
     this
  }

  def insert(collection: String, insertDocument: DBObject) = {
     val result = db.getCollection(collection).insert(insertDocument)
     val query = collection + ".insert(" + insertDocument + ")"
     shell = shell.tr(prop(query, result.get.getField("ok"), 1.0))
     this
  }

  def verifyIfCopied(newOldFields: Array[(String, String)]) = {
     documents.toArray.foreach({ document =>
       shell = shell.tr(field(s"document", document))
       for(newOldField <- newOldFields)
         verifyIfFieldCopiedIn(document.asInstanceOf[DBObject], newOldField)
       verifyExpansionVersion(document.asInstanceOf[DBObject], newOldFields.length)
     })
     this
  }

  private def verifyIfFieldCopiedIn(document: DBObject,newOldField: (String, String)) = {
     val newField = newOldField._1
     val oldField = newOldField._2
     shell = shell.tr(prop(s"document(${newField})", document(newField), document(oldField)))
  }

  def verifyIfExpanded(noOfExpansions: Int) = {
    documents.toArray.foreach({ document =>
      shell = shell.tr(field(s"document", document))
      verifyExpansionVersion(document.asInstanceOf[DBObject], noOfExpansions)
    })
    this
  }

  def verifyIfContracted(noOfContractions: Int) = {
    documents.toArray.foreach({ document =>
      shell = shell.tr(field(s"document", document))
      verifyContractionVersion(document.asInstanceOf[DBObject], noOfContractions)
    })
    this
  }

  def verifyExpansionVersion(document:DBObject, noOfExpansions: Int) = {
     val expansionVersion = document.asInstanceOf[DBObject].get("_expansionVersion")
     shell = shell.tr(prop("document('_expansionVersion')", expansionVersion, noOfExpansions))
  }

  def verifyIfRemoved(fields: Array[String], noOfContractions: Int) = {
    documents.toArray.foreach({ document =>
      shell = shell.tr(field(s"document", document))
      for(field <- fields)
         shell = shell.tr(prop(s"!document.containsField($field)", !document.asInstanceOf[DBObject].containsField(field), true))
      shell = shell.tr(prop(s"document('_contractionVersion')", document.asInstanceOf[DBObject].get("_contractionVersion"), noOfContractions))
    })
    this
  }

  def verifyIfAdded(fields: Array[String], noOfExpansions: Int) = {
    documents.toArray.foreach({ document =>
      shell = shell.tr(field(s"document", document))
      for(field <- fields)
        shell = shell.tr(prop(s"document.containsField($field)", document.asInstanceOf[DBObject].containsField(field), true))
      shell = shell.tr(prop(s"document('_expansionVersion')", document.asInstanceOf[DBObject].get("_expansionVersion"), noOfExpansions))
    })
    this
  }

  def verifyContractionVersion(document:DBObject, noOfContractions: Int) = {
    val contractionVersion = document.asInstanceOf[DBObject].get("_contractionVersion")
    shell = shell.tr(prop("document('_contractionVersion')", contractionVersion, noOfContractions))
  }

  def retrieve() = {
    close
    shell
  }
}
