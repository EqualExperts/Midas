package com.ee.midas.utils

import scala.tools.asm._
import java.io.File
import java.nio.file._
import java.util.regex.Pattern
import java.net.URI

class AnnotationScanner(pkg: String, annotationClass: Class[_]) extends Loggable {
  private val fsSlashifiedPkg = fsSlashify(pkg)

  private val slashifiedPkg = slashify(pkg)

  private val slashifiedAnnotation = slashify(annotationClass.getName)

  private val classLoader = AnnotationScanner.this.getClass.getClassLoader

  logInfo(s"FS slashified package = $fsSlashifiedPkg")
  logInfo(s"slashified package = $slashifiedPkg, slashified annotation = $slashifiedAnnotation")
  logInfo(s"classloader = $classLoader")

  private val pkgURI = classLoader.getResource(slashifiedPkg).toURI

  private var startDir: Path = null

  val pkgURIString = pkgURI.toString
  logInfo(s"PACKAGE URI = $pkgURIString")
  if(pkgURIString.startsWith("jar")) {
    val (jar, _) = pkgURIString.splitAt(pkgURIString.indexOf("!"))
    val jarUri = URI.create(jar)
    logDebug(s"JAR TO SCAN = $jarUri")
    import scala.collection.JavaConverters._
    FileSystems.newFileSystem(jarUri, Map[String, AnyRef]().asJava)
  }
  startDir = Paths.get(pkgURI)
  logDebug(s"STARTDIR URI = $startDir in classpath...")

  private val fileVisitor = new FileVisitor(startDir, Pattern.compile(".*\\.class$"))

  private def fsSlashify(string: String) = string.replaceAllLiterally(".", File.separator)

  private def slashify(string: String) = string.replaceAllLiterally(".", "/")

  private def dotify(string: String) = string.replaceAllLiterally("/", ".").replaceAllLiterally("\\", ".")

  private def classesInPackage: List[String] = {
    logInfo(s"Finding package $pkg in classpath...")
    fileVisitor.visit map { file =>
      val index = if(pkgURIString.startsWith("jar"))
                    file.indexOf(slashifiedPkg)
                  else
                    file.indexOf(fsSlashifiedPkg)

      val className = file.substring(index)
      className.replaceAllLiterally(".class", "")
    }
  }

  private def hasAnnotation(annotationClass: Class[_], className: String): Boolean = {
    logInfo(s"Does class $className have annotation $annotationClass?")
    val slashifiedClassName = fsSlashify(className)
    var foundAnnotation = false
    val cv = new ClassVisitor(Opcodes.ASM4) {
      // Invoked when a class level annotation is encountered
      override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = {
        logDebug(s"ClassVisitor.visitAnnotation($desc, $visible)")
        val annotation = desc.substring(1, desc.length - 1)
        if (annotation == slashifiedAnnotation)
          foundAnnotation = true
        super.visitAnnotation(desc, visible)
      }
    }
    logDebug(s"Visiting class $slashifiedClassName for annotation $slashifiedAnnotation")
    val in = classLoader.getResourceAsStream(slashifiedClassName + ".class")
    try {
      val classReader = new ClassReader(in)
      classReader.accept(cv, 0)
    } catch {
      case _: Throwable =>
    } finally {
      in.close()
    }
    logInfo(s"class $className has annotation $annotationClass = $foundAnnotation")
    foundAnnotation
  }

  def scan = {
    val classes = classesInPackage
    logInfo(s"Classpath Classes $classes")
    classesInPackage.toSet.filter(className => hasAnnotation(annotationClass, className)).map(dotify)
  }
}