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
