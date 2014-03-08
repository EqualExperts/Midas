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

package com.ee.midas.model

import java.net.URL
import java.io.{FileWriter, File}
import org.specs2.mutable.BeforeAfter

trait MidasConfigurationSetup extends BeforeAfter {

  /* deltas dir */
  val deltasDir = new File("src/test/scala/com/ee/midas/myDeltas")
  val deltasDirURL =  deltasDir.toURI.toURL
  val midasConfig = new File(s"${deltasDir.getAbsolutePath}/midas.config")
  val midasConfigText = """
                          |apps {
                          |  app1
                          |  app2
                          |}
                        """.stripMargin

  /* app1 */
  val app1 = new File(deltasDir.getAbsolutePath + "/app1")
  val app1DirURL: URL = app1.toURI.toURL
  val app1Config = new File(s"${app1.getAbsolutePath}/app1.midas")
  val app1ChangeSet01 = new File(app1.getAbsolutePath + "/001-ChangeSet")
  val app1ChangeSet01Expansion = new File(app1ChangeSet01.getAbsolutePath + "/expansion")
  val app1ChangeSet01Contraction = new File(app1ChangeSet01.getAbsolutePath + "/contraction")
  val app1ChangeSet01ExpansionDeltaFile = new File(app1ChangeSet01Expansion.getPath + "/01-expansion.delta")
  val app1ChangeSet01ContractionDeltaFile = new File(app1ChangeSet01Contraction.getPath + "/01contraction.delta")
  val app1ConfigText = s"""
                          |app1_version1 {
                          |  mode = expansion
                          |  nodeA {
                          |    ip = 127.0.0.1
                          |    changeSet = 2
                          |  }
                          |}
                        """.stripMargin


  /* app 2 */
  val app2 = new File(deltasDir.getAbsolutePath + "/app2")
  val app2DirURL: URL = app2.toURI.toURL
  val app2Config = new File(s"${app2.getAbsolutePath}/app2.midas")
  val app2ChangeSet01 = new File(app2.getAbsolutePath + "/001-ChangeSet")
  val app2ChangeSet01Expansion = new File(app2ChangeSet01.getAbsolutePath + "/expansion")
  val app2ChangeSet01Contraction = new File(app2ChangeSet01.getAbsolutePath + "/contraction")
  val app2ChangeSet01ExpansionDeltaFile = new File(app2ChangeSet01Expansion.getPath + "/01-expansion.delta")
  val app2ChangeSet01ContractionDeltaFile = new File(app2ChangeSet01Contraction.getPath + "/01contraction.delta")
  val app2ConfigText = s"""
                          |app2_version1 {
                          |  mode = contraction
                          |  nodeA {
                          |    ip = 127.0.0.1
                          |    changeSet = 2
                          |  }
                          |}
                        """.stripMargin

  def before: Any = {
    app1ChangeSet01Expansion.mkdirs()
    app1ChangeSet01Contraction.mkdirs()
    app1Config.createNewFile()
    app1ChangeSet01ExpansionDeltaFile.createNewFile()
    app1ChangeSet01ContractionDeltaFile.createNewFile()

    app2ChangeSet01Expansion.mkdirs()
    app2ChangeSet01Contraction.mkdirs()
    app2Config.createNewFile()
    app2ChangeSet01ExpansionDeltaFile.createNewFile()
    app2ChangeSet01ContractionDeltaFile.createNewFile()
  }

  def after: Any = {
    app1ChangeSet01ContractionDeltaFile.delete()
    app1ChangeSet01ExpansionDeltaFile.delete()
    app1ChangeSet01Contraction.delete()
    app1ChangeSet01Expansion.delete()
    app1ChangeSet01.delete()
    app1Config.delete()
    app1.delete()

    app2ChangeSet01ContractionDeltaFile.delete()
    app2ChangeSet01ExpansionDeltaFile.delete()
    app2ChangeSet01Contraction.delete()
    app2ChangeSet01Expansion.delete()
    app2ChangeSet01.delete()
    app2Config.delete()
    app2.delete()

    midasConfig.delete()
    deltasDir.delete()
  }

}
