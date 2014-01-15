package com.ee.midas.fixtures

import java.io.{FileWriter, File}
import org.specs2.form.Form
import org.specs2.specification.Forms._

case class Delta(baseDeltaDir: String , transformType: String, deltaStr: () => String) {

  val deltasPath = System.getProperty("user.dir") + baseDeltaDir
  val fullDirPath = if(transformType.equals("EXPANSION"))
    deltasPath + "/expansion"
  else
    deltasPath + "/contraction"
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
    Form("Write Delta").
    tr(field(deltaStr()))
  }

  def delete(fileName: String) = {
    def file = new File(fullDirPath + "/" + fileName)
    file.delete
    typeDir.delete
    deltasDir.delete
    Form()
  }

}
