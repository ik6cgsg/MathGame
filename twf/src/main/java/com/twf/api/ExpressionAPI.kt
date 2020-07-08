package com.twf.api

import com.twf.config.CompiledConfiguration
import com.twf.config.FunctionConfiguration
import com.twf.expressiontree.*
import com.twf.numbers.toReal
import com.twf.platformdependent.abs
import com.twf.platformdependent.escapeCharacters
import com.twf.platformdependent.toShortString
import kotlin.math.pow
import kotlin.math.sqrt

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
    matchJumbledAndNested: Boolean = false,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = ExpressionSubstitution(
    stringToExpression(left, compiledConfiguration = compiledConfiguration),
    stringToExpression(right, compiledConfiguration = compiledConfiguration),
    basedOnTaskContext = basedOnTaskContext,
    matchJumbledAndNested = matchJumbledAndNested
)

//substitutions
fun expressionSubstitutionFromStructureStrings(
    leftStructureString: String,
    rightStructureString: String,
    basedOnTaskContext: Boolean = false,
    matchJumbledAndNested: Boolean = false
) = ExpressionSubstitution(
    structureStringToExpression(leftStructureString),
    structureStringToExpression(rightStructureString),
    basedOnTaskContext = basedOnTaskContext,
    matchJumbledAndNested = matchJumbledAndNested
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
    var expr = expression
    var result = substitution.findAllPossibleSubstitutionPlaces(expression)
    if (result.isEmpty() && substitution.matchJumbledAndNested && expression.containsNestedSameFunctions()){
        expr = expression.cloneWithExpandingNestedSameFunctions()
        result = substitution.findAllPossibleSubstitutionPlaces(expr)
    }
    if (result.isEmpty()){
        expr = expr.cloneAndSimplifyByComputeSimplePlaces()
        result = substitution.findAllPossibleSubstitutionPlaces(expr)
    }
    return result
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
    matchJumbledAndNested: Boolean = false,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val substitutionPlaces = findSubstitutionPlacesInExpression(
        stringToExpression(expression, compiledConfiguration = compiledConfiguration),
        expressionSubstitutionFromStrings(
            substitutionLeft, substitutionRight,
            basedOnTaskContext = basedOnTaskContext, compiledConfiguration = compiledConfiguration, matchJumbledAndNested = matchJumbledAndNested
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
    matchJumbledAndNested: Boolean = false,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val substitutionPlaces = findSubstitutionPlacesInExpression(
        stringToExpression(expression, compiledConfiguration = compiledConfiguration),
        expressionSubstitutionFromStructureStrings(
            substitutionLeftStructureString, substitutionRightStructureString,
            basedOnTaskContext = basedOnTaskContext, matchJumbledAndNested = matchJumbledAndNested
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
    matchJumbledAndNested: Boolean = false,
    characterEscapingDepth: Int = 1,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val actualExpression = stringToExpression(expression, compiledConfiguration = compiledConfiguration)
    val actualSubstitution = expressionSubstitutionFromStrings(
        substitutionLeft, substitutionRight,
        basedOnTaskContext = basedOnTaskContext, compiledConfiguration = compiledConfiguration, matchJumbledAndNested = matchJumbledAndNested
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
    matchJumbledAndNested: Boolean = false,
    characterEscapingDepth: Int = 1,
    functionConfiguration: FunctionConfiguration = FunctionConfiguration(
        scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
    ),
    compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): String {
    val actualExpression = stringToExpression(expression, compiledConfiguration = compiledConfiguration)
    val actualSubstitution = expressionSubstitutionFromStructureStrings(
        substitutionLeftStructureString, substitutionRightStructureString,
        basedOnTaskContext = basedOnTaskContext,
        matchJumbledAndNested = matchJumbledAndNested
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


fun optGenerateSimpleComputationRule(
    expressionPartOriginal: ExpressionNode,
    simpleComputationRuleParams: SimpleComputationRuleParams
): MutableList<ExpressionSubstitution> {
    val result = mutableListOf<ExpressionSubstitution>()
    if (!simpleComputationRuleParams.isIncluded || expressionPartOriginal.calcComplexity() > 4 || expressionPartOriginal.getContainedFunctions().subtract(simpleComputationRuleParams.operationsMap.keys).isNotEmpty()){
        return result
    }
    val expressionPart = expressionPartOriginal.clone()
    expressionPart.variableReplacement(mapOf("π" to "3.1415926535897932384626433832795"))
    if (expressionPart.getContainedVariables().isEmpty()) {
        if (expressionPart.children.isNotEmpty()) {
            val computed = expressionPart.computeNodeIfSimple(simpleComputationRuleParams) ?: return result
            result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(ExpressionNode(NodeType.VARIABLE, computed.toShortString()))))
        } else if (expressionPart.value.toDoubleOrNull() != null) { //add plus node
            val currentValue = expressionPart.value.toDoubleOrNull() ?: return result

            //x ~> (x-1) + 1
            val plusTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
            plusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, (currentValue - 1).toShortString()))
            plusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, "1"))
            result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(plusTreeNode)))

            //x ~> (x+1) - 1
            val minusTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
            minusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, (currentValue + 1).toShortString()))
            minusTreeNode.addChild(ExpressionNode(NodeType.FUNCTION, "-"))
            minusTreeNode.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
            result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(minusTreeNode)))

            val intCurrentValue = currentValue.toInt()
            if (1 < intCurrentValue && intCurrentValue < 145 && (intCurrentValue - currentValue).toReal().additivelyEqualToZero()
            ) {
                val sqrtValue = sqrt(currentValue).toInt()
                for (i in 2..sqrtValue) {
                    val div = intCurrentValue / i
                    if (intCurrentValue == i * div) {
                        val mulTreeNode = ExpressionNode(NodeType.FUNCTION, "*")
                        mulTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, i.toString()))
                        mulTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, div.toString()))
                        result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(mulTreeNode)))
                    }
                }
            }
        }
    }
    return result
}

private fun addRootNodeToExpression(expression: ExpressionNode) : ExpressionNode {
    val root = ExpressionNode(NodeType.FUNCTION, "")
    root.addChild(expression)
    root.computeIdentifier()
    return root
}