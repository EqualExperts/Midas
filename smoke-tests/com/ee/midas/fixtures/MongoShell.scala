package com.ee.midas.fixtures

import com.mongodb.{DB, MongoClient}
import org.specs2.form.Form
import org.specs2.specification.Forms._

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

  def copied(collection: String, newOldFields : Array[(String, String)]) = {
    val documents = db.getCollection(collection).find()
    while(documents.hasNext) {
      val document = documents.next()
      println(document)
      shell = shell.tr(field(s"document", document))
      for(newOldField <- newOldFields)
        {
          val newField = newOldField._1
          val oldField = newOldField._2
          shell = shell.tr(prop(s"document.get(${newField})", document.get(newField), document.get(oldField)))
        }
      shell = shell.tr(prop(s"document.get('_expansionVersion')", document.get("_expansionVersion"), newOldFields.length))
    }
    this
  }

  def removed(collection: String, fields: Array[String]) = {
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

  def retrieve() = {
    close()
    shell
  }
}
