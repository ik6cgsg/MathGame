package com.twf.api

import com.twf.config.CompiledConfiguration
import com.twf.config.FunctionConfiguration
import com.twf.expressiontree.*

//expressions
fun stringToExpression(
        string: String,
        scope: String = "",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): ExpressionNode {
    val expressionTreeParser = ExpressionTreeParser(string,
            functionConfiguration = compiledConfiguration.functionConfiguration,
            compiledImmediateVariableReplacements = compiledConfiguration.compiledImmediateVariableReplacements)
    expressionTreeParser.parse()
    return expressionTreeParser.root
}

fun structureStringToExpression(
        structureString: String
): ExpressionNode {
    val expressionNodeConstructor = ExpressionNodeConstructor()
    return expressionNodeConstructor.construct(structureString)
}

fun expressionToStructureString(
        expressionNode: ExpressionNode
) = expressionNode.toString()

fun expressionToString(
        expressionNode: ExpressionNode
) = expressionNode.toUserView()


//compare expressions without substitutions
fun compareWithoutSubstitutions(
        left: ExpressionNode,
        right: ExpressionNode,
        scope: Set<String> = setOf(""),
        notChangesOnVariablesFunction: Set<String> = setOf("+", "-", "*", "/", "^"),
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(scope, notChangesOnVariablesFunction),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compiledConfiguration.factComporator.expressionComporator.compareWithoutSubstitutions(left, right)


//substitutions
fun expressionSubstitutionFromStrings(
        left: String,
        right: String,
        scope: String = "",
        basedOnTaskContext: Boolean = false,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = ExpressionSubstitution(
        stringToExpression(left, compiledConfiguration = compiledConfiguration),
        stringToExpression(right, compiledConfiguration = compiledConfiguration),
        basedOnTaskContext = basedOnTaskContext
)

fun findSubstitutionPlacesInExpression(
        expression: ExpressionNode,
        substitution: ExpressionSubstitution
): MutableList<SubstitutionPlace> {
    if (substitution.leftFunctions.isNotEmpty() && substitution.leftFunctions.intersect(expression.getContainedFunctions()).isEmpty() &&
            substitution.left.getContainedVariables().intersect(expression.getContainedVariables()).isEmpty()) {
        return mutableListOf()
    }
    return substitution.findAllPossibleSubstitutionPlaces(expression)
}

fun applySubstitution(
        expression: ExpressionNode,
        substitution: ExpressionSubstitution,
        substitutionPlaces: List<SubstitutionPlace> //containsPointersOnExpressionPlaces
): ExpressionNode {
    substitution.applySubstitution(substitutionPlaces)
    return expression
}

fun generateTask(
        expressionSubstitutions: List<ExpressionSubstitution>, //if substitution can be applied in both directions, it has to be specified twice
        stepsCount: Int,
        originalExpressions: List<ExpressionNode> //it's better to set original expression manually, to choose correct number of variables
) = generateExpressionTask(expressionSubstitutions, stepsCount, originalExpressions)


//string api
fun compareWithoutSubstitutions(
        left: String,
        right: String,
        scope: String = "",
        notChangesOnVariablesFunction: String = "+;-;*;/;^",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
                notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compareWithoutSubstitutions(
        stringToExpression(left, compiledConfiguration = compiledConfiguration),
        stringToExpression(right, compiledConfiguration = compiledConfiguration),
        compiledConfiguration = compiledConfiguration
)

data class SubstitutionPlaceOfflineData(
        val parentStartPosition: Int,
        val parentEndPosition: Int,
        val startPosition: Int,
        val endPosition: Int
) {
    fun toJSON() = "{" +
            "\"parentStartPosition\":\"$parentStartPosition\"," +
            "\"parentEndPosition\":\"$parentEndPosition\"," +
            "\"startPosition\":\"$startPosition\"," +
            "\"endPosition\":\"$endPosition\"" +
            "}"
}

fun findSubstitutionPlacesCoordinatesInExpressionJSON(
        expression: String,
        substitutionLeft: String,
        substitutionRight: String,
        scope: String = "",
        basedOnTaskContext: Boolean = false,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val substitutionPlaces = findSubstitutionPlacesInExpression(
            stringToExpression(expression, compiledConfiguration = compiledConfiguration),
            expressionSubstitutionFromStrings(substitutionLeft, substitutionRight,
                    basedOnTaskContext = basedOnTaskContext, compiledConfiguration = compiledConfiguration)
    )

    val data = substitutionPlaces.map {
        SubstitutionPlaceOfflineData(
                it.nodeParent.startPosition, it.nodeParent.endPosition,
                it.nodeParent.children[it.nodeChildIndex].startPosition,
                it.nodeParent.children[it.nodeChildIndex].endPosition)
    }.joinToString(separator = ",") { it.toJSON() }

    return "{\"substitutionPlaces\":[$data]}"
}

fun applyExpressionBySubstitutionPlaceCoordinates(
        expression: String,
        substitutionLeft: String,
        substitutionRight: String,
        parentStartPosition: Int,
        parentEndPosition: Int,
        startPosition: Int,
        endPosition: Int,
        scope: String = "",
        basedOnTaskContext: Boolean = false,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val actualExpression = stringToExpression(expression, compiledConfiguration = compiledConfiguration)
    val actualSubstitution = expressionSubstitutionFromStrings(substitutionLeft, substitutionRight,
            basedOnTaskContext = basedOnTaskContext, compiledConfiguration = compiledConfiguration)
    val substitutionPlaces = findSubstitutionPlacesInExpression(
            actualExpression,
            actualSubstitution
    )

    val actualPlace = substitutionPlaces.filter {
        it.nodeParent.startPosition == parentStartPosition &&
                it.nodeParent.endPosition == parentEndPosition &&
                it.nodeParent.children[it.nodeChildIndex].startPosition == startPosition &&
                it.nodeParent.children[it.nodeChildIndex].endPosition == endPosition
    }

    val result = if (actualPlace.isNotEmpty()) {
        applySubstitution(actualExpression, actualSubstitution, actualPlace)
    } else {
        actualExpression
    }

    return expressionToString(result)
}

fun generateTaskInJSON(
        expressionSubstitutions: String, //';' separated equalities
        stepsCount: Int,
        originalExpressions: String, //';' separated
        scope: String = "", //';' separated
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val expressionTask = generateTask(
            expressionSubstitutions.split(";").map {
                val parts = it.split("=")
                expressionSubstitutionFromStrings(parts.first(), parts.last(), compiledConfiguration = compiledConfiguration)
            },
            stepsCount,
            originalExpressions.split(";").map { stringToExpression(it, compiledConfiguration = compiledConfiguration) }
    )
    return "{" +
            "\"originalExpression\":\"${expressionToString(expressionTask.originalExpression)}\"," +
            "\"finalExpression\":\"${expressionToString(expressionTask.finalExpression)}\"," +
            "\"requiredSubstitutions\":[${
            expressionTask.requiredSubstitutions.joinToString (separator = ",") { "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(it.right)}\"}" }
            }]," +
            "\"allSubstitutions\":[${
            expressionTask.allSubstitutions.joinToString (separator = ",") { "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(it.right)}\"}" }
            }]" +
            "}"
}