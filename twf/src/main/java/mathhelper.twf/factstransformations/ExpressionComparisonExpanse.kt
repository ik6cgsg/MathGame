package mathhelper.twf.factstransformations

import mathhelper.twf.config.ComparisonType
import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.config.reverse
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.NodeType


class ExpressionComparisonExpanse (val compiledConfiguration: CompiledConfiguration) {

    fun expanseGenerator(expressionComparison: ExpressionComparison, result: MutableList<ExpressionComparison>,
                         onlyExpressPairs: Boolean) { //assumes that trees are not empty
        expanseGeneratorRecursive(expressionComparison.leftExpression.data, expressionComparison.rightExpression.data, result, onlyExpressPairs, expressionComparison.comparisonType)
        expanseGeneratorRecursive(expressionComparison.rightExpression.data, expressionComparison.leftExpression.data, result, onlyExpressPairs, expressionComparison.comparisonType)
    }


    private fun expanseGeneratorRecursive(l: ExpressionNode, r: ExpressionNode, result: MutableList<ExpressionComparison>,
                                          onlyExpressPairs: Boolean, comparisonType: ComparisonType) {
        if (l.value == "" && l.children.size == 1) {
            return expanseGeneratorRecursive(l.children.first(), r, result, onlyExpressPairs, comparisonType)
        } else if (r.value == "" && r.children.size == 1) {
            return expanseGeneratorRecursive(l, r.children.first(), result, onlyExpressPairs, comparisonType)
        }
        if (l.nodeType == NodeType.VARIABLE) {
            if (!onlyExpressPairs ||
                    (!l.isNumberValue() && !r.containsVariables(setOf(l.value)))) {
                result.add(ExpressionComparison(leftExpression = Expression(data = l), rightExpression = Expression(data = r), comparisonType = comparisonType))
            }
        } else {
            if (l.children.size == 1) {
                if (l.value == "+" && l.children.first().value == "-" && l.children.first().children.size == 1) {
                    val newL = l.children.first().children.first().clone()
                    val newR = compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
                        addChild(compiledConfiguration.createExpressionFunctionNode("-", -1)).apply {
                            addChild(r.clone())
                        }
                    }
                    expanseGeneratorRecursive(newL, newR, result, onlyExpressPairs, comparisonType.reverse())
                }
                val newL = l.children.first().clone()
                val newR = r
            } else if (l.value == "+" || l.value == "*") {
                for (i in 0..l.children.lastIndex) {
                    val newL = l.copy().apply {
                        for (j in 0..l.children.lastIndex) {
                            if (j != i) {
                                addChild(l.children[j].clone())
                            }
                        }
                    }
                    val newR = if (l.value == "+") {
                        compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
                            addChild(r.clone())
                            if (l.children[i].value == "-") {
                                addChild(l.children[i].clone())
                            } else {
                                addChild(compiledConfiguration.createExpressionFunctionNode("-", -1, listOf(l.children[i].clone())))
                            }
                        }
                    } else {
                        compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                            addChild(r.clone())
                            addChild(l.children[i].clone())
                        }
                    }
                    if (newL.children.size == 1 && newL.children.first().value != "-") {
                        expanseGeneratorRecursive(newL.children.first(), newR, result, onlyExpressPairs, comparisonType)
                    } else {
                        expanseGeneratorRecursive(newL, newR, result, onlyExpressPairs, comparisonType)
                    }
                }
            } else if (l.value == "/" && l.children.size == 2) {
                // 1. a/b = r -> a = r*b
                val newLA = l.children[0].clone()
                val newRA = compiledConfiguration.createExpressionFunctionNode("*", -1).apply {
                    addChild(r.clone())
                    addChild(l.children[1].clone())
                }
                expanseGeneratorRecursive(newLA, newRA, result, onlyExpressPairs, comparisonType)

                // 2. a/b = r -> b = a/r
                val newLB = l.children[1].clone()
                val newRB = compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                    addChild(l.children[0].clone())
                    addChild(r.clone())
                }
                expanseGeneratorRecursive(newLB, newRB, result, onlyExpressPairs, comparisonType.reverse())
            }
        }
    }
}