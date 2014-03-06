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
            def deltaFileAbsoluteName = deltaFile.absolutePath
            log.info("Reading $deltaFileAbsoluteName")
            def dsl = deltaFile.text
            def code = """{-> $dsl}"""
            //shell evaluates once, hence create new each time
            def shell = createNewShell()
            def delta = shell.evaluate(code, deltaFileAbsoluteName)
            try {
                use (FileExtension) {
                    parser.parse(deltaFile.changeSet(), delta)
                }
            } catch (Throwable t) {
                throw new InvalidGrammar("$deltaFileAbsoluteName --> ${t.message}")
            }
            //http://groovy.dzone.com/news/groovyshell-and-memory-leaks
            shell = null
        }
        parser.ast()
    }

}