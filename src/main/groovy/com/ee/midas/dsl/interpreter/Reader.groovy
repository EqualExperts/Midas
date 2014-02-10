package com.ee.midas.dsl.interpreter

import com.ee.midas.dsl.interpreter.ast.StatementTransformation
import com.ee.midas.dsl.interpreter.representation.InvalidGrammar
import com.ee.midas.dsl.interpreter.representation.Tree
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

@Slf4j
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
            def deltaFileName = deltaFile.absolutePath
            log.info("Reading $deltaFileName")
            def dsl = deltaFile.text
            def code = """{-> $dsl}"""
            //shell evaluates once, hence create new each time
            def shell = createNewShell()
            def delta = shell.evaluate(code, deltaFileName)
            try {
                parser.parse(delta)
            } catch (Throwable t) {
                throw new InvalidGrammar("$deltaFileName --> ${t.message}")
            }
            //http://groovy.dzone.com/news/groovyshell-and-memory-leaks
            shell = null
        }
        parser.ast()
    }
}