package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.ast.StatementTransformation
import com.ee.midas.dsl.interpreter.representation.Tree
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

public class Reader {
    public Reader() {
    }

    private GroovyShell createNewShell() {
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
//            interpreter.representation.Tree,
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

    public Tree read(final List<File> deltaFiles) {
        def parser = new Parser()
        deltaFiles.each { deltaFile ->
            def deltaFileName = deltaFile.name
            def dsl = deltaFile.text
            def code = """{-> $dsl}"""
            //shell evaluates once, hence create new each time
            def shell = createNewShell()
            def delta = shell.evaluate(code, deltaFileName)
            parser.parse(delta)
        }
        parser.ast()
    }
}