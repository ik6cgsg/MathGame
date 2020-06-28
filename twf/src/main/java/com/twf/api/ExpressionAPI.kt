package com.twf.api

import com.twf.config.CompiledConfiguration
import com.twf.config.FunctionConfiguration
import com.twf.expressiontree.*
import com.twf.numbers.toReal
import com.twf.platformdependent.abs
import com.twf.platformdependent.escapeCharacters
import kotlin.math.pow

//expressions
fun stringToExpression(
    string: String,
    scope: String = "",
    isMathMl: Boolean = false,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): ExpressionNode {
    val expressionTreeParser = ExpressionTreeParser(
        string,
        functionConfiguration = compiledConfiguration.functionConfiguration,
        compiledImmediateVariableReplacements = compiledConfiguration.compiledImmediateVariableReplacements,
        isMathML = isMathMl
    )
    expressionTreeParser.parse()
    return expressionTreeParser.root
}

fun stringToStructureString(
    string: String,
    scope: String = "",
    isMathMl: Boolean = false,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = expressionToStructureString(
    stringToExpression(
        string,
        scope,
        isMathMl,
        functionConfiguration,
        compiledConfiguration
    )
)

fun structureStringToExpression(
    structureString: String,
    scope: String = "",
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    )
): ExpressionNode {
    val expressionNodeConstructor = ExpressionNodeConstructor(functionConfiguration)
    val result = expressionNodeConstructor.construct(structureString)
    result.computeNodeIdsAsNumbersInDirectTraversal()
    result.computeIdentifier()
    return result
}

fun expressionToStructureString(
    expressionNode: ExpressionNode
) = expressionNode.toString()

fun expressionToString(
    expressionNode: ExpressionNode,
    characterEscapingDepth: Int = 1
) = escapeCharacters(expressionNode.toUserView(), characterEscapingDepth)


//compare expressions without substitutions
fun compareWithoutSubstitutions(
    left: ExpressionNode,
    right: ExpressionNode,
    scope: Set<String> = setOf(""),
    notChangesOnVariablesFunction: Set<String> = setOf("+", "-", "*", "/", "^"),
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(scope, notChangesOnVariablesFunction),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compiledConfiguration.factComporator.expressionComporator.compareWithoutSubstitutions(left, right)


//compare expression by pattern
fun compareByPattern(
    expression: ExpressionNode,
    pattern: ExpressionStructureConditionNode
) = checkExpressionStructure(expression, pattern)


fun stringToExpressionStructurePattern(
    string: String,
    scope: String = "",
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    )
): ExpressionStructureConditionNode {
    val expressionStructureConditionConstructor = ExpressionStructureConditionConstructor()
    return expressionStructureConditionConstructor.parse(string)
}


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

//substitutions
fun expressionSubstitutionFromStructureStrings(
    leftStructureString: String,
    rightStructureString: String,
    basedOnTaskContext: Boolean = false
) = ExpressionSubstitution(
    structureStringToExpression(leftStructureString),
    structureStringToExpression(rightStructureString),
    basedOnTaskContext = basedOnTaskContext
)

fun findSubstitutionPlacesInExpression(
    expression: ExpressionNode,
    substitution: ExpressionSubstitution
): MutableList<SubstitutionPlace> {
    if (substitution.leftFunctions.isNotEmpty() && substitution.leftFunctions.intersect(expression.getContainedFunctions())
            .isEmpty() &&
        substitution.left.getContainedVariables().intersect(expression.getContainedVariables()).isEmpty()
    ) {
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
    expression.getTopNode().reduceExtraSigns(setOf("+"), setOf("-"))
    expression.getTopNode().normilizeSubstructions(FunctionConfiguration())
    expression.getTopNode().computeNodeIdsAsNumbersInDirectTraversal()
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
        notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
            .filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compareWithoutSubstitutions(
    stringToExpression(left, compiledConfiguration = compiledConfiguration),
    stringToExpression(right, compiledConfiguration = compiledConfiguration),
    compiledConfiguration = compiledConfiguration
)

fun compareByPattern(
    expression: String,
    pattern: String,
    scope: String = "",
    notChangesOnVariablesFunction: String = "+;-;*;/;^",
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
        notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
            .filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compareByPattern(
    stringToExpression(expression, compiledConfiguration = compiledConfiguration),
    stringToExpressionStructurePattern(pattern, functionConfiguration = functionConfiguration)
)

fun compareWithoutSubstitutionsStructureStrings(
    leftStructureString: String,
    rightStructureString: String,
    scope: String = "",
    notChangesOnVariablesFunction: String = "+;-;*;/;^",
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
        notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
            .filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compareWithoutSubstitutions(
    structureStringToExpression(leftStructureString),
    structureStringToExpression(rightStructureString),
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
        expressionSubstitutionFromStrings(
            substitutionLeft, substitutionRight,
            basedOnTaskContext = basedOnTaskContext, compiledConfiguration = compiledConfiguration
        )
    )

    val data = substitutionPlaces.map {
        SubstitutionPlaceOfflineData(
            it.nodeParent.startPosition, it.nodeParent.endPosition,
            it.nodeParent.children[it.nodeChildIndex].startPosition,
            it.nodeParent.children[it.nodeChildIndex].endPosition
        )
    }.joinToString(separator = ",") { it.toJSON() }

    return "{\"substitutionPlaces\":[$data]}"
}

fun findStructureStringsSubstitutionPlacesCoordinatesInExpressionJSON(
    expression: String,
    substitutionLeftStructureString: String,
    substitutionRightStructureString: String,
    scope: String = "",
    basedOnTaskContext: Boolean = false,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val substitutionPlaces = findSubstitutionPlacesInExpression(
        stringToExpression(expression, compiledConfiguration = compiledConfiguration),
        expressionSubstitutionFromStructureStrings(
            substitutionLeftStructureString, substitutionRightStructureString,
            basedOnTaskContext = basedOnTaskContext
        )
    )

    val data = substitutionPlaces.map {
        SubstitutionPlaceOfflineData(
            it.nodeParent.startPosition, it.nodeParent.endPosition,
            it.nodeParent.children[it.nodeChildIndex].startPosition,
            it.nodeParent.children[it.nodeChildIndex].endPosition
        )
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
    characterEscapingDepth: Int = 1,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val actualExpression = stringToExpression(expression, compiledConfiguration = compiledConfiguration)
    val actualSubstitution = expressionSubstitutionFromStrings(
        substitutionLeft, substitutionRight,
        basedOnTaskContext = basedOnTaskContext, compiledConfiguration = compiledConfiguration
    )
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

    return escapeCharacters(expressionToString(result), characterEscapingDepth)
}

fun applyExpressionByStructureStringsSubstitutionPlaceCoordinates(
    expression: String,
    substitutionLeftStructureString: String,
    substitutionRightStructureString: String,
    parentStartPosition: Int,
    parentEndPosition: Int,
    startPosition: Int,
    endPosition: Int,
    scope: String = "",
    basedOnTaskContext: Boolean = false,
    characterEscapingDepth: Int = 1,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val actualExpression = stringToExpression(expression, compiledConfiguration = compiledConfiguration)
    val actualSubstitution = expressionSubstitutionFromStructureStrings(
        substitutionLeftStructureString, substitutionRightStructureString,
        basedOnTaskContext = basedOnTaskContext
    )
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

    return escapeCharacters(expressionToString(result), characterEscapingDepth)
}

fun generateTaskInJSON(
    expressionSubstitutions: String, //';' separated equalities
    stepsCount: Int,
    originalExpressions: String, //';' separated
    scope: String = "", //';' separated
    characterEscapingDepth: Int = 1,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val expressionTask = generateTask(
        expressionSubstitutions.split(";").map {
            val parts = it.split("=")
            expressionSubstitutionFromStrings(
                parts.first(),
                parts.last(),
                compiledConfiguration = compiledConfiguration
            )
        },
        stepsCount,
        originalExpressions.split(";").map { stringToExpression(it, compiledConfiguration = compiledConfiguration) }
    )
    return escapeCharacters("{" +
        "\"originalExpression\":\"${expressionToString(expressionTask.originalExpression)}\"," +
        "\"finalExpression\":\"${expressionToString(expressionTask.finalExpression)}\"," +
        "\"requiredSubstitutions\":[${
        expressionTask.requiredSubstitutions.joinToString(separator = ",") {
            "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
                it.right
            )}\"}"
        }
        }]," +
        "\"allSubstitutions\":[${
        expressionTask.allSubstitutions.joinToString(separator = ",") {
            "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
                it.right
            )}\"}"
        }
        }]" +
        "}",
        characterEscapingDepth)
}

fun generateTaskByStructureStringsSubstitutionsInJSON(
    expressionSubstitutions: String, //';' separated equalities
    stepsCount: Int,
    originalExpressions: String, //';' separated
    scope: String = "", //';' separated
    characterEscapingDepth: Int = 1,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val expressionTask = generateTask(
        expressionSubstitutions.split(";").map {
            val parts = it.split("=")
            expressionSubstitutionFromStructureStrings(parts.first(), parts.last())
        },
        stepsCount,
        originalExpressions.split(";").map { stringToExpression(it, compiledConfiguration = compiledConfiguration) }
    )
    return escapeCharacters("{" +
        "\"originalExpression\":\"${expressionToString(expressionTask.originalExpression)}\"," +
        "\"finalExpression\":\"${expressionToString(expressionTask.finalExpression)}\"," +
        "\"requiredSubstitutions\":[${
        expressionTask.requiredSubstitutions.joinToString(separator = ",") {
            "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
                it.right
            )}\"}"
        }
        }]," +
        "\"allSubstitutions\":[${
        expressionTask.allSubstitutions.joinToString(separator = ",") {
            "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
                it.right
            )}\"}"
        }
        }]" +
        "}",
        characterEscapingDepth)
}


data class SimpleComputationRuleParams(
    val isIncluded: Boolean,
    val operationsMap: Map<String, (List<Double>) -> Double?> = mapOf(
        "+" to {args -> plus(args)},
        "-" to {args -> minus(args)},
        "*" to {args -> mul(args)},
        "/" to {args -> div(args)},
        "^" to {args -> pow(args)},
        "log" to {args -> log(args)}
    )
)

fun optGenerateSimpleComputationRule(
    expressionPart: ExpressionNode,
    simpleComputationRuleParams: SimpleComputationRuleParams
): MutableList<ExpressionSubstitution> {
    val result = mutableListOf<ExpressionSubstitution>()
    if (expressionPart.children.isNotEmpty() && simpleComputationRuleParams.isIncluded
        && expressionPart.getContainedFunctions().subtract(simpleComputationRuleParams.operationsMap.keys).isEmpty()
        && expressionPart.getContainedVariables().isEmpty()
    ) {
        val computed = expressionPart.computeNode(simpleComputationRuleParams) ?: return result
        var stringValue = computed.toString()
        if (stringValue.contains('.')){
            val fractionPart = stringValue.substringAfter(".").substringBefore("000")
            stringValue = stringValue.substringBefore(".")
            if (fractionPart.isNotEmpty() && fractionPart !="0")
            stringValue = stringValue + "." + stringValue.substringAfter(".").substringBefore("000")
        }
        result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(ExpressionNode(NodeType.VARIABLE, stringValue))))
    }
    return result
}

private fun addRootNodeToExpression(expression: ExpressionNode) : ExpressionNode {
    val root = ExpressionNode(NodeType.FUNCTION, "")
    root.addChild(expression)
    root.computeIdentifier()
    return root
}

private fun ExpressionNode.computeNode (simpleComputationRuleParams: SimpleComputationRuleParams): Double? {
    if (nodeType == NodeType.VARIABLE) {
        return value.toDoubleOrNull()
    } else if (children.isEmpty()) {
        return null
    }

    val listOfArgs = mutableListOf<Double>()
    for (childNode in children) {
        childNode.computeNode(simpleComputationRuleParams)?.let { listOfArgs.add(it) } ?: return null
    }

    return simpleComputationRuleParams.operationsMap[value]?.invoke(listOfArgs)
}

private fun inZ (value: Double) = (value.toInt() - value).toReal().additivelyEqualToZero()

private fun roundNumber (number: Double): Double {
    var current = abs(number)
    while (!current.toReal().additivelyEqualToZero() and inZ(current)){
        current /= 10
    }
    while (!inZ(current)){
        current *= 10
    }
    return current
}

private fun plus (args: List<Double>): Double? {
    if (args.any { roundNumber(it) > 100 }) {
        return null
    }
    val result = args.sum()
    if (roundNumber(result) > 200) {
        return null
    }
    return result
}

private fun minus (args: List<Double>): Double? {
    if (args.size != 1) {
        return null
    }
    return -args.first()
}

private fun mul (args: List<Double>): Double? {
    if (args.any { roundNumber(it) > 10 }) {
        return null
    }
    var result = 1.0
    for (arg in args) {
        result *= arg
    }
    if (roundNumber(result) > 100) {
        return null
    }
    return result
}

private fun div (args: List<Double>): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > 100 || roundNumber(args.last()) > 10 || args.last() == 0.0) {
        return null
    }
    val result = args.first() / args.last()
    if (roundNumber(result) > 100) {
        return null
    }
    return result
}

private fun pow (args: List<Double>): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > 400 || roundNumber(args.last()) > 10) {
        return null
    }
    val result = args.first().pow(args.last())
    if (!result.isFinite() || roundNumber(result) > 400) {
        return null
    }
    return result
}

private fun log (args: List<Double>): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > 400 || roundNumber(args.last()) > 400) {
        return null
    }
    val result = kotlin.math.log(args.first(), args.last())
    if (!result.isFinite() || roundNumber(result) > 400) {
        return null
    }
    return result
}

