package com.ee.midas.inject

import java.io.{PrintWriter, File}
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import scala.collection.JavaConverters._

object DeltaFilesProcessor extends App {


  def translate(deltaFiles: Array[File]): String = {
    val sortedDeltaFiles = deltaFiles.filter(f => f.getName.endsWith(".delta")).sortBy(f => f.getName).toList
    println(s"Filtered and Sorted Delta Files = $sortedDeltaFiles")
    val generator = new ScalaGenerator()
    val reader = new Reader()
    val translator = new Translator(reader, generator)
    translator.translate(sortedDeltaFiles.asJava)
  }

  def fillTemplate(scalaTemplateFilename: String, translations: String): String = {
    val scalaTemplateContents = scala.io.Source.fromFile(scalaTemplateFilename).mkString
    val scalaFileContents = scalaTemplateContents.replaceAll("###EXPANSIONS-CONTRACTIONS###", translations)
    println("SCALA FILE CONTENTS = \n$scalaFileContents")
    scalaFileContents
  }

  def writeTo(outputFile: File, contents: String): Unit = {
    println(s"Writing generated output to $outputFile")
    val writer = new PrintWriter(outputFile)
    writer.write(contents)
    writer.close()
    println(s"Completed writing generated output to $outputFile")
  }

  //0. Deltas Dir FileWatcher
  //1. Translate (Delta -> Scala) 
  //2. Compile   (Scala -> ByteCode)
  //3. Deploy    (ByteCode -> JVM)
  override def main(args: Array[String]): Unit = {
    //All the uris below are relative to src/main/resources
    val deltasDirURI = "deltas"
    val srcScalaTemplateURI = "templates/Transformations.scala.template"
    val srcScalaDirURI = "generated/scala"
    val srcScalaFilename = "Transformations.scala"
    val binDirURI = "generated/scala/bin"

    val loader = Thread.currentThread().getContextClassLoader()

    val classpathURI = "."
    val classpathDir = loader.getResource(classpathURI)
    println(s"classpathDir = $classpathDir")

    val binDir = loader.getResource(binDirURI)
    println(s"output dir = $binDir")

    val srcScalaDir = loader.getResource(srcScalaDirURI)
    println(s"Source Scala Dir = $srcScalaDir")

    val srcScalaTemplateFile = loader.getResource(srcScalaTemplateURI)
    println(s"Template Scala File = $srcScalaTemplateFile")

    val compiler = new Compiler
    val deltasDir = loader.getResource(deltasDirURI)
    val watcher = new DirectoryWatcher(deltasDir.getPath)
    new Thread (new Runnable() {
      def run() = {
        watcher.start { e =>
          println(s"Received ${e.kind()}, Context = ${e.context()}}")
          val deltaFiles = new File(deltasDir.toURI).listFiles()
          println(s"Delta Files = $deltaFiles")
          val translations = translate(deltaFiles)
          val code = fillTemplate(srcScalaTemplateFile.getPath, translations)
          println(s"Generated Code = $code")
          val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)
          writeTo(srcScalaFile, code)
          println(s"Compiling Delta Files...in ${deltasDir}")
          compiler.compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)
        }
      }
    }).start()

    Thread.sleep(40 * 1000)
    watcher.stop
  }
}
