package com.ee.midas.dsl

import com.ee.midas.dsl.generator.ScalaGenerator
import com.ee.midas.dsl.interpreter.Reader

def sortedDeltaFiles(File deltasDir) {
    deltasDir.listFiles(new FilenameFilter() {
        @Override
        boolean accept(File dir, String name) {
            name.endsWith '.delta'
        }
    }).sort().toList()
}

def translate(deltaFiles) {
    def generator = new ScalaGenerator()
    def reader = new Reader()
    def translator = new Translator(reader, generator)
    translator.translate(deltaFiles)
}

def applyTranslations(scalaTemplateFile, translations) {
    def scalaTemplateContents = scalaTemplateFile.text
    def scalaFileContents = scalaTemplateContents.replaceAll('###EXPANSIONS-CONTRACTIONS###', translations)
    println("SCALA FILE CONTENTS = \n$scalaFileContents")
    scalaFileContents
}

def writeTo(outputFile, contents) {
    println("Writing generated output to $outputFile.name")
    outputFile.withWriter { writer ->
        writer << contents
    }
    println("Completed writing generated output to $outputFile.name")
}

// ---- Script starts here -----
def scalaTemplateURI = 'templates/Transformations.scala.template'
ClassLoader loader = Thread.currentThread().getContextClassLoader()
def scalaTemplateURL = loader.getResource(scalaTemplateURI)
println ("Template URL = $scalaTemplateURL")
if(args.length < 1) {
    println "Usage: Compile <deltas-dir> [scala-template-url=$scalaTemplateURL] <output-uri>"
    return
}

def deltasDir = new File(args[0])
if(!deltasDir.isDirectory()) {
    println("Expected Directory, Given File: ${args[0]}")
    return
}


if(args.length == 2) {
  scalaTemplateURL = args[1]
}

def scalaTemplateFile = new File(scalaTemplateURL.toURI())
def translations = translate(sortedDeltaFiles(deltasDir))
def content = applyTranslations(scalaTemplateFile, translations)
def srcCodeURI = 'generated/scala'
def srcCodeURL = loader.getResource(srcCodeURI)
def generatedScalaFile = new File(srcCodeURL.getPath() + '/Transformations.scala')
writeTo(generatedScalaFile, content)

def classpathURI = '.'
def classpathDir = loader.getResource(classpathURI)
println("classpathDir = $classpathDir")
def binDirURI = "$srcCodeURI/bin"
def binDir = loader.getResource(binDirURI)
println("Compiled Scala dir = $binDir")

