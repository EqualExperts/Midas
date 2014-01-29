package com.ee.midas

import java.io._
import com.ee.midas.dsl.Translator
import scala.collection.JavaConverters._
import java.net.{URL}
import com.ee.midas.utils.{ScalaCompiler, Loggable}
import com.ee.midas.transform.{TransformType, Transforms}
import scala.Array
import com.ee.midas.hotdeploy.{DeployableHolder, Deployer}

class DeltaFilesProcessor(val translator: Translator, val deployableHolder: DeployableHolder[Transforms])
  extends Loggable with ScalaCompiler with Deployer {

  private def fillTemplate(scalaTemplateFilename: String, translations: String): String = {
    val scalaTemplateContents = scala.io.Source.fromFile(scalaTemplateFilename).mkString
    logInfo(s"Scala Template to fill = ${scalaTemplateContents}")
    val scalaFileContents = scalaTemplateContents.replaceAllLiterally("###EXPANSIONS-CONTRACTIONS###", translations)
    scalaFileContents
  }

  private def writeTo(writer: Writer, contents: String): Unit = {
    writer.write(contents)
    writer.flush()
  }

  private def translate(transformType: TransformType, deltasDir: URL, scalaTemplateFilename: String, writer: Writer): Unit = {
    val deltaFiles = new File(deltasDir.toURI).listFiles()
    logDebug(s"Delta Files $deltaFiles")
    val sortedDeltaFiles = deltaFiles.filter(f => f.getName.endsWith(".delta")).sortBy(f => f.getName).toList
    logInfo(s"Filtered and Sorted Delta Files $sortedDeltaFiles")
    val scalaSnippets = translator.translate(transformType, sortedDeltaFiles.asJava)
    logInfo(s"Delta Files as Scala Snippets $scalaSnippets")
    val scalaCode = fillTemplate(scalaTemplateFilename, scalaSnippets)
    logInfo(s"Filled Scala Template $scalaCode")
    logInfo(s"Writing Scala Code using $writer")
    writeTo(writer, scalaCode)
    logInfo(s"Written Scala Code.")
  }

  //1. Translate (Delta -> Scala)
  //2. Compile   (Scala -> ByteCode)
  //3. Deploy    (ByteCode -> JVM)
  def process(transformType: TransformType, deltasDir: URL, srcScalaTemplate: URL, srcScalaWriter: Writer, srcScalaFile: File,
              binDir: URL, clazzName: String, classpathDir: URL): Unit = {
    logDebug(
    s"""
     transformType = $transformType
     deltasDir = $deltasDir
     classpathDir = $classpathDir
     binDir = $binDir
     Template Scala File = $srcScalaTemplate
    """.stripMargin)
    logDebug(s"Translating Delta Files...in ${deltasDir}")
    translate(transformType, deltasDir, srcScalaTemplate.getPath, srcScalaWriter)

    logDebug(s"Compiling Delta Files...")
    compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)

    logInfo(s"Deploying Delta Files...in JVM")
    val loader = getClass.getClassLoader
    val deployedInstance = deploy[Transforms](loader, Array(binDir), clazzName)

    //TODO: replace with actor model
    logInfo(s"STATE BEFORE = $deployableHolder")
    deployableHolder.set(deployedInstance)
    logInfo(s"STATE AFTER = $deployableHolder")
  }
}
