package com.ee.midas.dsl

import com.ee.midas.dsl.ast.StatementTransformation
import com.ee.midas.dsl.generator.ScalaGenerator
import com.ee.midas.dsl.interpreter.Parser
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

def createNewShell() {
    def secureCustomizer = new SecureASTCustomizer()
    secureCustomizer.with {
//    closuresAllowed = false
        methodDefinitionAllowed = false // user will not be able to define methods
        importsWhitelist = [] // empty whitelist means imports are disallowed
        staticImportsWhitelist = [] // same for static imports
        staticStarImportsWhitelist = []

        // method calls are only allowed if the receiver is of one of those types
        // be careful, it's not a runtime type!
//    receiversClassesWhiteList = [
//            interpreter.Parser,
//            interpreter.representation.Databases,
//            interpreter.representation.Collection,
//            interpreter.representation.Database
//            Math,
//            Integer,
//            Float,
//            Double,
//            Long,
//            BigDecimal
//    ].asImmutable()
    }

    def astTransformationCustomizer = new ASTTransformationCustomizer(new StatementTransformation())
    def configuration = new CompilerConfiguration()
    configuration.addCompilationCustomizers(secureCustomizer, astTransformationCustomizer)
    new GroovyShell(configuration)
}

if(!args) {
    println "Usage: DSLRunner <deltas directory>"
    return
}

def dslDirname = new File(args[0])
if(!dslDirname.isDirectory()) {
    println("Expected Directory, given Filename ${args[0]}")
    return
}

def dslFiles = dslDirname.listFiles(new FilenameFilter() {
    @Override
    boolean accept(File dir, String name) {
        name.endsWith('.delta')
    }
}).sort()
println("Sorted Delta Files = $dslFiles")

def parser = new Parser()

dslFiles.each { dslFile ->
    def dslFileName = dslFile.name
    def dsl = dslFile.text
    def code = """{-> $dsl}"""
    try {
        def shell = createNewShell()
        def delta = shell.evaluate(code, dslFileName)
        parser.parse(delta)
    } catch (Throwable problem) {
        println "Could not parse $dslFileName : $problem.message"
    }
}

def deltas = parser.ast()
def generator = new ScalaGenerator()
println generator.generate(deltas)


