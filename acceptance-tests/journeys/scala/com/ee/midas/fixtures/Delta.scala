/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.fixtures

import java.io.{PrintWriter, File}
import org.specs2.form.Form
import org.specs2.specification.Forms._

case class Delta(deltaDir: String , deltaStr: () => String) {

  val deltasPath = System.getProperty("user.dir") + deltaDir
  val deltasDir = new File(deltasPath)
  deltasDir.mkdirs

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

