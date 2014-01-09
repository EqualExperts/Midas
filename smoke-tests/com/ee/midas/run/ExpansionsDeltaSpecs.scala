package com.ee.midas.run

import com.mongodb.{DB, MongoClient}
import java.io.{File, FileWriter}
import org.specs2._
import specification._
import form._
import org.specs2.specification.Forms._

class SchemaExpansionSpecs extends Specification with Forms {
  sequential

  def is = s2"""
    Narration: An "organization" wishes to upgrade its schema for storing employee records.
        As a part of upgraded schema, "skills" need to be added to "employee" collection.

    1. Create delta file to add "skills" at location "deltaSpecs"
      ${  val form = Delta("organization", "employee", "\"skills\"", "\"Java\"")
        .fill("C:/EE/deltaSpecs/expansion.delta")
        form
      }

    2. Start Midas in EXPANSION mode
      ${  val form = CommandTerminal("--port", "27020", "--deltasDir", "C:/EE/deltaSpecs", "--mode", "EXPANSION").
                    startMidas
          form
      }

    3. Create some data in mongo
      ${  val form = MongoShell("localhost", 27017).useDatabase("organization").
          runCommand(s"""db.employee.insert({name:"Matt", "age": 26, address: {line1: "enter house/street", line2: "enter city"}})""").
          runCommand(s"""db.employee.insert({name:"Beth", "age": 22, address: {line1: "enter house/street", line2: "enter city"}})""").
          runCommand(s"""db.employee.insert({name:"John", "age": 35, address: {line1: "enter house/street", line2: "enter city"}})""").
          retrieve()
         form
      }

    4. Connect with midas and verify that documents contain "skills" field
      ${  val form = ClientApplication("localhost", 27020).
        search("organization", "employee", "skills").
        retrieve()
        form
      }

    5. Clean up the database
      ${  val form = MongoShell("localhost", 27017).
        useDatabase("organization").
        runCommand(s"""db.dropDatabase()""").
        retrieve()
        form
      }

    6: Shutdown Midas               ${CommandTerminal.stopMidas(27020)}
                                                                                                 """
}

case object CommandTerminal {
  var commandLine: String = null
  val terminal = MidasUtils

  def apply(args: String*) = {
     commandLine = args.mkString(" ")
     this
  }
  def startMidas = {
    terminal.startMidas(commandLine)
    Form("Command Terminal").
      tr(field(">", s"midas ${commandLine}"))
  }
  def stopMidas(port: Int) = {
    terminal.stopMidas(port)
  }

}

case class ClientApplication(host: String, port: Int) {
  val mongoClient = new MongoClient(host, port)
  var clientApp = Form("ClientApplication")
  def search(database: String, collection: String, newField: String) = {
    println("search")
     val documents = mongoClient.getDB(database).getCollection(collection).find()
    while(documents.hasNext) {
       val document = documents.next()
      println(document)
      clientApp = clientApp.tr(field(s"document", document))
      clientApp = clientApp.tr(prop(s"document.containsField($newField)", document.containsField(newField), true))
    }
    this
  }

  def retrieve() = {
    mongoClient.close()
    clientApp
  }
}

case class MongoShell(host: String, port: Int) {
  val mongoClient = new MongoClient(host, port)
  var db: DB = null
  var shell = Form("MongoShell")

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

  def retrieve() = {
    close()
    shell
  }
}

import specification.Forms._
case class Delta(db: String, collection: String, field: String, defaultValue: String) {
  val deltaStr = s"use $db" + "\n" + s"db.${collection}.add('{${field} : ${defaultValue}}')"

  def createDeltaAt(path:String) = {
    val file = new File(path)
    if(file.exists())
      file.delete()
    file.createNewFile()
    val writer = new FileWriter(file)
    writer.write(deltaStr)
    writer.close()
  }

  def deleteDeltaAt(path:String) = {
    def file = new File(path)
    if(file.exists())
      file.delete()
  }

  def fill(path : String) = {
    createDeltaAt(path)
    Form("Delta").
      tr(s"use $db").
      tr(s"""db.$collection.add('{$field : $defaultValue}')""")
  }

}