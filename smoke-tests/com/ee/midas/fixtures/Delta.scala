package com.ee.midas.fixtures

import java.io.{FileWriter, File}
import org.specs2.form.Form
import org.specs2.specification.Forms._

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
    Form("Write Delta").
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
    Form("Write Delta").
      tr(s"use $db").
      tr(s"""db.$collection.remove('[$field]')""")
  }

}
