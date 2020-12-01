package expressiontree

import api.normalizeExpressionToUsualForm
import config.CompiledConfiguration
import numbers.toReal
import platformdependent.toShortString
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun nodeIdsPositionsMap(nodeIds: List<Int>): Map<Int, Int> {
    val nodeIdsMap = mutableMapOf<Int, Int>()
    for (i in 0..nodeIds.lastIndex) {
        nodeIdsMap.put(nodeIds[i], i)
    }
    return nodeIdsMap
}

fun ExpressionNode.nodeIdsToNodeLinksInSameOrder(nodeIds: List<Int>, nodeIdsMap: Map<Int, Int> = nodeIdsPositionsMap(nodeIds)): List<ExpressionNode> {
    val result = MutableList(nodeIds.size, { ExpressionNode(NodeType.EMPTY, "") })
    nodeIdsToNodeLinksInSameOrderRecursivePart(nodeIdsMap, result)
    return result
}

private fun ExpressionNode.nodeIdsToNodeLinksInSameOrderRecursivePart(nodeIdsMap: Map<Int, Int>, result: MutableList<ExpressionNode>) {
    val index = nodeIdsMap[nodeId]
    if (index != null) {
        result[index] = this
    }
    for (child in children) {
        child.nodeIdsToNodeLinksInSameOrderRecursivePart(nodeIdsMap, result)
    }
}

// Pointer on ExpressionNode is specified to mention that this code should be applied to nodeIds in one tree only
fun ExpressionNode.findLowestSubtreeTopOfNodes(nodes: List<ExpressionNode>): ExpressionNode? {
    var currentSubtree = nodes.firstOrNull() ?: return null
    val nodeChains = nodes.toMutableList()
    var subtreeOfNodesFound = false
    while (!subtreeOfNodesFound) {
        subtreeOfNodesFound = true
        for (i in 0..nodeChains.lastIndex) {
            while (nodeChains[i].distanceToRoot > currentSubtree.distanceToRoot) {
                nodeChains[i] = nodeChains[i].parent ?: return null // tree is not consistent
            }
            if (nodeChains[i].distanceToRoot == currentSubtree.distanceToRoot && nodeChains[i].nodeId != currentSubtree.nodeId) {
                nodeChains[i] = nodeChains[i].parent ?: return null // tree is not consistent
            }
            if (nodeChains[i].distanceToRoot < currentSubtree.distanceToRoot) {
                currentSubtree = nodeChains[i]
                subtreeOfNodesFound = false
            }
        }
    }
    return currentSubtree
}

// Pointer on ExpressionNode is specified to mention that this code should be applied to nodeIds in one tree only
fun ExpressionNode.findLowestSubtreeWithNodes(nodes: List<ExpressionNode>, onlyHigherSelection: Boolean): ExpressionNode? {
    var currentSubtree: ExpressionNode? = null
    val nodeChains = nodes.toMutableList()
    val nodeSelectedChains: MutableList<ExpressionNode?> = nodeChains.map {
        val res = if (onlyHigherSelection) it.copy() else it.clone()
        res.apply { linkOnOriginalTreeNode = it }
    }.toMutableList()
    val treeParts = nodeSelectedChains.map { Pair(it!!.nodeId, it) }.toMap().toMutableMap()
    var subtreeOfNodesFound = false
    while (!subtreeOfNodesFound) {
        subtreeOfNodesFound = true
        for (i in 0..nodeChains.lastIndex) {
            if (nodeSelectedChains[i] == null || (currentSubtree != null && nodeChains[i].nodeId == currentSubtree.nodeId))
                continue
            if (currentSubtree != null && ((nodeChains[i].distanceToRoot > currentSubtree.distanceToRoot) ||
                            (nodeChains[i].functionStringDefinition?.function?.mainFunction != nodeChains[i].value) ||
                            (nodeChains[i].distanceToRoot == currentSubtree.distanceToRoot && nodeChains[i].nodeId != currentSubtree.nodeId))) {
                nodeChains[i] = nodeChains[i].parent ?: return null // tree is not consistent
                val suchPart = treeParts[nodeChains[i].nodeId]
                if (suchPart != null) {
                    if (suchPart.children.all { it.nodeId != nodeSelectedChains[i]!!.nodeId })
                        suchPart.addChild(nodeSelectedChains[i]!!)
                    nodeSelectedChains[i] = null
                } else {
                    val newChainParent = nodeChains[i].copy().apply { linkOnOriginalTreeNode = nodeChains[i] }
                    newChainParent.addChild(nodeSelectedChains[i]!!)
                    nodeSelectedChains[i] = newChainParent
                    treeParts.put(nodeChains[i].nodeId, nodeSelectedChains[i]!!)
                }
                subtreeOfNodesFound = false
            } else if (currentSubtree == null || nodeChains[i].distanceToRoot < currentSubtree.distanceToRoot) {
                currentSubtree = nodeSelectedChains[i]!!
                subtreeOfNodesFound = false
            } else {
                for (j in 0..nodeSelectedChains[i]!!.children.lastIndex) {
                    currentSubtree.addChild(nodeSelectedChains[i]!!.children[j])
                }
                nodeSelectedChains[i] = null
            }
        }
    }
    if (currentSubtree != null) {
        currentSubtree.sortChildrenAscendingNodeIds()
    }
    return currentSubtree
}

fun ExpressionNode.cloneWithoutSelectedNodes(selectedTopNodeIds: Set<Int>): ExpressionNode {
    if (nodeId in selectedTopNodeIds) {
        return ExpressionNode(NodeType.EMPTY, "")
    }
    val result = copy()
    if (result.nodeType == NodeType.VARIABLE) {
        return result
    }
    for (child in children) {
        val newChild = child.cloneWithoutSelectedNodes(selectedTopNodeIds)
        if (newChild.nodeType != NodeType.EMPTY) {
            result.addChild(newChild)
        } else if (value == "/") {
            result.addChild(ExpressionNode(NodeType.VARIABLE, "1")) // to leave fraction sign unchanged
        }
    }
    if (result.functionStringDefinition!!.function.numberOfArguments == -1 && result.children.size == 0) {
        return ExpressionNode(NodeType.EMPTY, "")
    } else if (result.functionStringDefinition!!.function.numberOfArguments > 0 && result.functionStringDefinition!!.function.numberOfArguments < result.children.size) {
        return ExpressionNode(NodeType.EMPTY, "")
    }
    return result
}

data class SubstitutionSelectionData(
        val originalExpression: ExpressionNode,
        val selectedNodeIds: List<Int>,
        val compiledConfiguration: CompiledConfiguration,
        val expressionToTransform: ExpressionNode = originalExpression.clone(),
        val isMoving: Boolean = false,
        val selectedNodeIdsMap: Map<Int, Int> = nodeIdsPositionsMap(selectedNodeIds),
        val selectedNodes: List<ExpressionNode> = expressionToTransform.nodeIdsToNodeLinksInSameOrder(selectedNodeIds, selectedNodeIdsMap),
        var topOfSelection: ExpressionNode? = null,
        var topOfSelectionParent: ExpressionNode? = null,
        var topOfSelectionIndex: Int = 0,
        var lowestSubtree: ExpressionNode? = null,
        var lowestSubtreeHigh: ExpressionNode? = null,
        var notSelectedSubtreeTopOriginalTree: ExpressionNode? = null,
        var notSelectedSubtreeTopArguments: ExpressionNode? = null, // in original order
        var selectedSubtreeTopArguments: ExpressionNode? = null, // in original order
        var selectedSubtreeTopArgumentsInSelectionOrder: ExpressionNode? = null
)

data class SubstitutionApplication(
        val expressionSubstitution: ExpressionSubstitution,
        val originalExpression: ExpressionNode,
        val originalExpressionChangingPart: ExpressionNode,
        val resultExpression: ExpressionNode,
        val resultExpressionChangingPart: ExpressionNode,
        val substitutionType: String,
        var priority: Int // as smaller as higher in output
)

fun simpleCommutativeOperationSelectionHandling(substitutionSelectionData: SubstitutionSelectionData) {
    if (substitutionSelectionData.selectedNodeIds.size > 1 && substitutionSelectionData.lowestSubtreeHigh != null && (substitutionSelectionData.lowestSubtreeHigh!!.functionStringDefinition?.function?.isCommutativeWithNullWeight ?: return ||
                    (substitutionSelectionData.selectedNodeIds.size == 2 && substitutionSelectionData.lowestSubtreeHigh!!.getChildNodesOnDepthOrWhileOperation(2, listOf("*", "/")).map { it.nodeId }.containsAll(substitutionSelectionData.selectedNodeIds) &&
                            substitutionSelectionData.lowestSubtreeHigh!!.value == "/"))) {
        val topOperation = substitutionSelectionData.lowestSubtreeHigh!!.functionStringDefinition!!.function.notObligateMainFunction()
        substitutionSelectionData.notSelectedSubtreeTopOriginalTree = substitutionSelectionData.topOfSelection!!.cloneWithoutSelectedNodes(substitutionSelectionData.lowestSubtreeHigh!!.getContainedChildOperationNodeIds(topOperation))
        substitutionSelectionData.notSelectedSubtreeTopArguments = topOperationNode(topOperation, substitutionSelectionData.lowestSubtreeHigh!!.value, substitutionSelectionData)
        if (substitutionSelectionData.lowestSubtreeHigh!!.value != "/") {
            substitutionSelectionData.notSelectedSubtreeTopArguments!!.addChild(ExpressionNode(NodeType.EMPTY, "place_for_result"))
        }
        substitutionSelectionData.selectedSubtreeTopArguments = topOperationNode(topOperation, substitutionSelectionData.lowestSubtreeHigh!!.value, substitutionSelectionData)
        substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder = topOperationNode(topOperation, substitutionSelectionData.lowestSubtreeHigh!!.value, substitutionSelectionData)
        substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children = Array(substitutionSelectionData.selectedNodeIds.size, { ExpressionNode(NodeType.EMPTY, "") }).toMutableList()
        substitutionSelectionData.lowestSubtreeHigh!!.simpleCommutativeOperationSelectionHandlingRecursivePart(substitutionSelectionData, topOperation)
        substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children = substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.filter { it.nodeType != NodeType.EMPTY }.toMutableList()
    } else {
        substitutionSelectionData.selectedSubtreeTopArguments = substitutionSelectionData.topOfSelection
        while (substitutionSelectionData.selectedSubtreeTopArguments!!.value.isEmpty() && substitutionSelectionData.selectedSubtreeTopArguments!!.children.size == 1) {
            substitutionSelectionData.selectedSubtreeTopArguments = substitutionSelectionData.selectedSubtreeTopArguments!!.children.first()
        }
        substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder = substitutionSelectionData.selectedSubtreeTopArguments
    }
}

private fun topOperationNode(topOperation: String, value: String, substitutionSelectionData: SubstitutionSelectionData) =
        if (value != "/") {
            substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode(topOperation, -1)
                    .apply { linkOnOriginalTreeNode = substitutionSelectionData.lowestSubtreeHigh!!.linkOnOriginalTreeNode }
        } else { // both sides of division were partly selected
            substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1)
                    .apply {
                        linkOnOriginalTreeNode = substitutionSelectionData.lowestSubtreeHigh!!.linkOnOriginalTreeNode
                        addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
                                .apply { linkOnOriginalTreeNode = substitutionSelectionData.lowestSubtreeHigh!!.linkOnOriginalTreeNode })
                        addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
                                .apply { linkOnOriginalTreeNode = substitutionSelectionData.lowestSubtreeHigh!!.linkOnOriginalTreeNode })
                    }
        }

private fun ExpressionNode.simpleCommutativeOperationSelectionHandlingRecursivePart(substitutionSelectionData: SubstitutionSelectionData, topOperation: String, hasMinus: Boolean = false, hasDivision: Boolean = false) {
    val originalNode = this.linkOnOriginalTreeNode!!
    val selectedNodeIdsMap = this.children.map { Pair(it.nodeId, it) }.toMap()
    for (i in 0..originalNode.children.lastIndex) {
        val updatedHasDivision = hasDivision xor (i > 0 && value == "/")
        val child = originalNode.children[i]
        val childSelectionNode = selectedNodeIdsMap[child.nodeId]
        if (childSelectionNode == null) {
            val newChild = createNewArgument(hasMinus, child, substitutionSelectionData)
            substitutionSelectionData.notSelectedSubtreeTopArguments!!.minusDivisionSafetyAddChild(substitutionSelectionData.compiledConfiguration, newChild, updatedHasDivision)
        } else {
            if (childSelectionNode.children.isNotEmpty() && (childSelectionNode.functionStringDefinition?.function?.notObligateMainFunction() == topOperation ||
                            (childSelectionNode.children.size == 1 && childSelectionNode.value == "+" && childSelectionNode.children.first().value == "-"))) {
                childSelectionNode.simpleCommutativeOperationSelectionHandlingRecursivePart(substitutionSelectionData, topOperation, hasMinus xor (childSelectionNode.value == "-"), updatedHasDivision)
            } else {
                val newChild = createNewArgument(hasMinus, child, substitutionSelectionData)
                substitutionSelectionData.selectedSubtreeTopArguments!!.minusDivisionSafetyAddChild(substitutionSelectionData.compiledConfiguration, newChild, updatedHasDivision)
                if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value != "/") {
                    substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.minusSafetySetChildOnPosition(substitutionSelectionData.compiledConfiguration, newChild.clone(), newChild.findPositionOfClosedSelectedNode(substitutionSelectionData))
                }
            }
        }
    }
}

private fun createNewArgument(hasMinus: Boolean, child: ExpressionNode, substitutionSelectionData: SubstitutionSelectionData) = if (hasMinus) {
    if (hasMinus && child.value == "-" && child.children.size == 1) {
        child.children.first().clone()
    } else {
        generateMinusNode(substitutionSelectionData.notSelectedSubtreeTopArguments!!.value, substitutionSelectionData.compiledConfiguration, child.clone())
    }
} else {
    child.clone()
}

private fun ExpressionNode.minusDivisionSafetyAddChild(compiledConfiguration: CompiledConfiguration, child: ExpressionNode, hasDivision: Boolean) {
    val newChild = if (child.value == "-" && value != "+") {
        compiledConfiguration.createExpressionFunctionNode("+", -1).apply { addChild(child) }
    } else {
        child
    }
    if (value != "/") {
        addChild(newChild)
    } else {
        if (hasDivision) {
            children.last().addChild(newChild) //add to denominator
        } else {
            children.first().addChild(newChild) //add to numerator
        }
    }
}

private fun ExpressionNode.minusSafetySetChildOnPosition(compiledConfiguration: CompiledConfiguration, child: ExpressionNode, position: Int) {
    if (child.value == "-" && value != "+") {
        val newChild = compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
            addChild(child)
        }
        setChildOnPosition(newChild, position)
    } else {
        setChildOnPosition(child, position)
    }
}

private fun generateMinusNode(currentNodeValue: String, compiledConfiguration: CompiledConfiguration, child: ExpressionNode) =
        if (currentNodeValue == "+") {
            compiledConfiguration.createExpressionFunctionNode("-", -1).apply {
                addChild(child.clone())
            }
        } else {
            compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
                addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
                children.last().addChild(child.clone())
            }
        }

private fun ExpressionNode.findPositionOfClosedSelectedNode(substitutionSelectionData: SubstitutionSelectionData): Int {
    var result = substitutionSelectionData.selectedNodeIdsMap[nodeId]
    if (result != null)
        return result
    for (child in children) {
        result = child.findPositionOfClosedSelectedNode(substitutionSelectionData)
        if (result >= 0)
            return result
    }
    return -1
}

fun fillSubstitutionSelectionData(substitutionSelectionData: SubstitutionSelectionData) {
    substitutionSelectionData.topOfSelection = substitutionSelectionData.expressionToTransform.findLowestSubtreeTopOfNodes(substitutionSelectionData.selectedNodes)
    substitutionSelectionData.topOfSelectionParent = substitutionSelectionData.topOfSelection?.parent
    substitutionSelectionData.topOfSelectionIndex = substitutionSelectionData.topOfSelectionParent!!.children.indexOf(substitutionSelectionData.topOfSelection)
    substitutionSelectionData.lowestSubtree = substitutionSelectionData.expressionToTransform.findLowestSubtreeWithNodes(substitutionSelectionData.selectedNodes, false)
    substitutionSelectionData.lowestSubtreeHigh = substitutionSelectionData.expressionToTransform.findLowestSubtreeWithNodes(substitutionSelectionData.selectedNodes, true)
    simpleCommutativeOperationSelectionHandling(substitutionSelectionData)
}

fun findConfiguredSubstitutionsApplications(substitutionSelectionData: SubstitutionSelectionData,
                                            simplifyNotSelectedTopArguments: Boolean = false,
                                            withReadyApplicationResult: Boolean = false,
                                            fastestAppropriateVersion: Boolean = false,
                                            withExtendingSubstitutions: Boolean = true): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    for (transformation in substitutionSelectionData.compiledConfiguration.compiledExpressionTreeTransformationRules) {
        if (!withExtendingSubstitutions && transformation.isExtending) {
            continue
        }
        var substitutionInstance = checkLeftCondition(substitutionSelectionData, transformation, fastestAppropriateVersion)
                ?: continue
        if (substitutionInstance.isApplicable) {
            val applicationToSelectedPartResult = transformation.applyRight(substitutionInstance) ?: continue
            addApplicationToResults(withReadyApplicationResult, substitutionSelectionData, simplifyNotSelectedTopArguments, applicationToSelectedPartResult, result, transformation,
                    transformation.type ?: "ConfiguredSubstitution",
                    transformation.priority ?: 50)
        }
    }
    return result
}

fun generatePermutationSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                     simplifyNotSelectedTopArguments: Boolean = false,
                                     withReadyApplicationResult: Boolean = false,
                                     fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder != null && substitutionSelectionData.selectedSubtreeTopArguments?.functionStringDefinition?.function?.isCommutativeWithNullWeight == true) {
        val userSelectedDirectlyChildrenOfCommutativeNode = substitutionSelectionData.lowestSubtreeHigh!!.allParentsMainFunctionIs(substitutionSelectionData.lowestSubtreeHigh!!.functionStringDefinition!!.function.mainFunction)
        var swapSubstitutionSuggested = false
        if (userSelectedDirectlyChildrenOfCommutativeNode) {
            val selectedHighList = substitutionSelectionData.lowestSubtreeHigh!!.listWhichParentsFunctionIs(substitutionSelectionData.lowestSubtreeHigh!!.functionStringDefinition!!.function.mainFunction)
            if (selectedHighList.size == 2) {
                val firstNode = selectedHighList.first()
                val firstNodeParent = firstNode.parent ?: return result
                val firstNodeIndex = firstNodeParent.children.indexOf(firstNode)

                val secondNode = selectedHighList.last()
                val secondNodeParent = secondNode.parent ?: return result
                val secondNodeIndex = secondNodeParent.children.indexOf(secondNode)

                firstNodeParent.setChildOnPosition(secondNode, firstNodeIndex)
                secondNodeParent.setChildOnPosition(firstNode, secondNodeIndex)
                val swapResult = substitutionSelectionData.topOfSelection!!.clone()


                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(substitutionSelectionData.topOfSelection!!.clone(), substitutionSelectionData.topOfSelectionIndex)
                val resultExpression = substitutionSelectionData.expressionToTransform.clone().apply {
                    normalizeExpressionToUsualForm(this)
                } //because this code performs lots times for lots of substitutions

                firstNodeParent.setChildOnPosition(firstNode, firstNodeIndex)
                secondNodeParent.setChildOnPosition(secondNode, secondNodeIndex)

                val swapSubstitution = ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.topOfSelection!!.clone()), addRootNodeToExpression(swapResult))
                result.add(SubstitutionApplication(
                        swapSubstitution,
                        substitutionSelectionData.originalExpression,
                        substitutionSelectionData.topOfSelection!!.clone(),
                        resultExpression,
                        swapResult,
                        "Swap", 10
                ))
                swapSubstitutionSuggested = true
            }
        }

        if (((substitutionSelectionData.notSelectedSubtreeTopArguments != null && substitutionSelectionData.notSelectedSubtreeTopArguments!!.children.size > 0) ||
                        (!swapSubstitutionSuggested && substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.toString() != substitutionSelectionData.selectedSubtreeTopArguments!!.toString()))) {
            // 1. Transform arguments in selected order
            addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                    substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!,
                    result,
                    ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.clone())), "Extraction",
                    if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.toString() != substitutionSelectionData.selectedSubtreeTopArguments!!.toString()) 30 else 80,
                    true) // to increase speed - use '!fastestAppropriateVersion'

            if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.size > 1 && userSelectedDirectlyChildrenOfCommutativeNode) { // to increase speed - use '&& !fastestAppropriateVersion'
                // 2. Transform arguments in selected order in brackets
                val selectedPartTransformationResultInSelectedOrderInBrackets = substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.copy().apply { addChild(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.clone()) }
                addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                        selectedPartTransformationResultInSelectedOrderInBrackets,
                        result,
                        ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(selectedPartTransformationResultInSelectedOrderInBrackets)), "Extraction", 90)

                if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.toString() != substitutionSelectionData.selectedSubtreeTopArguments!!.toString() &&
                        (substitutionSelectionData.notSelectedSubtreeTopArguments != null && substitutionSelectionData.notSelectedSubtreeTopArguments!!.children.size > 0) &&
                        substitutionSelectionData.topOfSelection!!.toString() != substitutionSelectionData.selectedSubtreeTopArguments!!.toString()) {
                    // 3. Transform arguments in original order
                    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                            substitutionSelectionData.selectedSubtreeTopArguments!!,
                            result,
                            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone())), "Extraction", 80,
                            true) // to increase speed - use '!fastestAppropriateVersion'

                    // 4. Transform arguments in original order in brackets
                    val selectedPartTransformationResultInOriginalOrderInBrackets = substitutionSelectionData.selectedSubtreeTopArguments!!.copy().apply { addChild(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()) }
                    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                            selectedPartTransformationResultInOriginalOrderInBrackets,
                            result,
                            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(selectedPartTransformationResultInOriginalOrderInBrackets)), "Extraction", 90)
                }
            }
        }
    }
    return result
}

data class ExpressionStrictureIdentifierCounter(val expressionStrictureIdentifier: ExpressionStrictureIdentifier, var count: Int = 1)

fun generateReduceArithmeticSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                          simplifyNotSelectedTopArguments: Boolean = false,
                                          withReadyApplicationResult: Boolean = false,
                                          fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder != null && substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.size > 1) {
        substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.fillStructureStringIdentifiers()
        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "+") { // + (*) -> * (+)
            var possibleMultipliersSet = getMultipliersFromNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first())
            for (i in 1..substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.lastIndex) {
                val childMultipliers = getMultipliersFromNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children[i])
                for (possibleMultiplier in possibleMultipliersSet) {
                    val maxCount = childMultipliers.firstOrNull { it.expressionStrictureIdentifier == possibleMultiplier.expressionStrictureIdentifier }?.count
                            ?: 0
                    possibleMultiplier.count = min(possibleMultiplier.count, maxCount)
                }
                possibleMultipliersSet = possibleMultipliersSet.filter { it.count > 0 }
            }
            if (possibleMultipliersSet.isNotEmpty()) {
                val prodNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
                val sumNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1)
                handleAdditiveNodeAsReductionPart(substitutionSelectionData, substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first(), possibleMultipliersSet, sumNode, prodNode)
                for (i in 1..substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.lastIndex) {
                    handleAdditiveNodeAsReductionPart(substitutionSelectionData, substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children[i], possibleMultipliersSet, sumNode, null)
                }
                prodNode.addChild(sumNode)
                addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                        prodNode,
                        result,
                        ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(prodNode)), "ReduceArithmetic", 5)
            }
        }

        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "+") { // a/x+b/x -> (a+b)/x;
            var possibleDenominator: ExpressionNode? = getOperandsFrom2ArgsNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first(), "/", 1, ExpressionNode(NodeType.EMPTY, ""))
            if (possibleDenominator!!.nodeType != NodeType.EMPTY) {
                for (i in 1..substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.lastIndex) {
                    if (possibleDenominator?.expressionStrictureIdentifier != getOperandsFrom2ArgsNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children[i], "/", 1, ExpressionNode(NodeType.EMPTY, "")).expressionStrictureIdentifier) {
                        possibleDenominator = null
                        break
                    }
                }
                if (possibleDenominator != null) {
                    val degNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1)
                    val prodNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
                    for (child in substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children) {
                        prodNode.addChild(getOperandsFrom2ArgsNode(child, "/", 0, null, substitutionSelectionData).clone())
                    }
                    degNode.addChild(prodNode)
                    degNode.addChild(possibleDenominator.clone())
                    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                            degNode,
                            result,
                            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(degNode)), "ReduceArithmetic", 5)
                }
            }
        }

        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "*") { // a^x*a^y -> a^(x+y);
            var possibleDegree: ExpressionNode? = getOperandsFrom2ArgsNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first(), "^", 0, null)
            for (i in 1..substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.lastIndex) {
                if (possibleDegree?.expressionStrictureIdentifier != getOperandsFrom2ArgsNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children[i], "^", 0, null).expressionStrictureIdentifier) {
                    possibleDegree = null
                    break
                }
            }
            if (possibleDegree != null) {
                val degNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1)
                degNode.addChild(possibleDegree.clone())
                val sumNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1)
                for (child in substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children) {
                    sumNode.addChild(getOperandsFrom2ArgsNode(child, "^", 1, ExpressionNode(NodeType.VARIABLE, "1")).clone())
                }
                degNode.addChild(sumNode)
                addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                        degNode,
                        result,
                        ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(degNode)), "ReduceArithmetic", 5)
            }
        }

        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "*") { // a^x*b^x -> (a*b)^x;
            var possibleDegree: ExpressionNode? = getOperandsFrom2ArgsNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first(), "^", 1, ExpressionNode(NodeType.EMPTY, ""))
            if (possibleDegree!!.nodeType != NodeType.EMPTY) {
                for (i in 1..substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.lastIndex) {
                    if (possibleDegree?.expressionStrictureIdentifier != getOperandsFrom2ArgsNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children[i], "^", 1, ExpressionNode(NodeType.EMPTY, "")).expressionStrictureIdentifier) {
                        possibleDegree = null
                        break
                    }
                }
                if (possibleDegree != null) {
                    val degNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1)
                    val prodNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
                    for (child in substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children) {
                        prodNode.addChild(getOperandsFrom2ArgsNode(child, "^", 0, null).clone())
                    }
                    degNode.addChild(prodNode)
                    degNode.addChild(possibleDegree.clone())
                    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                            degNode,
                            result,
                            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(degNode)), "ReduceArithmetic", 5)
                }
            }
        }

    }
    return result
}

fun generateReduceFractionSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                        simplifyNotSelectedTopArguments: Boolean = false,
                                        withReadyApplicationResult: Boolean = false,
                                        fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    if (substitutionSelectionData.selectedSubtreeTopArguments!!.value == "/" && substitutionSelectionData.selectedNodeIds.size == 2 &&
            substitutionSelectionData.selectedSubtreeTopArguments!!.children.all { it.children.size == 1 }) {
        substitutionSelectionData.selectedSubtreeTopArguments!!.fillStructureStringIdentifiers()
        val numerator = substitutionSelectionData.selectedSubtreeTopArguments!!.children.first().children.first()
        val denominator = substitutionSelectionData.selectedSubtreeTopArguments!!.children.last().children.first()
        val rightBase = substitutionSelectionData.notSelectedSubtreeTopArguments!!

        if (numerator.expressionStrictureIdentifier == denominator.expressionStrictureIdentifier) {
            val expressionSubstitution = ExpressionSubstitution(addRootNodeToExpression(
                    substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                        addChild(numerator)
                        addChild(denominator)
                    }),
                    addRootNodeToExpression(ExpressionNode(NodeType.VARIABLE, "1")))
            val applicationPlace = substitutionSelectionData.topOfSelection!!.clone()
            val applicationResultInPlace = rightBase.clone().normalizeReduceFractionResult()
            substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationResultInPlace, substitutionSelectionData.topOfSelectionIndex)
            result.add(SubstitutionApplication(
                    expressionSubstitution,
                    substitutionSelectionData.originalExpression,
                    applicationPlace,
                    substitutionSelectionData.expressionToTransform.clone().apply {
                        normalizeExpressionToUsualForm(this)
                    },
                    applicationResultInPlace,
                    "ReduceFraction", 5
            ))
        } else if (numerator.isNumberValue() && denominator.isNumberValue()) {
            val num = numerator.value.toDoubleOrNull() ?: return result
            val denom = denominator.value.toDoubleOrNull() ?: return result
            val numDivDenom = (num / denom).toInt()
            val denomDivNum = (denom / num).toInt()
            val resNode: ExpressionNode? = if (substitutionSelectionData.compiledConfiguration.factComporator.expressionComporator.baseOperationsDefinitions.additivelyEqual(num, denom * numDivDenom)) {
                ExpressionNode(NodeType.VARIABLE, numDivDenom.toString())
            } else if (substitutionSelectionData.compiledConfiguration.factComporator.expressionComporator.baseOperationsDefinitions.additivelyEqual(denom, num * denomDivNum)) {
                substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                    addChild(ExpressionNode(NodeType.VARIABLE, "1"))
                    addChild(ExpressionNode(NodeType.VARIABLE, denomDivNum.toString()))
                }
            } else null
            if (resNode != null) {
                val expressionSubstitution = ExpressionSubstitution(addRootNodeToExpression(
                        substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                            addChild(numerator)
                            addChild(denominator)
                        }),
                        addRootNodeToExpression(resNode))
                val applicationPlace = substitutionSelectionData.topOfSelection!!.clone()
                val applicationResultInPlace = rightBase.clone().apply {
                    if (resNode.value == "/") {
                        children.last().addChild(ExpressionNode(NodeType.VARIABLE, denomDivNum.toString()))
                    } else {
                        children.first().addChild(ExpressionNode(NodeType.VARIABLE, numDivDenom.toString()))
                    }
                }.normalizeReduceFractionResult()
                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationResultInPlace, substitutionSelectionData.topOfSelectionIndex)
                result.add(SubstitutionApplication(
                        expressionSubstitution,
                        substitutionSelectionData.originalExpression,
                        applicationPlace,
                        substitutionSelectionData.expressionToTransform.clone().apply {
                            normalizeExpressionToUsualForm(this)
                        },
                        applicationResultInPlace,
                        "ReduceFraction", 5
                ))
            }
        } else if (numerator.value == "^" && denominator.value == "^" && numerator.children.size == 2 && denominator.children.size == 2 &&
                numerator.children.first().expressionStrictureIdentifier == denominator.children.first().expressionStrictureIdentifier) {
            val numPow = numerator.children.last().value.toDoubleOrNull()
            val denomPow = denominator.children.last().value.toDoubleOrNull()
            if (numPow != null && denomPow != null) {
                val resNode = if (numPow >= denomPow) {
                    val pow = numPow - denomPow
                    if (substitutionSelectionData.compiledConfiguration.factComporator.expressionComporator.baseOperationsDefinitions.additivelyEqual(pow, 1.0)) {
                        numerator.children.first().clone()
                    } else {
                        substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1).apply {
                            addChild(numerator.children.first().clone())
                            addChild(ExpressionNode(NodeType.VARIABLE, pow.toShortString()))
                        }
                    }
                } else {
                    val pow = denomPow - numPow
                    substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                        addChild(ExpressionNode(NodeType.VARIABLE, "1"))
                        if (substitutionSelectionData.compiledConfiguration.factComporator.expressionComporator.baseOperationsDefinitions.additivelyEqual(pow, 1.0)) {
                            addChild(numerator.children.first().clone())
                        } else {
                            addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1).apply {
                                addChild(numerator.children.first().clone())
                                addChild(ExpressionNode(NodeType.VARIABLE, pow.toShortString()))
                            })
                        }
                    }
                }
                val expressionSubstitution = ExpressionSubstitution(addRootNodeToExpression(
                        substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                            addChild(numerator)
                            addChild(denominator)
                        }),
                        addRootNodeToExpression(resNode))
                val applicationPlace = substitutionSelectionData.topOfSelection!!.clone()
                val applicationResultInPlace = rightBase.clone().apply {
                    if (resNode.value == "/") {
                        children.last().addChild(resNode.children.last().clone())
                    } else {
                        children.first().addChild(resNode.clone())
                    }
                }.normalizeReduceFractionResult()
                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationResultInPlace, substitutionSelectionData.topOfSelectionIndex)
                result.add(SubstitutionApplication(
                        expressionSubstitution,
                        substitutionSelectionData.originalExpression,
                        applicationPlace,
                        substitutionSelectionData.expressionToTransform.clone().apply {
                            normalizeExpressionToUsualForm(this)
                        },
                        applicationResultInPlace,
                        "ReduceFraction", 5
                ))
            } else {
                // 1. To numerator
                val resNodeNumerator = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1).apply {
                    addChild(numerator.children.first().clone())
                    addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
                        addChild(numerator.children.last())
                        addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("-", -1).apply {
                            addChild(denominator.children.last())
                        })
                    })
                }
                val expressionSubstitutionNumerator = ExpressionSubstitution(addRootNodeToExpression(
                        substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                            addChild(numerator)
                            addChild(denominator)
                        }),
                        addRootNodeToExpression(resNodeNumerator))
                val applicationPlaceNumerator = substitutionSelectionData.topOfSelection!!.clone()
                val applicationResultInPlaceNumerator = rightBase.clone().apply {
                    children.first().addChild(resNodeNumerator.clone())
                }.normalizeReduceFractionResult()
                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationResultInPlaceNumerator, substitutionSelectionData.topOfSelectionIndex)
                result.add(SubstitutionApplication(
                        expressionSubstitutionNumerator,
                        substitutionSelectionData.originalExpression,
                        applicationPlaceNumerator,
                        substitutionSelectionData.expressionToTransform.clone().apply {
                            normalizeExpressionToUsualForm(this)
                        },
                        applicationResultInPlaceNumerator,
                        "ReduceFraction", 5
                ))

                // 2. To denominator
                val resNodeDenominator = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                    addChild(ExpressionNode(NodeType.VARIABLE, "1"))
                    addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1).apply {
                        addChild(numerator.children.first().clone())
                        addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
                            addChild(denominator.children.last())
                            addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("-", -1).apply {
                                addChild(numerator.children.last())
                            })
                        })
                    })
                }
                val expressionSubstitutionDenominator = ExpressionSubstitution(addRootNodeToExpression(
                        substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("/", -1).apply {
                            addChild(numerator)
                            addChild(denominator)
                        }),
                        addRootNodeToExpression(resNodeDenominator))
                val applicationPlaceDenominator = substitutionSelectionData.topOfSelection!!.clone()
                val applicationResultInPlaceDenominator = rightBase.clone().apply {
                    children.last().addChild(resNodeDenominator.children.last().clone())
                }.normalizeReduceFractionResult()
                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationResultInPlaceDenominator, substitutionSelectionData.topOfSelectionIndex)
                result.add(SubstitutionApplication(
                        expressionSubstitutionDenominator,
                        substitutionSelectionData.originalExpression,
                        applicationPlaceDenominator,
                        substitutionSelectionData.expressionToTransform.clone().apply {
                            normalizeExpressionToUsualForm(this)
                        },
                        applicationResultInPlaceDenominator,
                        "ReduceFraction", 5
                ))
            }
        }
    }
    return result
}

private fun ExpressionNode.normalizeReduceFractionResult(): ExpressionNode {
    if (children.all { it.children.size == 0 }) {
        setVariable("1")
    } else {
        if (children.first().children.size == 0) {
            children.first().setVariable("1")
        } else if (children.first().children.size == 1) {
            setChildOnPosition(children.first().children.first(), 0)
        }
        if (children.last().children.size == 0) {
            value = children.first().value
            functionStringDefinition = children.first().functionStringDefinition
            children = children.first().children
            children.forEach { it.parent = this }
        } else if (children.last().children.size == 1) {
            setChildOnPosition(children.last().children.first(), 1)
        }
    }
    return this
}

fun getOperandsFrom2ArgsNode(expressionNode: ExpressionNode, operation: String, operandIndex: Int, ifNotReturn: ExpressionNode?, substitutionSelectionData: SubstitutionSelectionData? = null, hasMinus: Boolean = false): ExpressionNode {
    if (expressionNode.value == "-") {
        return getOperandsFrom2ArgsNode(expressionNode.children.first(), operation, operandIndex, ifNotReturn, substitutionSelectionData, hasMinus xor true)
    } else {
        val result = if (expressionNode.value == operation && expressionNode.children.size == 2) {
            expressionNode.children[operandIndex]
        } else {
            ifNotReturn ?: expressionNode
        }
        if (hasMinus && substitutionSelectionData != null) {
            return substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("-", -1).apply { addChild(result) }
        } else return result
    }
}

fun getMultipliersFromNode(expressionNode: ExpressionNode): List<ExpressionStrictureIdentifierCounter> {
    if (expressionNode.value == "-") {
        return getMultipliersFromNode(expressionNode.children.first())
    } else if (expressionNode.value == "*") {
        val result = mutableListOf<ExpressionStrictureIdentifierCounter>()
        for (child in expressionNode.children) {
            val childIndex = result.indexOfFirst { child.expressionStrictureIdentifier == it.expressionStrictureIdentifier }
            if (childIndex < 0) {
                result.add(ExpressionStrictureIdentifierCounter(child.expressionStrictureIdentifier!!))
            } else {
                result[childIndex].count++
            }
        }
        return result
    } else {
        return listOf(ExpressionStrictureIdentifierCounter(expressionNode.expressionStrictureIdentifier!!))
    }
}

fun handleAdditiveNodeAsReductionPart(substitutionSelectionData: SubstitutionSelectionData, expressionNode: ExpressionNode, multipliers: List<ExpressionStrictureIdentifierCounter>, sumNode: ExpressionNode, prodNode: ExpressionNode?, hasMinus: Boolean = false) {
    if (expressionNode.value == "-") {
        handleAdditiveNodeAsReductionPart(substitutionSelectionData, expressionNode.children.first(), multipliers, sumNode, prodNode, hasMinus xor true)
    } else if (expressionNode.value == "*") {
        val multipliersCopy = mutableListOf<ExpressionStrictureIdentifierCounter>()
        for (multiplier in multipliers) {
            multipliersCopy.add(ExpressionStrictureIdentifierCounter(multiplier.expressionStrictureIdentifier, multiplier.count))
        }
        val sumProdNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
        for (child in expressionNode.children) {
            val mul = multipliersCopy.firstOrNull { it.expressionStrictureIdentifier == child.expressionStrictureIdentifier }
            if (mul != null && mul.count >= 1) {
                mul.count--
                prodNode?.addChild(child.clone())
            } else {
                if (hasMinus) {
                    val minusNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("-", -1)
                    minusNode.addChild(child.clone())
                    sumProdNode.addChild(minusNode)
                } else {
                    sumProdNode.addChild(child.clone())
                }
            }
        }
        if (sumProdNode.children.size == 1) {
            sumNode.addChild(sumProdNode.children.first())
        } else if (sumProdNode.children.size > 1) {
            sumNode.addChild(sumProdNode)
        }
    } else {
        sumNode.addChild(ExpressionNode(NodeType.VARIABLE, "1"))
        prodNode?.addChild(expressionNode.clone())
    }
}

fun generateOpeningBracketsSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                         simplifyNotSelectedTopArguments: Boolean = false,
                                         withReadyApplicationResult: Boolean = false,
                                         fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder != null && substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.size == 2) {
        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "*" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().value == "+" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().children.size > 1) {
            // all from fist X all from last transformation
            val sumNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1)
            val lChildren = if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().value == "+") {
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().children
            } else {
                listOf(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first())
            }
            val rChildren = if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().value == "+") {
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().children
            } else {
                listOf(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last())
            }
            addSumOpeningBracketsResult(lChildren, rChildren, substitutionSelectionData, sumNode, simplifyNotSelectedTopArguments, result)
        }

        if ((substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "*" || substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "/") &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().value == "+" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().children.size > 1) {
            // all from fist X last transformation
            val sumNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1)
            val lChildren = if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().value == "+") {
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().children
            } else {
                listOf(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first())
            }
            val rChildren = listOf(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last())
            addSumOpeningBracketsResult(lChildren, rChildren, substitutionSelectionData, sumNode, simplifyNotSelectedTopArguments, result)
        }

        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "^" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().value == "*" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().children.size > 1) {
            val prodNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
            for (lChild in substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().children) {
                val powNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1)
                powNode.addChild(lChild.clone())
                powNode.addChild(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().clone())
                prodNode.addChild(powNode)
            }
            addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                    prodNode,
                    result,
                    ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(prodNode)), "OpeningBrackets", 20)
        }

        if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value == "^" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().value == "+" &&
                substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().children.size > 1) {
            val prodNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("*", -1)
            for (rChild in substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.last().children) {
                val powNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("^", -1)
                powNode.addChild(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.children.first().clone())
                if (rChild.value == "-") {
                    powNode.addChild(substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("+", -1))
                    powNode.children.last().addChild(rChild.clone())
                } else {
                    powNode.addChild(rChild.clone())
                }
                prodNode.addChild(powNode)
            }
            addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                    prodNode,
                    result,
                    ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(prodNode)), "OpeningBrackets", 20)
        }
    }
    return result
}

private fun addSumOpeningBracketsResult(lChildren: List<ExpressionNode>, rChildren: List<ExpressionNode>, substitutionSelectionData: SubstitutionSelectionData, sumNode: ExpressionNode, simplifyNotSelectedTopArguments: Boolean, result: MutableList<SubstitutionApplication>) {
    for (lChild in lChildren) {
        for (rChild in rChildren) {
            val productionNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder!!.value, -1)
            var needMinus = false
            val lMul = if (lChild.value == "-") {
                needMinus = needMinus xor true
                lChild.children.first()
            } else lChild
            val rMul = if (rChild.value == "-") {
                needMinus = needMinus xor true
                rChild.children.first()
            } else rChild
            productionNode.addChild(lMul.clone())
            productionNode.addChild(rMul.clone())
            if (needMinus) {
                val minusNode = substitutionSelectionData.compiledConfiguration.createExpressionFunctionNode("-", -1)
                minusNode.addChild(productionNode)
                sumNode.addChild(minusNode)
            } else {
                sumNode.addChild(productionNode)
            }
        }
    }
    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
            sumNode,
            result,
            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(sumNode)), "OpeningBrackets", 20)
}


fun generateSimpleComputationSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                           simplifyNotSelectedTopArguments: Boolean = false,
                                           withReadyApplicationResult: Boolean = false,
                                           fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    if (substitutionSelectionData.compiledConfiguration.simpleComputationRuleParams.isIncluded &&
            substitutionSelectionData.selectedSubtreeTopArguments != null && !substitutionSelectionData.selectedSubtreeTopArguments!!.isNumberValue() &&
            substitutionSelectionData.selectedSubtreeTopArguments!!.calcComplexity() <= substitutionSelectionData.compiledConfiguration.simpleComputationRuleParams.maxCalcComplexity &&
            substitutionSelectionData.selectedSubtreeTopArguments!!.getContainedFunctions().subtract(substitutionSelectionData.compiledConfiguration.simpleComputationRuleParams.operationsMap.keys).isEmpty() &&
            substitutionSelectionData.selectedSubtreeTopArguments!!.getContainedVariables().isEmpty()) {
        val computed = substitutionSelectionData.selectedSubtreeTopArguments!!.computeNodeIfSimple(substitutionSelectionData.compiledConfiguration.simpleComputationRuleParams)
                ?: return result
        val computedNode = substitutionSelectionData.compiledConfiguration.createExpressionVariableNode(computed)
        addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                computedNode,
                result,
                ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.selectedSubtreeTopArguments!!.clone()), addRootNodeToExpression(computedNode)), "SimpleComputation", 10)
    }
    return result
}


fun generateNumberTransformationSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                              simplifyNotSelectedTopArguments: Boolean = false,
                                              withReadyApplicationResult: Boolean = false,
                                              fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    val currentValue = substitutionSelectionData.topOfSelection?.value?.toDoubleOrNull() ?: return result

    // x ~> a*b or a^b
    val intCurrentValue = currentValue.toInt()
    if (1 < intCurrentValue && intCurrentValue < 145 && (intCurrentValue - currentValue).toReal().additivelyEqualToZero()) {
        val sqrtValue = sqrt(currentValue).toInt()
        for (i in 2..sqrtValue) {
            val div = intCurrentValue / i
            if (intCurrentValue == i * div) {
                val mulTreeNode = ExpressionNode(NodeType.FUNCTION, "*")
                mulTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, i.toString()))
                mulTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, div.toString()))
                addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                        mulTreeNode,
                        result,
                        ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.topOfSelection!!.clone()), addRootNodeToExpression(mulTreeNode)), "NumberTransformation", 50)
            }
        }

        val maxPowValue = log2(currentValue).toInt()
        for (i in 2..maxPowValue) {
            val base = currentValue.pow(1.0 / i.toDouble())
            val baseInt = base.toInt()
            if ((baseInt - base).toReal().additivelyEqualToZero() && intCurrentValue == base.pow(i).toInt()) {
                val degTreeNode = ExpressionNode(NodeType.FUNCTION, "^")
                degTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, baseInt.toString()))
                degTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, i.toString()))
                addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                        degTreeNode,
                        result,
                        ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.topOfSelection!!.clone()), addRootNodeToExpression(degTreeNode)), "NumberTransformation", 50)
            }
        }
    }


    //x not in Z ~> x * 10^a / 10^a , where x * 10^a in Z
    val xTenPow = tenPowToMakeZ(currentValue, substitutionSelectionData.compiledConfiguration.simpleComputationRuleParams)
    if (0 < xTenPow && xTenPow < substitutionSelectionData.compiledConfiguration.simpleComputationRuleParams.maxTenPowIterations) {
        val divTreeNode = ExpressionNode(NodeType.FUNCTION, "/")
        val multiplier = 10.0.pow(xTenPow)
        divTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, (currentValue * multiplier).toShortString()))
        divTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, multiplier.toShortString()))
        addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
                divTreeNode,
                result,
                ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.topOfSelection!!.clone()), addRootNodeToExpression(divTreeNode)), "NumberTransformation", 60)
    }

    //x ~> (x-1) + 1
    val plusTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
    plusTreeNode.addChild(substitutionSelectionData.compiledConfiguration.createExpressionVariableNode(currentValue - 1))
    plusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
            plusTreeNode,
            result,
            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.topOfSelection!!.clone()), addRootNodeToExpression(plusTreeNode)), "NumberTransformation", 70)

    //x ~> (x+1) - 1
    val minusTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
    minusTreeNode.addChild(substitutionSelectionData.compiledConfiguration.createExpressionVariableNode(currentValue + 1))
    minusTreeNode.addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    minusTreeNode.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments,
            minusTreeNode,
            result,
            ExpressionSubstitution(addRootNodeToExpression(substitutionSelectionData.topOfSelection!!.clone()), addRootNodeToExpression(minusTreeNode)), "NumberTransformation", 70)
    return result
}


fun generateComplicatingExtensionSubstitutions(substitutionSelectionData: SubstitutionSelectionData,
                                               simplifyNotSelectedTopArguments: Boolean = false,
                                               withReadyApplicationResult: Boolean = false,
                                               fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    if (substitutionSelectionData.selectedNodes.size == 2) {
        val applicationPlace = substitutionSelectionData.selectedNodes.first()
        val applicationPlaceParent = applicationPlace.parent ?: return result
        val applicationPlaceIndex = applicationPlaceParent.children.indexOf(applicationPlace)

        val applicationObject = substitutionSelectionData.selectedNodes.last()

        //applicationPlace ~> applicationPlace + applicationObject - applicationObject
        val additiveTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
        additiveTreeNode.addChild(applicationPlace.clone())
        additiveTreeNode.addChild(applicationObject.clone())
        additiveTreeNode.addChild(ExpressionNode(NodeType.FUNCTION, "-"))
        additiveTreeNode.children.last().addChild(applicationObject.clone())
        val additiveSubstitution = ExpressionSubstitution(addRootNodeToExpression(applicationPlace.clone()), addRootNodeToExpression(additiveTreeNode))
        applicationPlaceParent.setChildOnPosition(additiveTreeNode, applicationPlaceIndex)
        substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(substitutionSelectionData.topOfSelection!!.clone(), substitutionSelectionData.topOfSelectionIndex)
        result.add(SubstitutionApplication(
                additiveSubstitution,
                substitutionSelectionData.originalExpression,
                applicationPlace,
                substitutionSelectionData.expressionToTransform.clone().apply {
                    normalizeExpressionToUsualForm(this)
                }, //because this code performs lots times for lots of substitutions
                additiveTreeNode,
                "ComplicatingExtension", 60
        ))

        //applicationPlace ~> (applicationPlace * applicationObject) / applicationObject
        val multiplicativeTreeNode = ExpressionNode(NodeType.FUNCTION, "/")
        multiplicativeTreeNode.addChild(ExpressionNode(NodeType.FUNCTION, "*"))
        multiplicativeTreeNode.children.last().addChild(applicationPlace.clone())
        multiplicativeTreeNode.children.last().addChild(applicationObject.clone())
        multiplicativeTreeNode.addChild(applicationObject.clone())
        val multiplicativeSubstitution = ExpressionSubstitution(addRootNodeToExpression(applicationPlace.clone()), addRootNodeToExpression(multiplicativeTreeNode))
        applicationPlaceParent.setChildOnPosition(multiplicativeTreeNode, applicationPlaceIndex)
        substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(substitutionSelectionData.topOfSelection!!.clone(), substitutionSelectionData.topOfSelectionIndex)
        result.add(SubstitutionApplication(
                multiplicativeSubstitution,
                substitutionSelectionData.originalExpression,
                applicationPlace,
                substitutionSelectionData.expressionToTransform.clone().apply {
                    normalizeExpressionToUsualForm(this)
                }, //because this code performs lots times for lots of substitutions
                multiplicativeTreeNode,
                "ComplicatingExtension", 60
        ))
    }
    return result
}


fun applySubstitution(substitutionSelectionData: SubstitutionSelectionData,
                      expressionSubstitution: ExpressionSubstitution,
                      simplifyNotSelectedTopArguments: Boolean = false): ExpressionNode? {
    val result = mutableListOf<SubstitutionApplication>()
    val substitutionInstance = checkLeftCondition(substitutionSelectionData, expressionSubstitution, false)
            ?: return null
    if (substitutionInstance.isApplicable) {
        val applicationToSelectedPartResult = expressionSubstitution.applyRight(substitutionInstance) ?: return null
        addApplicationToResults(true, substitutionSelectionData, simplifyNotSelectedTopArguments, applicationToSelectedPartResult, result, expressionSubstitution, expressionSubstitution.type
                ?: "", expressionSubstitution.priority ?: 100)
        return result.first().resultExpression
    }
    return null
}

private fun checkLeftCondition(substitutionSelectionData: SubstitutionSelectionData, expressionSubstitution: ExpressionSubstitution, fastestAppropriateVersion: Boolean = true): SubstitutionInstance? {
    var substitutionInstance = if (substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder != null && !expressionSubstitution.changeOnlyOrder) {
        expressionSubstitution.checkLeftCondition(substitutionSelectionData.selectedSubtreeTopArgumentsInSelectionOrder
                ?: return null)
    } else null
    if (substitutionInstance == null || (!fastestAppropriateVersion && !substitutionInstance.isApplicable)) {
        substitutionInstance = expressionSubstitution.checkLeftCondition(substitutionSelectionData.selectedSubtreeTopArguments
                ?: return null)
    }
    return substitutionInstance
}

private fun addApplicationToResults( //default fastest line is to run with 'simplifyNotSelectedTopArguments' = 'true' && 'onSameBracketLevel' = 'false'
        withReadyApplicationResult: Boolean,
        substitutionSelectionData: SubstitutionSelectionData,
        simplifyNotSelectedTopArguments: Boolean,
        applicationToSelectedPartResult: ExpressionNode,
        result: MutableList<SubstitutionApplication>,
        transformation: ExpressionSubstitution,
        substitutionType: String,
        priority: Int,
        onSameBracketLevel: Boolean = false) {
    if (withReadyApplicationResult) {
        if (substitutionSelectionData.notSelectedSubtreeTopArguments != null) {
            val applicationResultNode = if (onSameBracketLevel && substitutionSelectionData.notSelectedSubtreeTopArguments!!.value == applicationToSelectedPartResult.value) {
                applicationToSelectedPartResult.clone().apply {
                    if (simplifyNotSelectedTopArguments) {
                        for (i in 1..substitutionSelectionData.notSelectedSubtreeTopArguments!!.children.lastIndex) {
                            addChild(substitutionSelectionData.notSelectedSubtreeTopArguments!!.children[i])
                        }
                    } else {
                        for (child in substitutionSelectionData.notSelectedSubtreeTopOriginalTree!!.children) {
                            addChild(child)
                        }
                    }
                }
            } else if (simplifyNotSelectedTopArguments) {
                substitutionSelectionData.notSelectedSubtreeTopArguments!!.apply { setChildOnPosition(applicationToSelectedPartResult, 0) }
            } else {
                val commutativeOperationNode = substitutionSelectionData.notSelectedSubtreeTopArguments!!.copy()
                commutativeOperationNode.addChild(applicationToSelectedPartResult)
                for (child in substitutionSelectionData.notSelectedSubtreeTopOriginalTree!!.children) {
                    commutativeOperationNode.addChild(child)
                }
                commutativeOperationNode
            }
            if (substitutionSelectionData.topOfSelectionParent != null) {
                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationResultNode, substitutionSelectionData.topOfSelectionIndex)
            }
        } else {
            if (substitutionSelectionData.topOfSelectionParent != null) {
                substitutionSelectionData.topOfSelectionParent!!.setChildOnPosition(applicationToSelectedPartResult, substitutionSelectionData.topOfSelectionIndex)
            }
        }
    }
    result.add(SubstitutionApplication(
            transformation,
            substitutionSelectionData.originalExpression,
            substitutionSelectionData.selectedSubtreeTopArguments!!,
            if (withReadyApplicationResult) {
                substitutionSelectionData.expressionToTransform.clone().apply {
                    normalizeExpressionToUsualForm(this)
                } //because this code performs lots times for lots of substitutions
            } else {
                ExpressionNode(NodeType.EMPTY, "To get application result use argument 'withReadyApplicationResult' = 'true'")
            },
            applicationToSelectedPartResult,
            substitutionType, priority
    ))
}

fun generateFormIndependentSubstitutionsBySelectedNodes(substitutionSelectionData: SubstitutionSelectionData, simplifyNotSelectedTopArguments: Boolean = false, withReadyApplicationResult: Boolean = false, fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    result.addAll(generateSimpleComputationSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    result.addAll(generateOpeningBracketsSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    result.addAll(generatePermutationSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    result.addAll(generateNumberTransformationSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    result.addAll(generateComplicatingExtensionSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    return result
}

fun generateFormDependentSubstitutionsBySelectedNodes(substitutionSelectionData: SubstitutionSelectionData, simplifyNotSelectedTopArguments: Boolean = false, withReadyApplicationResult: Boolean = false, fastestAppropriateVersion: Boolean = false, withExtendingSubstitutions: Boolean = true): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    result.addAll(generateReduceArithmeticSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    result.addAll(generateReduceFractionSubstitutions(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))

    if (substitutionSelectionData.topOfSelection?.value == "/" && substitutionSelectionData.selectedNodeIds.size == 2) {
        val divisionSubstitutionSelectionData = SubstitutionSelectionData(substitutionSelectionData.originalExpression, listOf(substitutionSelectionData.topOfSelection!!.nodeId), substitutionSelectionData.compiledConfiguration)
        fillSubstitutionSelectionData(divisionSubstitutionSelectionData)
        result.addAll(findConfiguredSubstitutionsApplications(divisionSubstitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion, withExtendingSubstitutions))
    } else {
        result.addAll(findConfiguredSubstitutionsApplications(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion, withExtendingSubstitutions))
    }
    return result
}

fun generateSubstitutionsBySelectedNodes(substitutionSelectionData: SubstitutionSelectionData, simplifyNotSelectedTopArguments: Boolean = false, withReadyApplicationResult: Boolean = false, fastestAppropriateVersion: Boolean = false): List<SubstitutionApplication> {
    val result = mutableListOf<SubstitutionApplication>()
    fillSubstitutionSelectionData(substitutionSelectionData)
    result.addAll(generateFormIndependentSubstitutionsBySelectedNodes(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion))
    result.addAll(generateFormDependentSubstitutionsBySelectedNodes(substitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion, withExtendingSubstitutions=true))
    result.sortBy { it.priority }

    if (substitutionSelectionData.selectedNodeIds.size > 1) {
        val resultApterAdditionalSubstitutions = mutableListOf<SubstitutionApplication>()
        for (rule in substitutionSelectionData.compiledConfiguration.compiledExpressionSimpleAdditionalTreeTransformationRules) {
            val expression = substitutionSelectionData.originalExpression.clone()
            val selectedNodes: List<ExpressionNode> = expression.nodeIdsToNodeLinksInSameOrder(substitutionSelectionData.selectedNodeIds, nodeIdsPositionsMap(substitutionSelectionData.selectedNodeIds))
            var happenedReplacementsCount = 0
            for (selectedNode in selectedNodes) {
                if (selectedNode.value != rule.right.children.first().value) {
                    val selectedNodeParent = selectedNode.parent ?: continue
                    val selectedNodeIndex = selectedNodeParent.children.indexOf(selectedNode)
                    val applicationResult = rule.checkAndApply(selectedNode)
                    if (applicationResult != null) {
                        selectedNodeParent.setChildOnPosition(applicationResult, selectedNodeIndex)
                        happenedReplacementsCount++
                    }
                }
            }

            if (0 < happenedReplacementsCount && happenedReplacementsCount < selectedNodes.size) {
                expression.computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot(Int.MAX_VALUE / 2, 0, false)
                val additionalSubstitutionSelectionData = SubstitutionSelectionData(expression, substitutionSelectionData.selectedNodeIds, substitutionSelectionData.compiledConfiguration)
                fillSubstitutionSelectionData(additionalSubstitutionSelectionData)
                result.addAll(generateFormDependentSubstitutionsBySelectedNodes(additionalSubstitutionSelectionData, simplifyNotSelectedTopArguments, withReadyApplicationResult, fastestAppropriateVersion, withExtendingSubstitutions = false))
            }
        }
        resultApterAdditionalSubstitutions.sortBy { it.priority }
        result.addAll(resultApterAdditionalSubstitutions)
    }

    return result
}