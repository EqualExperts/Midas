package com.ee.midas.fixtures

import java.io.{FileWriter, File}
import org.specs2.form.Form
import org.specs2.specification.Forms._

case class Delta(baseDeltaDir: String , transformType: String, deltaStr: () => String) {

  val deltasPath = System.getProperty("user.dir") + baseDeltaDir
  val fullDirPath = transformType match {
      case "EXPANSION" => deltasPath + "/expansion"
      case "CONTRACTION" => deltasPath + "/contraction"
      case "" => deltasPath
  }

  val deltasDir = new File(deltasPath)
  val typeDir = new File(fullDirPath)
  deltasDir.mkdir
  typeDir.mkdir

  def saveAs(fileName: String) = {
    val file = new File(fullDirPath + "/" + fileName)
    if(file.exists())
      file.delete()
    file.createNewFile()
    val writer = new FileWriter(file)
    writer.write(deltaStr())
    writer.close()
    Thread.sleep(5000)
    var form = Form("Write Delta")
    var lines = deltaStr().split("\n")
    lines = lines.dropRight(1)
    for (line <- lines)
      form = form.tr(field(line))
    form
  }

  def delete(fileName: String) = {
    def file = new File(fullDirPath + "/" + fileName)
    file.delete
    typeDir.delete
    deltasDir.delete
    Form("Delete Delta")
  }

}
