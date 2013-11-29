package com.ee.midas.inject

import java.io._
import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator
import scala.collection.JavaConverters._
import java.net.URL
import com.ee.midas.utils.Loggable
import java.nio.file._

object DeltaFilesProcessor extends App with Loggable {

  private def convert(deltaFiles: Array[File]): String = {
    val sortedDeltaFiles = deltaFiles.filter(f => f.getName.endsWith(".delta")).sortBy(f => f.getName).toList
    log.info(s"Filtered and Sorted Delta Files = $sortedDeltaFiles")
    val generator = new ScalaGenerator()
    val reader = new Reader()
    val translator = new Translator(reader, generator)
    translator.translate(sortedDeltaFiles.asJava)
  }

  private def fillTemplate(scalaTemplateFilename: String, translations: String): String = {
    val scalaTemplateContents = scala.io.Source.fromFile(scalaTemplateFilename).mkString
    log.info(s"Scala Template to fill = ${scalaTemplateContents}")
    val scalaFileContents = scalaTemplateContents.replaceAll("###EXPANSIONS-CONTRACTIONS###", translations)
    scalaFileContents
  }

  private def writeTo(outputFile: File, contents: String): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.write(contents)
    writer.close()
  }

  def translate(deltasDir: URL, scalaTemplateFilename: String, outputScalaFile: File) : Unit = {
    val deltaFiles = new File(deltasDir.toURI).listFiles()
    log.info(s"Got Delta Files = $deltaFiles")
    val scalaSnippets = convert(deltaFiles)
    log.info(s"Got Scala Snippets $scalaSnippets")
    val scalaCode = fillTemplate(scalaTemplateFilename, scalaSnippets)
    log.info(s"Filled Scala Template = $scalaCode")
    log.info(s"Writing Scala Code --> $outputScalaFile")
    writeTo(outputScalaFile, scalaCode)
    log.info(s"Written Scala Code --> $outputScalaFile")
  }

  private def copy(fromDir: File, toDir: File): Unit = {
    if(fromDir.isDirectory()){
      if(!toDir.exists()){
        toDir.mkdir()
        log.info("Directory copied from " + fromDir + "  to " + toDir);
      }
      //list all the directory contents
      val files = fromDir.list()
      files.foreach { file: String =>
        val src = new File(fromDir, file)
        val dest = new File(toDir, file)
        copy(src, dest)
      }

    } else {
      Files.copy(Paths.get(fromDir.toURI), Paths.get(toDir.toURI), StandardCopyOption.REPLACE_EXISTING)
      log.info("File copied from " + fromDir + " to " + toDir)
    }
  }

  //0. Deltas Dir FileWatcher
  //1. Translate (Delta -> Scala) 
  //2. Compile   (Scala -> ByteCode)
  //3. Deploy    (ByteCode -> JVM)
  override def main(args: Array[String]): Unit = {
    //All the uris below are relative to src/main/resources
    val deltasDirURI = "deltas/"
    val srcScalaTemplateURI = "templates/Transformations.scala.template"
    val srcScalaDirURI = "generated/scala/"
    val srcScalaFilename = "Transformations.scala"
    val binDirURI = "generated/scala/bin/"

    val loader = Thread.currentThread().getContextClassLoader()

    val classpathURI = "."
    val classpathDir = loader.getResource(classpathURI)
    log.info(s"classpathDir = $classpathDir")

    val binDir = loader.getResource(binDirURI)
    log.info(s"output dir = $binDir")

    val srcScalaDir = loader.getResource(srcScalaDirURI)
    log.info(s"Source Scala Dir = $srcScalaDir")

    val srcScalaTemplateFile = loader.getResource(srcScalaTemplateURI)
    log.info(s"Template Scala File = $srcScalaTemplateFile")

    val deltasDir = loader.getResource(deltasDirURI)
    val watcher = new DirectoryWatcher(deltasDir.getPath)
    val compiler = new Compiler
//    val deployer = new Deployer
//    copy(new File(binDir.toURI), new File(classpathDir.toURI))

    new Thread (new Runnable() {
      def run() = {
        watcher.start { e =>
          log.info(s"Received ${e.kind()}, Context = ${e.context()}")
          val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)
          translate(deltasDir, srcScalaTemplateFile.getPath, srcScalaFile)
          log.info(s"Compiling Delta Files...in ${deltasDir}")
          compiler.compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)
          val fromDir = new File(binDir.toURI)
          val toDir = new File(classpathDir.toURI)
          copy(fromDir, toDir)
          log.info(s"Deploying Delta Files...in JVM")
          Deployer.deploy(loader)
        }
      }
    }).start()

    Thread.sleep(200 * 1000)
    watcher.stop
  }
}
