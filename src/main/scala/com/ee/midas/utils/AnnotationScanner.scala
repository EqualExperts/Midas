package com.ee.midas.utils

import scala.tools.asm._
import java.util.jar.{JarEntry, JarInputStream}
import java.io.{File, IOException, FileInputStream}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import com.ee.midas.dsl.expressions.FunctionExpression
import java.util.regex.Pattern
import java.net.URI
import java.util.Collections

class AnnotationScanner(pkg: String, annotationClass: Class[_]) extends Loggable {
  private val slashifiedPkg = slashify(pkg)

  private val slashifiedAnnotation = slashify(annotationClass.getName)

  private val classLoader = AnnotationScanner.this.getClass.getClassLoader

  private val pkgURI = classLoader.getResource(slashifiedPkg).toURI

  private var startDir: Path = null

  val pkgURIString = pkgURI.toString
  log.info(s"PACKAGE URI = $pkgURIString")
  if(pkgURIString.startsWith("jar")) {
    val (jar, _) = pkgURIString.splitAt(pkgURIString.indexOf("!"))
    val jarUri = URI.create(jar)
    log.debug(s"JAR TO SCAN = $jarUri")
    import scala.collection.JavaConverters._
    FileSystems.newFileSystem(jarUri, Map[String, AnyRef]().asJava)
  }
  startDir = Paths.get(pkgURI)
  log.debug(s"STARTDIR URI = $startDir in classpath...")

  private val fileVisitor = new FileVisitor(startDir, Pattern.compile(".*\\.class$"))

  private def slashify(string: String) = string.replaceAllLiterally(".", File.separator)

  private def classesInPackage: Set[String] = {
    log.info(s"Finding package $pkg in classpath...")
    fileVisitor.visit map { file =>
      val index = file.indexOf(slashifiedPkg)
      val className = file.substring(index)
      className.replaceAllLiterally(".class", "")
    }
  }

  private def hasAnnotation(annotationClass: Class[_], className: String): Boolean = {
    log.info(s"Does: Whether class $className have annotation $annotationClass?")
    val slashifiedClassName = slashify(className)
    var foundAnnotation = false
    val cv = new ClassVisitor(Opcodes.ASM4) {
      // Invoked when a class level annotation is encountered
      override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = {
        log.debug(s"ClassVisitor.visitAnnotation($desc, $visible)")
        val annotation = desc.substring(1, desc.length - 1)
        if (annotation == slashifiedAnnotation)
          foundAnnotation = true
        super.visitAnnotation(desc, visible)
      }
    }
    log.debug(s"Visiting class $slashifiedClassName for annotation $slashifiedAnnotation")
    val in = classLoader.getResourceAsStream(slashifiedClassName + ".class")
    try {
      val classReader = new ClassReader(in)
      classReader.accept(cv, 0)
    } catch {
      case _: Throwable =>
    } finally {
      in.close()
    }
    log.info(s"Answer: class $className has annotation $annotationClass = $foundAnnotation")
    foundAnnotation
  }

  def scan = {
    val classes = classesInPackage
    log.info(s"Classpath Classes $classes")
    classesInPackage
      .filter(className => hasAnnotation(annotationClass, className))
      .map(className => className.replaceAllLiterally("/", "."))
  }
}