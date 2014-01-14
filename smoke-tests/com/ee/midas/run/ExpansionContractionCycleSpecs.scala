package com.ee.midas.run

import java.io.{File, FileWriter}
import org.specs2._
import specification._
import form._
import com.ee.midas.fixtures.{CommandTerminal, MongoShell}

class ExpansionContractionCycleSpecs extends Specification with Forms {
  sequential
  var midasTerminal = CommandTerminal("")
  var midasTerminal1 = CommandTerminal("")
  def is = s2"""
    Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. Bob, the Business analyst
               wants to discuss about new marketing strategy. So he approches Dave,
               the Developer.

    Bob: " Hey Dave, as a part of our new marketing strategy, we are planning to track our customers
           by their date-of-births as opposed to using age currently."
    Dave: " Ok, We will add DOB field to the schema and slowly bring all the customers documents to same
            consistent state"
    Dave adding further: " After that , we will remove the age field gradually "
    Bob: " Thanks, that sounds good to me"

    1. Insert documents in the database .
      ${  val form = MongoShell("Open MongoShell", "localhost", 27017).useDatabase("users").
          runCommand(s"""db.customers.insert({name:"Matt", "age": 26, address: {line1: "enter house/street", line2: "enter city"}})""").
          runCommand(s"""db.customers.insert({name:"Beth", "age": 22, address: {line1: "enter house/street", line2: "enter city"}})""").
          runCommand(s"""db.customers.insert({name:"John", "age": 35, address: {line1: "enter house/street", line2: "enter city"}})""").
          retrieve()
          form
      }

    2. Create delta file to add "DOB" at location "deltaSpecs" in "expansion" folder
      ${  val form = Delta("users", "customers", "\"DOB\"", "\"new Date(\\'Jun 23, 1912\\')\"")
          .fillExpansion("/deltaSpecs/expansion/expansion.delta")
          form
      }

    3. Start Midas in EXPANSION mode
      ${  midasTerminal = CommandTerminal("--port", "27020", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "EXPANSION")
          val form = midasTerminal.startMidas
          form
      }

    4. Connect with midas and verify that read documents contain "DOB" field
      ${  val form = MongoShell("IncyWincyShoppingApp UpgradedVersion", "localhost", 27020).useDatabase("users").
          find("customers", "DOB").
          retrieve()
          form
      }

    5. Update and write back the documents to the database
      ${  val form =  MongoShell("IncyWincyShoppingApp UpgradedVersion", "localhost", 27017).useDatabase("users").
          runCommand("""db.customers.update({name: "Matt"}, { $set: {"DOB":"new Date('Jun 25, 1990')", "_expansionVersion": 1}}, {$upsert: true, multi: true})""").
          runCommand("""db.customers.update({name: "Beth"}, { $set: {"DOB":"new Date('Jun 15, 1990')", "_expansionVersion": 1}}, {$upsert: true, multi: true})""").
          runCommand("""db.customers.update({name: "John"}, { $set: {"DOB":"new Date('Jun 05, 1990')", "_expansionVersion": 1}}, {$upsert: true, multi: true})""").
          retrieve()
          form
      }

    6. Create delta file to remove "age" at location "deltaSpecs" in "contraction" folder
      ${  val form = Delta("users", "customers", "\"age\"")
          .fillContraction("/deltaSpecs/contraction/contraction.delta")
          form
      }

    7. Restart Midas in CONTRACTION mode
      ${  midasTerminal.stopMidas(27020)}
      ${  midasTerminal = CommandTerminal("--port", "27040", "--deltasDir", System.getProperty("user.dir") + "/deltaSpecs", "--mode", "CONTRACTION")
          val form = midasTerminal.startMidas
          form
      }

    8. Connect with midas and verify that read documents do not contain "age" field
      ${  val form = MongoShell("Open Command Terminal", "localhost", 27040).useDatabase("users").
          removed("customers", "age").
          retrieve()
          form
      }

    9. Clean up the database
      ${  val form = MongoShell("Open MongoShell", "localhost", 27017).
          useDatabase("users").
          runCommand(s"""db.dropDatabase()""").
          retrieve()
          form
      }

    10. Cleanup Deltas Directory
      ${ Delta("", "", "")
         .deleteDeltaAt(System.getProperty("user.dir") + "/deltaSpecs/expansion")
         val form = Delta("", "", "")
         .deleteDeltaAt(System.getProperty("user.dir") + "/deltaSpecs/contraction")
         form
      }

    11:Shutdown Midas               ${midasTerminal.stopMidas(27040)}
                                                                                                   """
}

import specification.Forms._
case class Delta(db: String, collection: String, field: String, defaultValue: String = "") {
  val expansionDeltaStr = s"use $db" + "\n" + s"db.${collection}.add('{${field} : ${defaultValue}}')"
  val contractionDeltaStr = s"use $db" + "\n" + s"db.${collection}.remove('[${field}]')"

  def createDeltaAt(path:String, deltaStr: String) = {
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
    for(document <- file.listFiles())
      document.delete
    file.delete
    Form()
  }

  def fillExpansion(path : String) = {
    val deltasPath = System.getProperty("user.dir") + "/deltaSpecs"
    val expansionDirPath = deltasPath + "/expansion"
    val deltasDir = new File(deltasPath)
    val expansionDir = new File(expansionDirPath)
    deltasDir.mkdir
    expansionDir.mkdir

    createDeltaAt(System.getProperty("user.dir") + path, expansionDeltaStr)
    Form("Delta").
      tr(s"use $db").
      tr(s"""db.$collection.add('{$field : $defaultValue}')""")
  }

  def fillContraction(path : String) = {
    val deltasPath = System.getProperty("user.dir") + "/deltaSpecs"
    val contractionDirPath = deltasPath + "/contraction"
    val deltasDir = new File(deltasPath)
    val contractionDir = new File(contractionDirPath)
    deltasDir.mkdir
    contractionDir.mkdir

    createDeltaAt(System.getProperty("user.dir") + path, contractionDeltaStr)
    Form("Delta").
      tr(s"use $db").
      tr(s"""db.$collection.remove('[$field]')""")
  }

}
