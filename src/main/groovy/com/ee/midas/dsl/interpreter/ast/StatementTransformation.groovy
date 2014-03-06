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

package com.ee.midas.dsl.interpreter.ast

import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
@Slf4j
public class StatementTransformation implements ASTTransformation {
    private def transformations = ['use' : 'using']

    //TODO: Disable closures from within DSL files instead investigate how below can be done
    private def wrapInClosure(BlockStatement blockStatement) {
        def closureExpression = new ClosureExpression(null, blockStatement)
        def expressionStatement = new ExpressionStatement(closureExpression)
        def newBlockStatement = new BlockStatement()
        newBlockStatement.addStatement(expressionStatement)
        newBlockStatement
    }

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        log.info("Source name = ${source.name}")
        log.info("All AST NODES = ${nodes.collect { it.toString() }}")
        ModuleNode ast = source.ast
        def blockStatement = ast.statementBlock
        log.info("All Statements = ${blockStatement.statements}")

        blockStatement.visit(new CodeVisitorSupport() {
            void visitConstantExpression(ConstantExpression expression) {
                def name = expression.value
                if (transformations.containsKey(name)) {
                    def newName = transformations[name]
                    log.debug("AST: Transforming Name => $name -> $newName")
                    expression.value = newName
                } else {
                    log.debug("AST: Skipping Name => $name")
                }
            }

            public void visitArgumentlistExpression(ArgumentListExpression ale) {
                log.debug("AST: Inspecting Arg List for GStrings $ale.expressions")
                def expressions = ale.expressions
                expressions.eachWithIndex { expression, index ->
                    if(expression.getClass() == GStringExpression) {
                        log.debug("AST: Transforming GString => String ($expression.text)")
                        expressions[index] = new ConstantExpression(expression.text)
                    }
                }
                log.debug("AST: Transformed Arg List $ale.expressions")
                super.visitArgumentlistExpression(ale)
            }
        })
    }
}
