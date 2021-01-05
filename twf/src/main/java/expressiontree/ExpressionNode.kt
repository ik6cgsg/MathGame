package expressiontree

import config.*
import platformdependent.toShortString
import standartlibextensions.*
import kotlin.math.max

enum class NodeType {
    FUNCTION,
    VARIABLE,
    EMPTY,
    ERROR
}

data class NodeValue(
        var exactNumber: Int
)

data class ExpressionStrictureIdentifier (
        var originalOrderIdentifier: String,
        var commutativeSortedIdentifier: String
) {
    override fun equals(other: Any?): Boolean {
        if (other is ExpressionStrictureIdentifier)
            return originalOrderIdentifier == other.originalOrderIdentifier || commutativeSortedIdentifier == other.commutativeSortedIdentifier
        else return false
    }

    override fun hashCode() = 1
}

data class ExpressionNode(
        //data
        var nodeType: NodeType,
        var value: String,
        var startPosition: Int = -1,
        var endPosition: Int = -1,
        var subValue: String = "", //value on bottom lines <msub>
        var parent: ExpressionNode? = null,
        var functionStringDefinition: FunctionStringDefinition? = null, //maybe ExpressionNode also should have string with original representation, not just link on definition kind
        var identifier: String = "",
        var nodeId: Int = -1,
        var distanceToRoot: Int = 0,
        var expressionStrictureIdentifier: ExpressionStrictureIdentifier? = null
) {
    var children: MutableList<ExpressionNode> = mutableListOf()

    //only facts variables:
    var rules: MutableList<ExpressionSubstitution> = mutableListOf() //correct for this and children levels transformation rules
    var parserMark: String = ""
    //todo(add fact chain type: '>', '=', '<', 'equality')
    var linkOnOriginalTreeNode: ExpressionNode? = null

    fun addChild(newNode: ExpressionNode) {
        children.add(newNode)
        newNode.parent = this
    }

    fun addChildOnPosition(newNode: ExpressionNode, position: Int) {
        children.add(position, newNode)
        newNode.parent = this
    }

    fun setChildOnPosition(newNode: ExpressionNode, position: Int) {
        children[position] = newNode
        newNode.parent = this
    }

    fun setVariable(value: String) {
        setVariableWithoutChildrenClearing(value)
        children.clear()
    }

    fun setVariableWithoutChildrenClearing(value: String) {
        nodeType = NodeType.VARIABLE
        this.value = value
        identifier = value
        functionStringDefinition = null
    }

    fun setNode(value: ExpressionNode) {
        nodeType = value.nodeType
        this.value = value.value
        identifier = value.identifier
        functionStringDefinition = value.functionStringDefinition
        children.clear()
        children = value.children
        subValue = value.subValue
        value.parent = this
    }

    fun isNumberValue() = value.isNotBlank() && (value.first().isNumberPart() || value == "π")

    fun getNodeValueString() = value

    fun getTopNode(): ExpressionNode {
        var result = this
        while (result.parent != null) {
            result = result.parent!!
        }
        return result
    }

    fun getChildNodesOnDepthOrWhileOperation(depth: Int, operationList: List<String>): List<ExpressionNode> {
        val result = mutableListOf<ExpressionNode>()
        if (depth < 1 && value !in operationList) return result
        for (child in children) {
            result.add(child)
            result.addAll(child.getChildNodesOnDepthOrWhileOperation(depth - 1, operationList))
        }
        return result
    }

    fun getChildNodesWhileOperation(operationList: List<String>): List<ExpressionNode> {
        val result = mutableListOf<ExpressionNode>()
        if (value !in operationList){
            result.add(this)
        } else {
            for (child in children) {
                result.addAll(child.getChildNodesWhileOperation(operationList))
            }
        }
        return result
    }

    fun resetNodeIds() {
        nodeId = -1
        for (child in children) {
            child.resetNodeIds()
        }
    }

    fun computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot(startId: Int = 0, startDistance: Int = 0, replaceCalculatedNodeIds: Boolean = true): Int {
        if (replaceCalculatedNodeIds || nodeId < 0) {
            nodeId = startId
        }
        var currentStartId = startId + 1
        distanceToRoot = startDistance
        for (child in children) {
            currentStartId = child.computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot(currentStartId, startDistance + 1, replaceCalculatedNodeIds)
        }
        return currentStartId
    }

    fun correctPositions() {
        for (child in children) {
            child.correctPositions()
            if (child.startPosition < startPosition) {
                startPosition = child.startPosition
            }
            if (child.endPosition > endPosition) {
                endPosition = child.endPosition
            }
        }
    }

    fun normalizeParentLinks() {
        for (child in children) {
            child.normalizeParentLinks()
            child.parent = this
        }
    }

    fun normalizeFunctionStringDefinitions(functionConfiguration: FunctionConfiguration) {
        functionStringDefinition = functionConfiguration.fastFindStringDefinitionByNameAndNumberOfArguments(
                value,
                children.size
        )
        for (child in children) {
            child.normalizeFunctionStringDefinitions(functionConfiguration)
        }
    }

    fun reduceExtraSigns(extraUnaryFunctions: Set<String>, exclusionChildFunctions: Set<String> = setOf()) {
        for (i in children.lastIndex downTo 0) {
            children[i].reduceExtraSigns(extraUnaryFunctions, exclusionChildFunctions)
            if ((children.size == 1 || value !in extraUnaryFunctions) && children[i].children.size == 1 && children[i].value in extraUnaryFunctions && children[i].children.first().value !in exclusionChildFunctions) {
                children[i] = children[i].children.first()
                children[i].parent = this
            }
        }
    }

    fun normilizeSubtructions(functionConfiguration: FunctionConfiguration) { //TODO: generalize because it depends on configuration
        for (i in children.lastIndex downTo 0) {
            children[i].normilizeSubtructions(functionConfiguration)
            if (children[i].nodeType == NodeType.VARIABLE && children[i].value.startsWith("-")) {
                val double = children[i].value.toDoubleOrNull()
                if (double != null && double < 0) {
                    if (value == "-") {
                        setVariable((-double).toString())
                    } else {
                        val valueNode = ExpressionNode(NodeType.VARIABLE, (-double).toShortString())
                        val minusNode = ExpressionNode(NodeType.FUNCTION, "-")
                        minusNode.addChild(valueNode)
                        if (value != "+") {
                            children[i] = minusNode
                            minusNode.parent = this
                        } else {
                            val plusNode = ExpressionNode(NodeType.FUNCTION, "+")
                            plusNode.addChild(minusNode)
                            children[i] = plusNode
                            plusNode.parent = this
                        }
                    }
                }
            }
            if (children[i].value == "-" && value != "+") {
                val plusParent = ExpressionNode(
                        NodeType.FUNCTION,
                        "+",
                        children[i].startPosition,
                        children[i].endPosition,
                        "",
                        this,
                        functionConfiguration.fastFindStringDefinitionByNameAndNumberOfArguments("+", 1)
                )
                plusParent.addChild(children[i])
                children[i] = plusParent
            }
        }
    }

    fun normalizeNullWeightCommutativeFunctions() {
        for (i in children.lastIndex downTo 0) {
            children[i].normalizeNullWeightCommutativeFunctions()
            if (children[i].children.size == 1 && children[i].functionStringDefinition?.function?.isCommutativeWithNullWeight == true) {
                children[i].children.first().parent = this
                children[i] = children[i].children.first()
            }
        }
    }

    fun fillStructureStringIdentifiers (getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }) {
        if (nodeType == NodeType.VARIABLE) {
            val v = getNodeValueString(this)
            expressionStrictureIdentifier = ExpressionStrictureIdentifier(v, v)
        } else {
            val start = value + "("
            expressionStrictureIdentifier = ExpressionStrictureIdentifier(start, start)
            for (child in children) {
                child.fillStructureStringIdentifiers(getNodeValueString)
            }
            expressionStrictureIdentifier!!.originalOrderIdentifier += children.joinToString (separator = ";") { it.expressionStrictureIdentifier!!.originalOrderIdentifier }
            expressionStrictureIdentifier!!.commutativeSortedIdentifier += if (functionStringDefinition?.function?.isCommutativeWithNullWeight == true) {
                children.sortedBy { it.expressionStrictureIdentifier!!.commutativeSortedIdentifier }.joinToString(separator = ";") { it.expressionStrictureIdentifier!!.commutativeSortedIdentifier }
            } else {
                children.joinToString(separator = ";") { it.expressionStrictureIdentifier!!.commutativeSortedIdentifier }
            }
            expressionStrictureIdentifier!!.originalOrderIdentifier += ")"
            expressionStrictureIdentifier!!.commutativeSortedIdentifier += ")"
        }
    }

    fun computeIdentifier(getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }): String {
        if (nodeType == NodeType.VARIABLE) {
            identifier = getNodeValueString(this)
        } else {
            identifier = value + "("
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.computeIdentifier(getNodeValueString)/* else child.identifier*/ + ";"
            }
            if (children.size > 0)
                identifier = identifier.substring(0, identifier.length - 1)
            identifier += ")"
        }
        return identifier
    }

    fun computeIdentifierWithPositions(getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }): String {
        if (nodeType == NodeType.VARIABLE) {
            identifier = getNodeValueString(this)
        } else {
            identifier = value + "("
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.computeIdentifierWithPositions(
                        getNodeValueString
                )/* else child.identifier*/ + ";"
            }
            if (children.size > 0)
                identifier = identifier.substring(0, identifier.length - 1)
            identifier += ")"
        }
        return identifier + "{$startPosition;$endPosition}"
    }

    fun toStringsWithPositions(
            getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() },
            offset: String = ""
    ): String {
        if (nodeType == NodeType.VARIABLE) {
            identifier = offset + getNodeValueString(this) + "  :  [$startPosition; $endPosition; $nodeId]\n"
        } else {
            identifier = offset + value + "  :  [$startPosition; $endPosition; $nodeId]\n"
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.toStringsWithPositions(
                        getNodeValueString,
                        offset + "  "
                )/* else child.identifier*/
            }
        }
        return identifier
    }

    fun toStringsWithNodeIds(getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }, offset: String = ""): String {
        if (nodeType == NodeType.VARIABLE) {
            identifier = offset + getNodeValueString(this) + "  :  [$nodeId]\n"
        } else {
            identifier = offset + value + "  :  [$nodeId]\n"
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.toStringsWithNodeIds(getNodeValueString, offset + "  ")/* else child.identifier*/
            }
        }
        return identifier
    }

    fun toPlainTextView(
            functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
            getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }
    ): String {
        var result = toPlainTextViewRec(functionConfiguration, getNodeValueString)
        if (result.first() == '(' && result.last() == ')') {
            var numberOfOpenBrackets = 1
            var currentIndex = 1
            while (currentIndex < result.length && numberOfOpenBrackets > 0) {
                if (result[currentIndex] == '(') {
                    numberOfOpenBrackets++
                } else if (result[currentIndex] == ')') {
                    numberOfOpenBrackets--
                }
                currentIndex++
            }
            if (currentIndex >= result.lastIndex) {
                result = result.substring(1, result.length - 1)
            }
        }
        return result
    }

    private fun toPlainTextViewRec(
            functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
            getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }
    ): String {
        var identifier = ""
        if (nodeType == NodeType.VARIABLE) {
            identifier = getNodeValueString(this)
        } else if (value == "" && children.size == 1) {
            identifier = children.first().toPlainTextViewRec(functionConfiguration)
        } else if (functionConfiguration.functionProperties.filter {
                    it.function == value &&
                            it.defaultStringDefinitionType != StringDefinitionType.FUNCTION
                }.isNotEmpty()) {
            val functionIdentifier = functionConfiguration.functionProperties.find { it.function == value }!!
            when (functionIdentifier.defaultStringDefinitionType) {
                StringDefinitionType.BINARY_OPERATION -> {
                    if (binaryOperationNeedBrackets()) {
                        identifier = "("
                    }
                    for (child in children) {
                        if (functionConfiguration.functionPropertiesByName.get(child.value + "_-1")?.mainFunction == functionIdentifier.function &&
                                functionConfiguration.functionPropertiesByName.get(child.value + "_-1")?.function != functionIdentifier.function
                        ) {
                            if (identifier.length > "(".length) {
                                identifier = identifier.substring(
                                        0,
                                        identifier.length - functionIdentifier.plainTextRepresentation.length
                                )
                            }
                            identifier += functionConfiguration.functionPropertiesByName.get(child.value + "_-1")!!.function
                            identifier += child.children.first()
                                    .toPlainTextViewRec(functionConfiguration) + functionIdentifier.plainTextRepresentation
                        } else {
                            identifier += child.toPlainTextViewRec(functionConfiguration) + functionIdentifier.plainTextRepresentation
                        }
                    }
                    if (children.size > 0) {
                        identifier =
                                identifier.substring(0, identifier.length - functionIdentifier.plainTextRepresentation.length)
                    }
                    if (binaryOperationNeedBrackets()) {
                        identifier += ")"
                    }
                }
                StringDefinitionType.UNARY_LEFT_OPERATION -> {
                    val childplainTextRepresentation = children.first().toPlainTextViewRec(functionConfiguration)
                    identifier =
                            functionIdentifier.plainTextRepresentation + if (childplainTextRepresentation.first().isSign(false)) {
                                "($childplainTextRepresentation)"
                            } else {
                                childplainTextRepresentation
                            }
                }
                StringDefinitionType.UNARY_RIGHT_OPERATION -> {
                    val childplainTextRepresentation = children.first().toPlainTextViewRec(functionConfiguration)
                    identifier = if (childplainTextRepresentation.first().isSign(false)) {
                        "($childplainTextRepresentation)"
                    } else {
                        childplainTextRepresentation
                    } + functionIdentifier.plainTextRepresentation
                }
            }
        } else {
            identifier = value + "("
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.toPlainTextViewRec()/* else child.identifier*/ + ","
            }
            if (children.size > 0)
                identifier = identifier.substring(0, identifier.length - 1)
            identifier += ")"
        }
        return identifier
    }

    fun toTexView(
            functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
            getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }
    ): String {
        var result = toTexViewRec(functionConfiguration, getNodeValueString)
        if (result.first() == '(' && result.last() == ')') {
            var numberOfOpenBrackets = 1
            var currentIndex = 1
            while (currentIndex < result.length && numberOfOpenBrackets > 0) {
                if (result[currentIndex] == '(') {
                    numberOfOpenBrackets++
                } else if (result[currentIndex] == ')') {
                    numberOfOpenBrackets--
                }
                currentIndex++
            }
            if (currentIndex >= result.lastIndex) {
                result = result.substring(texOpenBracket.length, result.length - texCloseBracket.length)
            }
        }
        return result
    }

    private fun toTexViewRec(
            functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
            getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }
    ): String {
        var identifier = ""
        if (nodeType == NodeType.VARIABLE) {
            identifier = getNodeValueString(this)
        } else if (value == "" && children.size == 1) {
            identifier = children.first().toTexViewRec(functionConfiguration)
        } else {
            val functionIdentifier = functionConfiguration.functionProperties.find { it.function == value }
            if (functionIdentifier != null && functionIdentifier.texStringDefinitionType != StringDefinitionType.FUNCTION) {
                when (functionIdentifier.texStringDefinitionType) {
                    StringDefinitionType.BINARY_OPERATION -> {
                        if (binaryOperationNeedBrackets()) {
                            identifier = texOpenBracket
                        }
                        if (value == "/" && children.size == 2) {
                            identifier += "\\frac{" + children.first().toTexViewRec(functionConfiguration) + "}{" + children.last().toTexViewRec(functionConfiguration) + "}"
                        } else if (value == "log" && children.size == 2) {
                            identifier += "\\log _{" + children.last().toTexViewRec(functionConfiguration) + "}{" + children.first().toTexViewRec(functionConfiguration) + "}"
                        } else if (value == "sqrt" && children.size == 1) {
                            identifier += "\\sqrt{" + children.first().toTexViewRec(functionConfiguration) + "}"
                        } else if (value == "^" && children.size >= 2) {
                            for (child in children) {
                                identifier += "{" + child.toTexViewRec(functionConfiguration) + "^"
                            }
                            identifier = identifier.substring(0, identifier.length - 1) + "}".repeat(children.size)
                        } else {
                            for (child in children) {
                                if (functionConfiguration.functionPropertiesByName.get(child.value + "_-1")?.mainFunction == functionIdentifier.function &&
                                        functionConfiguration.functionPropertiesByName.get(child.value + "_-1")?.function != functionIdentifier.function
                                ) {
                                    if (identifier.length > "\\left(".length) {
                                        identifier = identifier.substring(
                                                0,
                                                identifier.length - (functionIdentifier.texRepresentation.length + 2)
                                        )
                                    }
                                    identifier += functionConfiguration.functionPropertiesByName.get(child.value + "_-1")!!.function
                                    identifier += child.children.first()
                                            .toTexViewRec(functionConfiguration) + " ${functionIdentifier.texRepresentation} "
                                } else {
                                    identifier += child.toTexViewRec(functionConfiguration) + " ${functionIdentifier.texRepresentation} "
                                }
                            }
                            if (children.size > 0) {
                                identifier =
                                        identifier.substring(0, identifier.length - (functionIdentifier.texRepresentation.length + 2))
                            }
                        }
                        if (binaryOperationNeedBrackets()) {
                            identifier += texCloseBracket
                        }
                    }
                    StringDefinitionType.UNARY_LEFT_OPERATION -> {
                        val childTexRepresentation = children.first().toTexViewRec(functionConfiguration)
                        identifier =
                                " ${functionIdentifier.texRepresentation} " + if (childTexRepresentation.first().isSign(false)) {
                                    "($childTexRepresentation)"
                                } else {
                                    childTexRepresentation
                                }
                    }
                    StringDefinitionType.UNARY_RIGHT_OPERATION -> {
                        val childTexRepresentation = children.first().toTexViewRec(functionConfiguration)
                        identifier = if (childTexRepresentation.first().isSign(false)) {
                            "($childTexRepresentation)"
                        } else {
                            childTexRepresentation
                        } + " ${functionIdentifier.texRepresentation} "
                    }
                }
            } else {
                identifier = value + texOpenBracket
                for (child in children) {
                    identifier += /*if (child.identifier.isEmpty()) */child.toTexViewRec()/* else child.identifier*/ + ","
                }
                if (children.size > 0)
                    identifier = identifier.substring(0, identifier.length - 1)
                identifier += texCloseBracket
            }
        }
        return identifier
    }

    private fun binaryOperationNeedBrackets(bracketThresholdAdditive: Double = 0.2) =
            parent?.functionStringDefinition?.function?.defaultStringDefinitionType != StringDefinitionType.FUNCTION && (
                    parent?.functionStringDefinition?.function?.defaultStringDefinitionType == StringDefinitionType.UNARY_RIGHT_OPERATION ||
                            (parent?.functionStringDefinition?.function?.defaultStringDefinitionType == StringDefinitionType.UNARY_LEFT_OPERATION &&
                                    parent?.functionStringDefinition?.function?.function == parent?.functionStringDefinition?.function?.mainFunction) ||
                            (bracketThresholdAdditive + (parent?.functionStringDefinition?.function?.priority
                                    ?: 0.0)) >= functionStringDefinition?.function?.priority ?: 0.0 ||
                            (functionStringDefinition?.function?.defaultStringDefinitionType == StringDefinitionType.FUNCTION && nodeId != parent?.children?.last()?.nodeId &&
                                    children.first().children.isNotEmpty()))

    fun isNodeValueEquals(expressionNode: ExpressionNode) = getNodeValueString() == expressionNode.getNodeValueString()
    fun isNodeSubtreeIdentifiersEquals(expressionNode: ExpressionNode) =
            computeIdentifier() == expressionNode.computeIdentifier()

    fun sortChildrenAscendingIdentifiers() {
        if (nodeType == NodeType.FUNCTION && (functionStringDefinition?.function?.isCommutativeWithNullWeight == true))
            children.sortBy({ it.identifier })
    }

    fun sortChildrenAscendingNodeIds() {
        for (child in children) {
            child.sortChildrenAscendingNodeIds()
        }
        children.sortBy({ it.nodeId })
    }

    fun sortChildrenByNodeIdsOrder(nodeIdsPositionsMap: Map<Int, Int>) {
        for (child in children) {
            child.sortChildrenByNodeIdsOrder(nodeIdsPositionsMap)
        }
        if (functionStringDefinition?.function?.isCommutativeWithNullWeight == true &&
                children.all { nodeIdsPositionsMap.containsKey(it.nodeId) }) {
            children.sortWith(Comparator { o1, o2 -> (nodeIdsPositionsMap[o1.nodeId]!! - nodeIdsPositionsMap[o2.nodeId]!!) })
        }
    }

    fun sortChildrenForExpressionSubstitutionComparison(): ExpressionNode {
        for (child in children) {
            child.sortChildrenForExpressionSubstitutionComparison()
        }
        if (nodeType == NodeType.FUNCTION && (functionStringDefinition?.function?.isCommutativeWithNullWeight == true))
            children.sortBy {
                (
                        if (it.isNumberValue())
                            "1${it.identifier}"
                        else if (it.nodeType == NodeType.FUNCTION)
                            "2${it.value}"
                        else
                            "3"
                        )
            } //first: sorted numbers; than functions and variables in the original order //TODO: it leads to the problem of wrong matching if order is changed
        return this
    }

    fun cloneWithSortingChildrenForExpressionSubstitutionComparison() = clone()
            .sortChildrenForExpressionSubstitutionComparison()

    fun getCountOfNodes(): Int = 1 + children.sumBy { it.getCountOfNodes() }

    fun getMaxMinNumberOfPointsForEquality(): Int {
        if (nodeType == NodeType.VARIABLE) {
            return 1
        } else {
            val maxMinNumberOfPointsForEquality = children.map { it.getMaxMinNumberOfPointsForEquality() }.max() ?: 1
            return max(
                    maxMinNumberOfPointsForEquality, functionStringDefinition?.function?.minNumberOfPointsForEquality
                    ?: 1
            )
        }
    }

    fun containsVariables(): Boolean {
        if (nodeType == NodeType.VARIABLE) {
            if (value.toDoubleOrNull() == null)
                return true
        } else for (child in children) {
            if (child.containsVariables())
                return true
        }
        return false
    }

    fun getVariableNames(): Set<String> {
        var result = mutableSetOf<String>()
        if (nodeType == NodeType.VARIABLE) {
            if (value.toDoubleOrNull() == null)
                result.add(value)
        } else for (child in children)
            result.addAll(child.getVariableNames())
        return result
    }

    fun containsFunctions(): Boolean {
        if (nodeType == NodeType.FUNCTION && value != "") {
            return false
        } else for (child in children) {
            if (child.containsFunctions()) {
                return false
            }
        }
        return true
    }

    fun containsFunction(name: String, numberOfArgs: Int): Boolean {
        if (nodeType == NodeType.FUNCTION && value == name && children.size == numberOfArgs) {
            return true
        } else for (child in children) {
            if (child.containsFunction(name, numberOfArgs)) {
                return true
            }
        }
        return false
    }

    fun isBoolExpression(boolFunctions: Set<String>) = getContainedFunctions().intersect(boolFunctions).isNotEmpty()

    fun getContainedFunctions(): Set<String> {
        val result = mutableSetOf<String>()
        if (!children.isEmpty()) {
            if (value.isNotBlank()) {
                result.add(value)
            }
            for (child in children)
                result.addAll(child.getContainedFunctions())
        }
        return result
    }

    fun topOperationIsPossibleMainFunction (topOperation: String) = functionStringDefinition?.function?.notObligateMainFunction() == topOperation || (linkOnOriginalTreeNode?.children?.size == 1 // Assume that operation division '/' always has 2 arguments
            && functionStringDefinition?.function?.numberOfArguments == -1 && functionStringDefinition?.function?.mainFunctionIsCommutativeWithNullWeight == true)

    fun getContainedChildOperationNodeIds(topOperation: String): Set<Int> {
        val result = mutableSetOf<Int>()
        if (children.isEmpty() || !topOperationIsPossibleMainFunction(topOperation)) {
            result.add(nodeId)
        } else for (child in children)
            result.addAll(child.getContainedChildOperationNodeIds(topOperation))
        return result
    }

    fun getAllChildrenNodeIds(): Set<Int> {
        val result = mutableSetOf<Int>()
        for (child in children) {
            result.add(child.nodeId)
            result.addAll(child.getAllChildrenNodeIds())
        }
        return result
    }

    fun allParentsMainFunctionIs(topOperation: String): Boolean {
        if (!topOperationIsPossibleMainFunction(topOperation)) {
            return children.isEmpty() // all children parents main function is not 'topOperation'
        } else {
            for (child in children) {
                if (!child.allParentsMainFunctionIs(topOperation)) {
                    return false
                }
            }
        }
        return true
    }

    fun listWhichParentsFunctionIs(topOperation: String): List<ExpressionNode> {
        if (value != topOperation) {
            return if (children.isEmpty()) listOf(linkOnOriginalTreeNode!!) else emptyList()
        } else {
            val result = mutableListOf<ExpressionNode>()
            for (child in children) {
                result.addAll(child.listWhichParentsFunctionIs(topOperation))
            }
            return result
        }
    }

    fun getContainedVariables(): Set<String> {
        val result = mutableSetOf<String>()
        if (children.isEmpty()) {
            if (value != "π" && value.toDoubleOrNull() == null) {
                result.add(value)
            }
        } else for (child in children)
            result.addAll(child.getContainedVariables())
        return result
    }

    fun getContainedVariables(variables: Set<String>): Set<String> {
        val result = mutableSetOf<String>()
        if (children.isEmpty()) {
            if (value.toDoubleOrNull() == null) {
                if (variables.contains(value)) {
                    result.add(value)
                }
            }
        } else for (child in children)
            result.addAll(child.getContainedVariables(variables))
        return result
    }

    fun getNotContainedVariables(variables: Set<String>): Set<String> {
        val result = mutableSetOf<String>()
        if (children.isEmpty()) {
            if (value.toDoubleOrNull() == null) {
                if (!variables.contains(value)) {
                    result.add(value)
                }
            }
        } else for (child in children)
            result.addAll(child.getNotContainedVariables(variables))
        return result
    }

    fun canContainDivisions(): Boolean {
        if (value == "/") {
            return true
        }
        if (value == "^" && children.size > 1) {
            for (i in 2..children.lastIndex) {
                if (children[i].nodeType == NodeType.FUNCTION)
                    return true
            }
        }
        for (child in children)
            return child.canContainDivisions()
        return false
    }

    fun getDepth(): Int {
        var result = 1
        for (child in children)
            result = maxOf(result, child.getDepth() + 1)
        return result
    }

    fun getMaxConstant(): Double {
        var result = 0.0
        if (children.isEmpty()) {
            result = (value.toDoubleOrNull() ?: 0.0).abs()
        }
        for (child in children)
            result = maxOf(result, child.getMaxConstant().abs())
        return result
    }

    override fun toString() = computeIdentifier()
    fun toStringWithPositions() = computeIdentifierWithPositions()

    fun normalizeSubTree(
            currentDeep: Int = 0,
            nameArgsMap: MutableMap<String, String> = mutableMapOf(),
            sorted: Boolean
    ): ExpressionNode {
        if (nodeType == NodeType.FUNCTION) {
            val numberOfDefinitionArguments = functionStringDefinition?.function?.numberOfDefinitionArguments ?: 0
            val childrenNameArgs = mutableListOf<String>()
            val previousNameArgsMap: MutableMap<String, String> = mutableMapOf()
            for (i in 0 until numberOfDefinitionArguments) {
                childrenNameArgs.add("" + children[i].value)
                val currentNameArgMap = nameArgsMap.get(children[i].value)
                if (currentNameArgMap != null) previousNameArgsMap.put(children[i].value, currentNameArgMap)
                nameArgsMap.put(children[i].value, "sys_twf_name_var_value_${currentDeep}_$i")
            }
            for (child in children) {
                child.normalizeSubTree(currentDeep + 1, nameArgsMap, sorted)
            }
            if (sorted) {
                sortChildrenAscendingIdentifiers()
            }
            computeIdentifier()
            for (name in childrenNameArgs) {
                nameArgsMap.remove(name)
            }
            nameArgsMap.putAll(previousNameArgsMap)
        } else {
            val newValue = nameArgsMap.get(value)
            if (newValue != null)
                value = newValue
        }
        return this
    }

    fun variableReplacement(replacements: Map<String, String> = mutableMapOf()): ExpressionNode {
        if (nodeType == NodeType.FUNCTION) {
            for (child in children) {
                child.variableReplacement(replacements)
            }
        } else {
            val newValue = replacements.get(value)
            if (newValue != null)
                value = newValue
        }
        return this
    }

    fun clone(): ExpressionNode {
        val result = copy()
        for (child in children) result.addChild(child.clone())
        return result
    }

    fun containsNestedSameFunctions(): Boolean {
        for (child in children) {
            if ((functionStringDefinition?.function?.isCommutativeWithNullWeight ?: false) && value == child.value) {
                return true
            }
            if (child.containsNestedSameFunctions())
                return true
        }
        return false
    }

    fun cloneWithExpandingNestedSameFunctions(): ExpressionNode {
        val result = copy()
        for (child in children) {
            val childCopy = child.cloneWithExpandingNestedSameFunctions()
            if ((functionStringDefinition?.function?.isCommutativeWithNullWeight ?: false) && value == childCopy.value
            ) {
                for (childCopyChild in childCopy.children) {
                    result.addChild(childCopyChild)
                }
            } else {
                result.addChild(childCopy)
            }
        }
        return result
    }

    fun cloneAndSimplifyByCommutativeNormalizeAndComputeSimplePlaces(compiledConfiguration: CompiledConfiguration = CompiledConfiguration(), selectedNodeIds: Array<Int> = emptyArray()): ExpressionNode {
        if ((selectedNodeIds.isEmpty() || !getAllChildrenNodeIds().containsAny(selectedNodeIds)) &&
                calcComplexity() <= compiledConfiguration.simpleComputationRuleParams.maxCalcComplexity && !containsVariables()) {
            val computed = computeNodeIfSimple(compiledConfiguration.simpleComputationRuleParams)
            if (computed != null) {
                val actualNodeId = nodeId
                return compiledConfiguration.createExpressionVariableNode(computed).apply { nodeId = actualNodeId }
            }
        }
        val result = copy()
        for (child in children) {
            val simplifiedChild = child.cloneAndSimplifyByCommutativeNormalizeAndComputeSimplePlaces(compiledConfiguration, selectedNodeIds)
            if (simplifiedChild.value == value && functionStringDefinition?.function?.isCommutativeWithNullWeight == true && simplifiedChild.nodeId !in selectedNodeIds) {
                for (childOfChild in simplifiedChild.children) {
                    result.addChild(childOfChild.cloneAndSimplifyByCommutativeNormalizeAndComputeSimplePlaces(compiledConfiguration, selectedNodeIds))
                }
            } else {
                result.addChild(simplifiedChild)
            }
        }
        return result
    }

    fun cloneAndSimplifyByComputeSimplePlaces(simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault, selectedNodeIds: Array<Int> = emptyArray()): ExpressionNode {
        if ((selectedNodeIds.isEmpty() || !getAllChildrenNodeIds().containsAny(selectedNodeIds)) &&
                calcComplexity() <= simpleComputationRuleParams.maxCalcComplexity && !containsVariables()) {
            val computed = computeNodeIfSimple(simpleComputationRuleParams)
            if (computed != null) {
                return ExpressionNode(NodeType.VARIABLE, computed.toShortString(), nodeId = nodeId)
            }
        }
        val result = copy()
        for (child in children) {
            result.addChild(child.cloneAndSimplifyByComputeSimplePlaces(simpleComputationRuleParams, selectedNodeIds))
        }
        return result
    }

    fun dropBracketNodesIfOperationsSame() {
        val newChildren: ArrayList<ExpressionNode> = ArrayList()
        for (child in children) {
            child.dropBracketNodesIfOperationsSame()
            if (child.value == value && value != "" &&
                    functionStringDefinition != null && functionStringDefinition!!.function.numberOfArguments < 0
            ) {
                for (childChild in child.children) {
                    newChildren.add(childChild)
                    childChild.parent = this
                }
            } else {
                newChildren.add(child)
            }
        }
        children = newChildren
    }

    fun cloneWithDeepSubstitutions(nameArgsMap: MutableMap<String, ExpressionNode>): ExpressionNode {
        if (nodeType == NodeType.FUNCTION) {
            val result = this.copy()
            for (child in children) {
                result.addChild(child.cloneWithDeepSubstitutions(nameArgsMap))
            }
            return result
        } else {
            val newNode = nameArgsMap.get(value)
            if (newNode != null) return newNode.cloneWithNormalization(sorted = false)
            else return this.copy()
        }
    }

    fun cloneWithNormalization(nameArgsMap: MutableMap<String, String> = mutableMapOf(), sorted: Boolean) =
            clone().normalizeSubTree(nameArgsMap = nameArgsMap, sorted = sorted)

    fun cloneWithVariableReplacement(replacements: Map<String, String>) = clone().variableReplacement(replacements)

    fun isNodeSubtreeEquals(
            expressionNode: ExpressionNode,
            nameArgsMap: MutableMap<String, String> = mutableMapOf()
    ): Boolean {
        val actualValue = nameArgsMap.get(value) ?: value
        val result = actualValue == expressionNode.value
        if (nodeType == NodeType.FUNCTION) {
            if (children.size != expressionNode.children.size ||
                    functionStringDefinition?.function?.numberOfDefinitionArguments != expressionNode.functionStringDefinition?.function?.numberOfDefinitionArguments
            )
                return false
            val numberOfDefinitionArguments = functionStringDefinition?.function?.numberOfDefinitionArguments ?: 0
            val childrenNameArgs = mutableListOf<String>()
            val previousNameArgsMap: MutableMap<String, String> = mutableMapOf()
            for (i in 0 until numberOfDefinitionArguments) {
                childrenNameArgs.add("" + children[i].value)
                val currentNameArgMap = nameArgsMap.get(children[i].value)
                if (currentNameArgMap != null) previousNameArgsMap.put(children[i].value, currentNameArgMap)
                nameArgsMap.put(children[i].value, expressionNode.children[i].value)
            }
            for (i in 0 until children.size) {
                if (!children[i].isNodeSubtreeEquals(expressionNode.children[i], nameArgsMap))
                    return false
            }
            for (name in childrenNameArgs) {
                nameArgsMap.remove(name)
            }
            nameArgsMap.putAll(previousNameArgsMap)
        }
        return result
    }

    fun isNodeMatchesNumber(numberValue: String): Boolean {
        val matchingValue = numberValue.toDoubleOrNull() ?: return false
        val expressionValue = if (nodeType == NodeType.VARIABLE) {
            value.toDoubleOrNull() ?: return false
        } else if (calcComplexity() < 4 && !containsVariables()) {
            computeNodeIfSimple() ?: return false
        } else return false
        return (expressionValue - matchingValue).abs() < 11.9e-7  // todo: unify with approach in BaseOperationDefinitions.kt
    }

    fun replaceNotDefinedFunctionsOnVariables(
            functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>,
            definedFunctionNameNumberOfArgsSet: Set<String>,
            expressionComporator: ExpressionComporator? = null,
            hasBoolFunctions: Boolean = false
    ) {
        for (child in children)
            child.replaceNotDefinedFunctionsOnVariables(
                    functionIdentifierToVariableMap,
                    definedFunctionNameNumberOfArgsSet,
                    expressionComporator,
                    hasBoolFunctions
            )
        if (nodeType == NodeType.FUNCTION && !definedFunctionNameNumberOfArgsSet.contains(value + "_" + children.size)
                && !definedFunctionNameNumberOfArgsSet.contains(value + "_-1")
        ) {
            var nodeExists = false

            for ((expression, variable) in functionIdentifierToVariableMap) {
                if (children.size == expression.children.size) {
                    if (expressionComporator != null &&
                            expressionComporator.compiledConfiguration.comparisonSettings.useTestingToCompareFunctionArgumentsInProbabilityComparison &&
                            !hasBoolFunctions
                    ) {
                        if (expressionComporator.baseOperationsDefinitions.definedFunctionNameNumberOfArgsSet.contains(
                                        value + "_" + children.size
                                ) ||
                                expressionComporator.baseOperationsDefinitions.definedFunctionNameNumberOfArgsSet.contains(
                                        value + "_-1"
                                )
                        ) { //if function is not 'd' or 'i'
                            if (expressionComporator.probabilityTestComparison(
                                            this,
                                            expression,
                                            ComparisonType.EQUAL
                                    )
                            ) {
                                nodeExists = true
                                setVariable(variable)
                                break
                            }
                        } else {
                            var hasDifferentArgs = false
                            for (i in 0..children.lastIndex) {
                                if (!expressionComporator.probabilityTestComparison(
                                                children[i],
                                                expression.children[i],
                                                ComparisonType.EQUAL
                                        )
                                ) {
                                    hasDifferentArgs = true
                                    break
                                }
                            }
                            if (!hasDifferentArgs) {
                                nodeExists = true
                                setVariable(variable)
                                break
                            }
                        }
                    } else {
                        if (isNodeSubtreeEquals(expression)) {
                            nodeExists = true
                            setVariable(variable)
                            break
                        }
                    }
                }
            }
            if (!nodeExists) {
                val variableName = "sys_def_var_replace_fun_${functionIdentifierToVariableMap.size}"
                val key = this.clone()
                functionIdentifierToVariableMap.put(key, variableName)
                setVariable(variableName)
            }
        }
    }
}

class ExpressionNodeConstructor(
        val functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
        val compiledImmediateVariableReplacements: Map<String, String> = mapOf()
) {
    fun construct(identifier: String): ExpressionNode {
        if (identifier.isEmpty()) {
            return ExpressionNode(NodeType.FUNCTION, "")
        }
        val child = if (identifier.first() == '(' && identifier.last() == ')') {
            constructRecursive(identifier.substring(1, identifier.lastIndex), 1)
        } else {
            constructRecursive(identifier, 0)
        }
        if (child.nodeType == NodeType.ERROR) {
            return child
        }
        val newNode = ExpressionNode(NodeType.FUNCTION, "", identifier = identifier)
        newNode.addChild(child)
        return newNode
    }

    private fun constructRecursive(identifier: String, startPosition: Int = 0): ExpressionNode {
        if (identifier.isEmpty()) {
            return ExpressionNode(NodeType.ERROR, value = "Something expected", startPosition = startPosition, endPosition = startPosition + 1)
        }
        val openBracketIndex = identifier.indexOfFirst { it == '(' }
        if (openBracketIndex >= 0) {
            val newNode =
                    ExpressionNode(NodeType.FUNCTION, identifier.substring(0, openBracketIndex), identifier = identifier)
            var openBracketCount = 0
            var currentChildIdentifier: StringBuilder = StringBuilder("")
            for (i in (openBracketIndex + 1) until identifier.length) {
                if (openBracketCount == 0 && (identifier[i] == ';' || identifier[i] == ')')) {
                    val child = constructRecursive(currentChildIdentifier.toString(), startPosition + i - currentChildIdentifier.length)
                    if (child.nodeType == NodeType.ERROR) {
                        return child
                    }
                    newNode.addChild(child)
                    currentChildIdentifier = StringBuilder("")
                } else {
                    currentChildIdentifier.append(identifier[i])
                }
                if (identifier[i] == '(') openBracketCount++
                else if (identifier[i] == ')') openBracketCount--
                if (openBracketCount < -1) {
                    return ExpressionNode(NodeType.ERROR, value = "Unexpected ')'", startPosition = startPosition + i, endPosition = startPosition + i + 1)
                }
            }
            if (openBracketCount >= 0) {
                return ExpressionNode(NodeType.ERROR, value = "closing bracket missing", startPosition = startPosition + identifier.length - 1, endPosition = startPosition + identifier.length)
            }
            if (newNode.value.isNotEmpty() || newNode.children.size > 1) {
                newNode.functionStringDefinition = functionConfiguration.fastFindStringDefinitionByNameAndNumberOfArguments(
                        newNode.value,
                        newNode.children.size
                )
                if (newNode.functionStringDefinition == null) {
                    return ExpressionNode(NodeType.ERROR, value = "Unknown function: '" + newNode.value + "' with '" +
                            newNode.children.size + "' arguments", startPosition = startPosition, endPosition = startPosition + openBracketIndex)
                }
            }
            return newNode
        } else {
            val notDigitVariableSymbolIndex = identifier.indexOfFirst { !it.isNameOrNaturalNumberPart()}
            if (notDigitVariableSymbolIndex >= 0) {
                return ExpressionNode(NodeType.ERROR, value = "Wrong variable symbol '${identifier[notDigitVariableSymbolIndex]}'", startPosition = startPosition + notDigitVariableSymbolIndex, endPosition = startPosition + notDigitVariableSymbolIndex + 1)
            }
            val newValue = compiledImmediateVariableReplacements.get(identifier)
            if (newValue != null)
                return ExpressionNode(NodeType.VARIABLE, newValue, identifier = identifier)
            else
                return ExpressionNode(NodeType.VARIABLE, identifier, identifier = identifier)
        }
    }
}

fun normalizeExpressionsForComparison(
        left: ExpressionNode,
        right: ExpressionNode
): Pair<ExpressionNode, ExpressionNode> {
    val ordered = if (left.nodeType == NodeType.FUNCTION && left.value == "") Pair(left, right)
    else if (right.nodeType == NodeType.FUNCTION && right.value == "") Pair(right, left)
    else return Pair(left, right)

    if (ordered.second.nodeType != NodeType.FUNCTION || ordered.second.value != "") {
        return Pair(ordered.first, ExpressionNode(NodeType.FUNCTION, "").apply { addChild(ordered.second) })
    } else return ordered
}

fun subtractionTree(minuend: ExpressionNode, subtrahend: ExpressionNode): ExpressionNode {
    val result = ExpressionNode(NodeType.FUNCTION, "")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.first().addChild(minuend.children.first().clone())
    result.children.first().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    result.children.first().children.last().addChild(subtrahend.children.first().clone())
    return result
}

fun divisionTree(dividend: ExpressionNode, divider: ExpressionNode): ExpressionNode {
    val result = ExpressionNode(NodeType.FUNCTION, "")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "/"))
    result.children.first().addChild(dividend.children.first().clone())
    result.children.first().addChild(divider.children.first().clone())
    return result
}

fun addRootNodeToExpression(expression: ExpressionNode) : ExpressionNode {
    val root = ExpressionNode(NodeType.FUNCTION, "")
    root.addChild(expression)
    root.computeIdentifier()
    return root
}