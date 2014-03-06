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

import scala.tools.nsc.{Settings, Global}
import scala.tools.nsc.reporters.ConsoleReporter

trait ScalaCompiler extends Loggable {
  def compile(classpathDir: String, outputDir: String, file: String): Unit =
    compile(classpathDir, outputDir, List(file))

  def compile(classpathDir: String, outputDir: String, files: List[String]): Unit = {
    def error(message: String): Unit = {
      logError(s"Scala Compiler Error $message")
    }
    val runtimeClasspath = System.getProperty("java.class.path")
    logDebug(s"Runtime Classpath = $runtimeClasspath")
    logDebug(s"Inside Compile classpathDir = $classpathDir, outputDirURI = $outputDir, files to compile = $files")
    val settings = new Settings(error)

    settings.outdir.value = outputDir
    settings.processArgumentString("-usejavacp")
    settings.classpath.value = classpathDir

    val reporter = new ConsoleReporter(settings)
    val compiler = new Global(settings, reporter)
    logInfo(s"Started Compilation of $files")
    val run = new compiler.Run
    run compile files
    logInfo(s"Completed Compilation of $files")
//    reporter.printSummary()
    if(reporter.hasErrors) {
      logError(s"Compilation Failed!")
      throw new RuntimeException(s"Compilation Failed!")
    }
  }
}