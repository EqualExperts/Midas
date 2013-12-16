package com.ee.midas.hotdeploy

import java.io._
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import scala.collection.JavaConverters._
import java.net.URL
import com.ee.midas.utils.Loggable
import com.ee.midas.transform.TransformsHolder
import scala.Array

class DeltaFilesProcessor(val translator: Translator) extends Loggable with Compilable with Deployable {

  private def convert(deltaFiles: Array[File]): String = {
    val sortedDeltaFiles = deltaFiles.filter(f => f.getName.endsWith(".delta")).sortBy(f => f.getName).toList
    log.info(s"Filtered and Sorted Delta Files = $sortedDeltaFiles")
    translator.translate(sortedDeltaFiles.asJava)
  }

  private def fillTemplate(scalaTemplateFilename: String, translations: String): String = {
    val scalaTemplateContents = scala.io.Source.fromFile(scalaTemplateFilename).mkString
    log.info(s"Scala Template to fill = ${scalaTemplateContents}")
    val scalaFileContents = scalaTemplateContents.replaceAll("###EXPANSIONS-CONTRACTIONS###", translations)
    scalaFileContents
  }

  private def writeTo(writer: Writer, contents: String): Unit = {
    writer.write(contents)
    writer.flush()
  }

  private def translate(deltasDir: URL, scalaTemplateFilename: String, writer: Writer): Unit = {
    val deltaFiles = new File(deltasDir.toURI).listFiles()
    log.info(s"Got Delta Files = $deltaFiles")
    val scalaSnippets = convert(deltaFiles)
    log.info(s"Got Scala Snippets $scalaSnippets")
    val scalaCode = fillTemplate(scalaTemplateFilename, scalaSnippets)
    log.info(s"Filled Scala Template = $scalaCode")
    log.info(s"Writing Scala Code using $writer")
    writeTo(writer, scalaCode)
    log.info(s"Written Scala Code.")
  }

  //1. Translate (Delta -> Scala)
  //2. Compile   (Scala -> ByteCode)
  //3. Deploy    (ByteCode -> JVM)
  def process(deltasDirURI: String, srcScalaTemplateURI: String, srcScalaWriter: Writer, srcScalaFile: File,
              binDirURI: String, clazzName: String): Unit = {
    val loader = getClass.getClassLoader
    log.info(s"ORIGINAL TRANSFORMATIONS = ${TransformsHolder.get}")

    val classpathURI = "."
    val classpathDir = loader.getResource(classpathURI)
    log.info(s"classpathDir = $classpathDir")

    val binDir = loader.getResource(binDirURI)
    log.info(s"output dir = $binDir")

    val srcScalaTemplateFile = loader.getResource(srcScalaTemplateURI)
    log.info(s"Template Scala File = $srcScalaTemplateFile")

    val deltasDir = loader.getResource(deltasDirURI)
    translate(deltasDir, srcScalaTemplateFile.getPath, srcScalaWriter)

    log.info(s"Compiling Delta Files...in ${deltasDir}")
    compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)

    log.info(s"Deploying Delta Files...in JVM")
    val deployedInstance = deploy(loader, Array(binDir), clazzName)

    //TODO: replace with actor model
    log.info(s"All Transformations BEFORE = $TransformsHolder")
    TransformsHolder.set(deployedInstance)
    log.info(s"All Transformations AFTER = $TransformsHolder")
  }
}
