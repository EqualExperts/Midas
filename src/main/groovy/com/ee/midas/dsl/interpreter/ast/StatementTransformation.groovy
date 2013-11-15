package com.ee.midas.dsl.interpreter.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
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
        println("Source name = ${source.name}")
        ModuleNode ast = source.ast
        def blockStatement = ast.statementBlock

        blockStatement.visit(new CodeVisitorSupport() {
            void visitConstantExpression(ConstantExpression expression) {
                def name = expression.value
                if (transformations.containsKey(name)) {
                    def newName = transformations[name]
                    println("AST: Transforming Name => $name -> $newName")
                    expression.value = newName
                } else {
                    println("AST: Skipping Name => $name")
                }
            }
        })
    }
}
