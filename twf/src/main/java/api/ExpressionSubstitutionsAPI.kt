package api

import config.CompiledConfiguration
import config.FunctionConfiguration
import expressiontree.*
import platformdependent.escapeCharacters


fun expressionSubstitutionFromStrings(
        left: String = "",
        right: String = "",
        scope: String = "",
        basedOnTaskContext: Boolean = false,
        matchJumbledAndNested: Boolean = false,
        priority: Int = 50,
        code: String = "",
        nameEn: String = "",
        nameRu: String = "",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = ExpressionSubstitution(
        if (left.isNotBlank()) stringToExpression(left, compiledConfiguration = compiledConfiguration) else ExpressionNode(NodeType.EMPTY, ""),
        if (right.isNotBlank()) stringToExpression(right, compiledConfiguration = compiledConfiguration) else ExpressionNode(NodeType.EMPTY, ""),
        basedOnTaskContext = basedOnTaskContext,
        matchJumbledAndNested = matchJumbledAndNested,
        priority = priority,
        code = if (code.isNotBlank()) code else "'${stringToStructureString(left, compiledConfiguration = compiledConfiguration)}'->'${stringToStructureString(right, compiledConfiguration = compiledConfiguration)}'",
        nameEn = nameEn,
        nameRu = nameRu
)


fun expressionSubstitutionFromStructureStrings(
        leftStructureString: String = "",
        rightStructureString: String = "",
        basedOnTaskContext: Boolean = false,
        matchJumbledAndNested: Boolean = false,
        simpleAdditional: Boolean = false,
        isExtending: Boolean = false,
        priority: Int = 50,
        code: String = "",
        nameEn: String = "",
        nameRu: String = "",
        normType: ExpressionSubstitutionNormType = ExpressionSubstitutionNormType.ORIGINAL
) = ExpressionSubstitution(
        if (leftStructureString.isNotBlank()) structureStringToExpression(leftStructureString) else ExpressionNode(NodeType.EMPTY, ""),
        if (rightStructureString.isNotBlank()) structureStringToExpression(rightStructureString) else ExpressionNode(NodeType.EMPTY, ""),
        basedOnTaskContext = basedOnTaskContext,
        matchJumbledAndNested = matchJumbledAndNested,
        simpleAdditional = simpleAdditional,
        isExtending = isExtending,
        priority = priority,
        code = if (code.isNotBlank()) code else "'$leftStructureString'->'$rightStructureString'",
        nameEn = nameEn,
        nameRu = nameRu,
        normType = normType
)


fun findSubstitutionPlacesInExpression(
        expression: ExpressionNode,
        substitution: ExpressionSubstitution,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
): MutableList<SubstitutionPlace> {
    if (substitution.leftFunctions.isNotEmpty() && substitution.leftFunctions.intersect(expression.getContainedFunctions())
                    .isEmpty() &&
            substitution.left.getContainedVariables().intersect(expression.getContainedVariables()).isEmpty()
    ) {
        return mutableListOf()
    }
    var expr = expression
    var result = substitution.findAllPossibleSubstitutionPlaces(expression, compiledConfiguration.factComporator.expressionComporator)
    if (result.isEmpty() && substitution.matchJumbledAndNested && expression.containsNestedSameFunctions()){
        expr = expression.cloneWithExpandingNestedSameFunctions()
        result = substitution.findAllPossibleSubstitutionPlaces(expr, compiledConfiguration.factComporator.expressionComporator)
    }
    if (result.isEmpty()){
        expr = expr.cloneAndSimplifyByComputeSimplePlaces()
        result = substitution.findAllPossibleSubstitutionPlaces(expr, compiledConfiguration.factComporator.expressionComporator)
    }
    return result
}


fun applySubstitution(
        expression: ExpressionNode,
        substitution: ExpressionSubstitution,
        substitutionPlaces: List<SubstitutionPlace>, //containsPointersOnExpressionPlaces
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
): ExpressionNode {
    substitution.applySubstitution(substitutionPlaces, compiledConfiguration.factComporator.expressionComporator)
    expression.getTopNode().reduceExtraSigns(setOf("+"), setOf("-"))
    expression.getTopNode().normilizeSubtructions(FunctionConfiguration())
    expression.getTopNode().computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot()
    expression.normalizeParentLinks()
    return expression
}


fun createCompiledConfigurationFromExpressionSubstitutionsAndParams (
        expressionSubstitutions: Array<ExpressionSubstitution>,
        additionalParamsMap: Map<String, String> = mapOf()
): CompiledConfiguration {
    val simpleComputationRuleCodesCandidates = expressionSubstitutions.map { it.code }.filter { it.isNotBlank() }.toSet()
    return CompiledConfiguration(additionalParamsMap = additionalParamsMap, simpleComputationRuleCodesCandidates = simpleComputationRuleCodesCandidates).apply {
        compiledExpressionTreeTransformationRules.clear()
        compiledExpressionSimpleAdditionalTreeTransformationRules.clear()
        val handledCodesHashSet = hashSetOf<String>()
        for (substitution in expressionSubstitutions) {
            if (handledCodesHashSet.contains(substitution.code))
                continue
            handledCodesHashSet.add(substitution.code)
            if (substitution.left.nodeType == NodeType.EMPTY || substitution.right.nodeType == NodeType.EMPTY) {
                if (substitution.code.isNotEmpty()) {
                    expressionTreeAutogeneratedTransformationRuleIdentifiers.put(substitution.code, substitution)
                }
            } else {
                compiledExpressionTreeTransformationRules.add(substitution)
                if (substitution.simpleAdditional) {
                    compiledExpressionSimpleAdditionalTreeTransformationRules.add(substitution)
                }
            }
        }
    }
}


fun findApplicableSubstitutionsInSelectedPlace (
        expression: ExpressionNode,
        selectedNodeIds: Array<Int>,
        compiledConfiguration: CompiledConfiguration,
        simplifyNotSelectedTopArguments: Boolean = false,
        withReadyApplicationResult: Boolean = true,
        withFullExpressionChangingPart: Boolean = true
) = generateSubstitutionsBySelectedNodes(
        SubstitutionSelectionData(expression, selectedNodeIds, compiledConfiguration),
        withReadyApplicationResult = withReadyApplicationResult
).apply {
    if (withFullExpressionChangingPart) {
        forEach {
            it.originalExpressionChangingPart = ExpressionNode(NodeType.FUNCTION, "").apply { addChild(it.originalExpressionChangingPart) }
            it.resultExpressionChangingPart = ExpressionNode(NodeType.FUNCTION, "").apply { addChild(it.resultExpressionChangingPart) }
        }
    }
}


fun applySubstitutionInSelectedPlace (
        expression: ExpressionNode,
        selectedNodeIds: Array<Int>,
        substitution: ExpressionSubstitution,
        compiledConfiguration: CompiledConfiguration,
        simplifyNotSelectedTopArguments: Boolean = false
): ExpressionNode? {
    val substitutionSelectionData = SubstitutionSelectionData(expression, selectedNodeIds, compiledConfiguration)
    fillSubstitutionSelectionData(substitutionSelectionData)
    return applySubstitution(substitutionSelectionData, substitution, simplifyNotSelectedTopArguments)
}


fun findLowestSubtreeTopOfSelectedNodesInExpression(
        node: ExpressionNode,
        selectedNodes: List<ExpressionNode>
) = node.findLowestSubtreeTopOfNodes(selectedNodes)



//string API
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
