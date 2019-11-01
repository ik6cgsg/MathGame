package com.twf.api

import com.twf.config.CompiledConfiguration
import com.twf.expressiontree.*

//expressions
fun stringToExpression(
        string: String,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
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
fun compareWithoutSubstitutions (
        left: ExpressionNode,
        right: ExpressionNode,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
) = compiledConfiguration.factComporator.expressionComporator.compareWithoutSubstitutions(left, right)


//substitutions
fun expressionSubstitutionFromStrings(
        left: String,
        right: String,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
) = ExpressionSubstitution(
        stringToExpression(left, compiledConfiguration),
        stringToExpression(right, compiledConfiguration)
)

fun findSubstitutionPlacesInExpression(
        expression: ExpressionNode,
        substitution: ExpressionSubstitution
) = substitution.findAllPossibleSubstitutionPlaces(expression)

fun applySubstitution(
        expression: ExpressionNode,
        substitution: ExpressionSubstitution,
        substitutionPlaces: List<SubstitutionPlace> //containsPointersOnExpressionPlaces
): ExpressionNode {
    substitution.applySubstitution(substitutionPlaces)
    return expression
}




//string com.twf.api
fun compareWithoutSubstitutions (
        left: String,
        right: String,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
) = compareWithoutSubstitutions(
        stringToExpression(left, compiledConfiguration),
        stringToExpression(right, compiledConfiguration),
        compiledConfiguration
)

data class SubstitutionPlaceOfflineData(
        val parentStartPosition: Int,
        val parentEndPosition: Int,
        val startPosition: Int,
        val endPosition: Int
) {
    fun toJSON () = "{" +
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
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
): String {
    val substitutionPlaces = findSubstitutionPlacesInExpression(
            stringToExpression(expression, compiledConfiguration),
            expressionSubstitutionFromStrings(substitutionLeft, substitutionRight, compiledConfiguration)
    )

    val data = substitutionPlaces.map {
        SubstitutionPlaceOfflineData(
                it.nodeParent.startPosition, it.nodeParent.endPosition,
                it.nodeParent.children[it.nodeChildIndex].startPosition,
                it.nodeParent.children[it.nodeChildIndex].endPosition)
    }.joinToString (separator = ",") { it.toJSON() }

    return "{\"substitutionPlaces\":[$data]}"
}

fun applyExpressionBySubstitutionPlaceCoordinates (
        expression: String,
        substitutionLeft: String,
        substitutionRight: String,
        parentStartPosition: Int,
        parentEndPosition: Int,
        startPosition: Int,
        endPosition: Int,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
):String {
    val actualExpression = stringToExpression(expression, compiledConfiguration)
    val actualSubstitution = expressionSubstitutionFromStrings(substitutionLeft, substitutionRight, compiledConfiguration)
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

    val result = if (actualPlace.isNotEmpty()){
        applySubstitution(actualExpression, actualSubstitution, actualPlace)
    } else {
        actualExpression
    }

    return expressionToString(result)
}