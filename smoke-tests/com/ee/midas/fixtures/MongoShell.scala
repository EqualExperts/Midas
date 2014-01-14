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

  def find(collection: String, newField: String) = {
    println("search")
    val documents = db.getCollection(collection).find()
    while(documents.hasNext) {
      val document = documents.next()
      println(document)
      shell = shell.tr(field(s"document", document))
      shell = shell.tr(prop(s"document.containsField($newField)", document.containsField(newField), true))
      shell = shell.tr(prop(s"document.get('_expansionVersion')", document.get("_expansionVersion"), 1))
    }
    this
  }

  def removed(collection: String, newField: String) = {
    println("search")
    val documents = db.getCollection(collection).find()
    while(documents.hasNext) {
      val document = documents.next()
      println(document)
      shell = shell.tr(field(s"document", document))
      shell = shell.tr(prop(s"!document.containsField($newField)", !document.containsField(newField), true))
      shell = shell.tr(prop(s"document.get('_contractionVersion')", document.get("_contractionVersion"), 1))
    }
    this
  }

  def retrieve() = {
    close()
    shell
  }
}
