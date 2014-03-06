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