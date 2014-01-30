package com.ee.midas.fixtures

import com.mongodb.{DB, MongoClient}
import org.specs2.form.Form
import org.specs2.specification.Forms._
import org.bson.BSONObject

case class MongoShell(formName: String, host: String, port: Int) {
  val mongoClient = new MongoClient(host, port)
  var db: DB = null
  var shell = Form(formName)

  def close() = mongoClient.close()

  def useDatabase(dbName: String) = {
    db = mongoClient.getDB(dbName)
    shell = shell.tr(field(s">use $dbName"))
    this
  }

  def runCommand(query: String) = {
    val result = db.doEval(query)
    shell = shell.tr(prop(s">$query", result.ok(), true))
    this
  }

  def verifyIfCopied(collection: String, newOldFields : Array[(String, String)]) = {
    val documents = db.getCollection(collection).find()
    while(documents.hasNext) {
      val document = documents.next()
      println(document)
      shell = shell.tr(field(s"document", document))
      for(newOldField <- newOldFields)
        {
          val newField = newOldField._1
          val oldField = newOldField._2
          val newFieldValue = if(newField.contains("."))
                                 readNestedValue(newField, document)
                              else
                                 document.get(newField)
          println("new value "+newFieldValue)
          val oldFieldValue = if(oldField.contains("."))
                                readNestedValue(oldField, document)
                              else
                                document.get(oldField)
          println("old value "+oldFieldValue)
          shell = shell.tr(prop(s"document.get(${newField})", newFieldValue, oldFieldValue))
        }
      shell = shell.tr(prop(s"document.get('_expansionVersion')", document.get("_expansionVersion"), newOldFields.length))
    }
    this
  }

  def verifyIfRemoved(collection: String, fields: Array[String]) = {
    val documents = db.getCollection(collection).find()
    while(documents.hasNext) {
      val document = documents.next()
      println(document)
      shell = shell.tr(field(s"document", document))
      for(field <- fields)
         shell = shell.tr(prop(s"!document.containsField($field)", !document.containsField(field), true))
      shell = shell.tr(prop(s"document.get('_contractionVersion')", document.get("_contractionVersion"), fields.length))
    }
    this
  }

  def verifyIfAdded(collection: String, fields: Array[String]) = {
    val documents = db.getCollection(collection).find()
    while(documents.hasNext) {
      val document = documents.next()
      println(document)
      shell = shell.tr(field(s"document", document))
//      for(field <- fields)
//        shell = shell.tr(prop(s"document.containsField($field)", document.containsField(field), true))
//      shell = shell.tr(prop(s"document.get('_contractionVersion')", document.get("_contractionVersion"), fields.length+1))
    }
    this
  }

  private def readNestedValue(fieldName: String, document: Object): Object = {
    val nestedFields = fieldName.split("\\.")
    var fieldValue = document
    for(field <- nestedFields) {
      fieldValue = fieldValue.asInstanceOf[BSONObject].get(field)
    }
    fieldValue
  }

  def retrieve() = {
    close()
    shell
  }
}
