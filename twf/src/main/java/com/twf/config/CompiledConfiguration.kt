package com.twf.config

import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionTreeParser
import com.twf.expressiontree.ExpressionSubstitution
import com.twf.expressiontree.applyAllFunctionSubstitutions
import com.twf.factstransformations.FactComporator
import com.twf.factstransformations.FactSubstitution
import com.twf.factstransformations.parseFromFactIdentifier

class CompiledConfiguration(
        val variableConfiguration: VariableConfiguration = VariableConfiguration(),
        val functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
        val comparisonSettings: ComparisonSettings = ComparisonSettings(),
        val checkedFactAccentuation: CheckedFactAccentuation = CheckedFactAccentuation(),
        val factsLogicConfiguration: FactsLogicConfiguration = FactsLogicConfiguration()) {
    val compiledImmediateVariableReplacements = mapOf<String, String>(*(variableConfiguration.variableImmediateReplacementRules.map { Pair(it.left, it.right) }.toTypedArray()))
    val compiledExpressionTreeTransformationRules = mutableListOf<ExpressionSubstitution>()
    val compiledFactTreeTransformationRules = mutableListOf<FactSubstitution>()
    val compiledImmediateTreeTransformationRules = mutableListOf<ExpressionSubstitution>()
    val compiledFunctionDefinitions = mutableListOf<ExpressionSubstitution>()
    val definedFunctionNameNumberOfArgsSet = mutableSetOf<String>()
    val noTransformationDefinedFunctionNameNumberOfArgsSet = mutableSetOf<String>()
    var configurationErrors = mutableListOf<ConfigurationError>()

    val factComporator: FactComporator

    fun parseStringExpression(expression: String, nameForRuleDesignationsPossible: Boolean = false): ExpressionNode? {
        val expressionTreeParser = ExpressionTreeParser(expression, nameForRuleDesignationsPossible, functionConfiguration, compiledImmediateVariableReplacements)
        val error = expressionTreeParser.parse()
        if (error != null) {
            configurationErrors.add(ConfigurationError(error.description, "TreeTransformationRule", expression, error.position))
            return null
        } else {
            return expressionTreeParser.root
        }
    }


    init {
        factComporator = FactComporator()
        factComporator.init(this)

        functionConfiguration.notChangesOnVariablesInComparisonFunction
                .forEach { definedFunctionNameNumberOfArgsSet.add(it.getIdentifier()) }

        functionConfiguration.notChangesOnVariablesInComparisonFunctionWithoutTransformations
                .forEach { noTransformationDefinedFunctionNameNumberOfArgsSet.add(it.getIdentifier()) }

        val compiledSubstitutions = mutableMapOf<String, ExpressionSubstitution>()
        for (functionDefinition in functionConfiguration.functionDefinitions) {
            val leftTree = parseStringExpression(functionDefinition.definitionLeftExpression, true)
            val rightTree = parseStringExpression(functionDefinition.definitionRightExpression, true)
            if (leftTree != null && rightTree != null) {
                if (leftTree.children.isEmpty() || rightTree.children.isEmpty()) {
                    configurationErrors.add(ConfigurationError("function definition rule is empty", "TreeTransformationRule",
                            "values: '" + functionDefinition.definitionLeftExpression + "' and '" + functionDefinition.definitionRightExpression + "'", -1))
                } else {
                    leftTree.variableReplacement(compiledImmediateVariableReplacements)
                    rightTree.variableReplacement(compiledImmediateVariableReplacements)
                    rightTree.applyAllFunctionSubstitutions(compiledSubstitutions)
                    val newSubstitution = ExpressionSubstitution(leftTree, rightTree)
                    compiledFunctionDefinitions.add(newSubstitution)
                    val definitionIdentifier = leftTree.children[0].value + "_" + leftTree.children[0].children.size
                    compiledSubstitutions.put(definitionIdentifier, newSubstitution)
                }
            }
        }

        for (treeTransformationRule in functionConfiguration.treeTransformationRules) {
            val leftTree = parseStringExpression(treeTransformationRule.definitionLeftExpression, true)
            val rightTree = parseStringExpression(treeTransformationRule.definitionRightExpression, true)
            if (leftTree != null && rightTree != null) {
                leftTree.variableReplacement(compiledImmediateVariableReplacements)
                rightTree.variableReplacement(compiledImmediateVariableReplacements)
                val newSubstitution = ExpressionSubstitution(leftTree, rightTree, treeTransformationRule.weight)
                if (treeTransformationRule.isImmediate) compiledImmediateTreeTransformationRules.add(newSubstitution)
                else compiledExpressionTreeTransformationRules.add(newSubstitution)
            }
        }

        for (treeTransformationRule in functionConfiguration.taskContextTreeTransformationRules) {
            val leftTree = parseStringExpression(treeTransformationRule.definitionLeftExpression, true)
            val rightTree = parseStringExpression(treeTransformationRule.definitionRightExpression, true)
            if (leftTree != null && rightTree != null) {
                leftTree.variableReplacement(compiledImmediateVariableReplacements)
                rightTree.variableReplacement(compiledImmediateVariableReplacements)
                val newSubstitution = ExpressionSubstitution(leftTree, rightTree, treeTransformationRule.weight, basedOnTaskContext = true)
                if (treeTransformationRule.isImmediate) compiledImmediateTreeTransformationRules.add(newSubstitution)
                else compiledExpressionTreeTransformationRules.add(newSubstitution)
            }
        }

        for (factTransformation in factsLogicConfiguration.factsTransformationRules) {
            val leftTree = parseFromFactIdentifier(factTransformation.definitionLeftFactTree)
            val rightTree = parseFromFactIdentifier(factTransformation.definitionRightFactTree)
            if (leftTree != null && rightTree != null) {
                leftTree.variableReplacement(compiledImmediateVariableReplacements)
                rightTree.variableReplacement(compiledImmediateVariableReplacements)
                compiledFactTreeTransformationRules.add(
                        FactSubstitution(leftTree, rightTree, factTransformation.weight, direction = factTransformation.direction, factComporator = factComporator)
                )
                if (!factTransformation.isOneDirection) {
                    compiledFactTreeTransformationRules.add(
                            FactSubstitution(rightTree, leftTree, factTransformation.weight, direction = factTransformation.direction, factComporator = factComporator)
                    )
                }
            }
        }
    }
}