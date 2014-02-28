package com.ee.midas.config

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
