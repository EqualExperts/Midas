package com.ee.midas.fixtures

import java.io.{PrintWriter, File}
import org.specs2.form.Form
import org.specs2.specification.Forms._

case class Delta(deltaDir: String , deltaStr: () => String) {

  val deltasPath = System.getProperty("user.dir") + deltaDir
  val deltasDir = new File(deltasPath)
  deltasDir.mkdir

  def saveAs(formName: String, fileName: String) = {
    val file = new File(deltasPath + "/" + fileName)
    if(file.exists())
      file.delete()
    file.createNewFile()
    write(deltaStr(), file)
    Thread.sleep(5000)
    var form = Form(formName)
    form = writeToForm(deltaStr(), form)
    form
  }

  def writeToForm(text: String, form: Form): Form = {
    var newForm = form
    var lines = deltaStr().split("\n")
    lines = lines.dropRight(1)
    lines.foreach(line => newForm = newForm.tr(field(line)) )
    newForm
  }

  def write(text: String, toFile: File) = {
    val writer = new PrintWriter(toFile, "utf-8")
    writer.write(text)
    writer.flush()
    writer.close()
  }

  def delete(formName: String, fileName: String) = {
    def file = new File(deltasPath + "/" + fileName)
    file.delete
    deltasDir.delete
    Form(formName)
  }
}

