package com.twf.expressiontree

import com.twf.config.ComparisonType
import com.twf.config.FunctionConfiguration
import com.twf.config.FunctionStringDefinition
import com.twf.config.StringDefinitionType
import com.twf.standartlibextensions.abs
import com.twf.standartlibextensions.isNumberPart
import com.twf.standartlibextensions.isSign
import kotlin.math.max

enum class NodeType {
    FUNCTION,
    VARIABLE,
    EMPTY
}

data class NodeValue(
        var exactNumber: Int
)

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
        var nodeId: Int = -1
) {
    var children: ArrayList<ExpressionNode> = ArrayList()

    //only facts variables:
    var rules: ArrayList<ExpressionSubstitution> = ArrayList() //correct for this and children levels transformation rules
    var parserMark: String = ""
    //todo(add fact chain type: '>', '=', '<', 'equality')

    fun addChild(newNode: ExpressionNode) {
        children.add(newNode)
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
        while (result.parent != null){
            result = result.parent!!
        }
        return result
    }

    fun computeNodeIdsAsNumbersInDirectTraversal (startId: Int = 0): Int {
        nodeId = startId
        var currentStartId = startId + 1
        for (child in children){
            currentStartId = child.computeNodeIdsAsNumbersInDirectTraversal(currentStartId)
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
                identifier += /*if (child.identifier.isEmpty()) */child.computeIdentifierWithPositions(getNodeValueString)/* else child.identifier*/ + ";"
            }
            if (children.size > 0)
                identifier = identifier.substring(0, identifier.length - 1)
            identifier += ")"
        }
        return identifier + "{$startPosition;$endPosition}"
    }

    fun toStringsWithPositions(getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }, offset: String = ""): String {
        if (nodeType == NodeType.VARIABLE) {
            identifier = offset + getNodeValueString(this) + "  :  [$startPosition; $endPosition; $nodeId)\n"
        } else {
            identifier = offset + value + "  :  [$startPosition; $endPosition; $nodeId)\n"
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.toStringsWithPositions(getNodeValueString, offset + "  ")/* else child.identifier*/
            }
        }
        return identifier
    }

    fun toUserView(functionConfiguration: FunctionConfiguration = FunctionConfiguration(), getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }): String {
        var result = toUserViewRec(functionConfiguration, getNodeValueString)
        if (result.first() == '(' && result.last() == ')') {
            var numberOfOpenBrackets = 1
            var currentIndex = 1
            while (currentIndex < result.length && numberOfOpenBrackets > 0){
                if (result[currentIndex] == '('){
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

    private fun toUserViewRec(functionConfiguration: FunctionConfiguration = FunctionConfiguration(), getNodeValueString: (ExpressionNode) -> String = { it.getNodeValueString() }): String {
        var identifier = ""
        if (nodeType == NodeType.VARIABLE) {
            identifier = getNodeValueString(this)
        } else if (value == "" && children.size == 1) {
            identifier = children.first().toUserViewRec(functionConfiguration)
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
                                functionConfiguration.functionPropertiesByName.get(child.value + "_-1")?.function != functionIdentifier.function) {
                            if (identifier.length > "(".length) {
                                identifier = identifier.substring(0, identifier.length - functionIdentifier.userRepresentation.length)
                            }
                            identifier += functionConfiguration.functionPropertiesByName.get(child.value + "_-1")!!.function
                            identifier += child.children.first().toUserViewRec(functionConfiguration) + functionIdentifier.userRepresentation
                        } else {
                            identifier += child.toUserViewRec(functionConfiguration) + functionIdentifier.userRepresentation
                        }
                    }
                    if (children.size > 0) {
                        identifier = identifier.substring(0, identifier.length - functionIdentifier.userRepresentation.length)
                    }
                    if (binaryOperationNeedBrackets()) {
                        identifier += ")"
                    }
                }
                StringDefinitionType.UNARY_LEFT_OPERATION -> {
                    val childUserRepresentation = children.first().toUserViewRec(functionConfiguration)
                    identifier = functionIdentifier.userRepresentation + if (childUserRepresentation.first().isSign(false)){
                        "($childUserRepresentation)"
                    } else {
                        childUserRepresentation
                    }
                }
                StringDefinitionType.UNARY_RIGHT_OPERATION -> {
                    val childUserRepresentation = children.first().toUserViewRec(functionConfiguration)
                    identifier = if (childUserRepresentation.first().isSign(false)){
                        "($childUserRepresentation)"
                    } else {
                        childUserRepresentation
                    } + functionIdentifier.userRepresentation
                }
            }
        } else {
            identifier = value + "("
            for (child in children) {
                identifier += /*if (child.identifier.isEmpty()) */child.toUserViewRec()/* else child.identifier*/ + ","
            }
            if (children.size > 0)
                identifier = identifier.substring(0, identifier.length - 1)
            identifier += ")"
        }
        return identifier
    }

    private fun binaryOperationNeedBrackets() =
            parent?.functionStringDefinition?.function?.defaultStringDefinitionType != StringDefinitionType.FUNCTION

    fun isNodeValueEquals(expressionNode: ExpressionNode) = getNodeValueString() == expressionNode.getNodeValueString()
    fun isNodeSubtreeIdentifiersEquals(expressionNode: ExpressionNode) = computeIdentifier() == expressionNode.computeIdentifier()

    fun sortChildrenAscendingIdentifiers() {
        if (nodeType == NodeType.FUNCTION && (functionStringDefinition?.function?.isCommutativeWithNullWeight == true))
            children.sortBy({ it.identifier })
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
            return max(maxMinNumberOfPointsForEquality, functionStringDefinition?.function?.minNumberOfPointsForEquality
                    ?: 1)
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
        var result = mutableSetOf<String>()
        if (!children.isEmpty()) {
            if (value.isNotBlank()) {
                result.add(value)
            }
            for (child in children)
                result.addAll(child.getContainedFunctions())
        }
        return result
    }

    fun getContainedVariables(): Set<String> {
        var result = mutableSetOf<String>()
        if (children.isEmpty()) {
            if (value.toDoubleOrNull() == null) {
                result.add(value)
            }
        } else for (child in children)
            result.addAll(child.getContainedVariables())
        return result
    }

    fun getContainedVariables(variables: Set<String>): Set<String> {
        var result = mutableSetOf<String>()
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
        var result = mutableSetOf<String>()
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

    fun normalizeSubTree(currentDeep: Int = 0, nameArgsMap: MutableMap<String, String> = mutableMapOf(), sorted: Boolean): ExpressionNode {
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

    fun dropBracketNodesIfOperationsSame (){
        val newChildren: ArrayList<ExpressionNode> = ArrayList()
        for (child in children){
            child.dropBracketNodesIfOperationsSame()
            if (child.value == value && value != "" &&
                    functionStringDefinition != null && functionStringDefinition!!.function.numberOfArguments < 0){
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

    fun cloneWithNormalization(nameArgsMap: MutableMap<String, String> = mutableMapOf(), sorted: Boolean) = clone().normalizeSubTree(nameArgsMap = nameArgsMap, sorted = sorted)

    fun cloneWithVariableReplacement(replacements: Map<String, String>) = clone().variableReplacement(replacements)

    fun isNodeSubtreeEquals(expressionNode: ExpressionNode, nameArgsMap: MutableMap<String, String> = mutableMapOf()): Boolean {
        val actualValue = nameArgsMap.get(value) ?: value
        val result = actualValue == expressionNode.value
        if (nodeType == NodeType.FUNCTION) {
            if (children.size != expressionNode.children.size ||
                    functionStringDefinition?.function?.numberOfDefinitionArguments != expressionNode.functionStringDefinition?.function?.numberOfDefinitionArguments)
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

    fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>,
                                              definedFunctionNameNumberOfArgsSet: Set<String>,
                                              expressionComporator: ExpressionComporator? = null,
                                              hasBoolFunctions: Boolean = false) {
        for (child in children)
            child.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet, expressionComporator, hasBoolFunctions)
        if (nodeType == NodeType.FUNCTION && !definedFunctionNameNumberOfArgsSet.contains(value + "_" + children.size)
                && !definedFunctionNameNumberOfArgsSet.contains(value + "_-1")) {
            var nodeExists = false

            for ((expression, variable) in functionIdentifierToVariableMap) {
                if (children.size == expression.children.size) {
                    if (expressionComporator != null &&
                            expressionComporator.compiledConfiguration.comparisonSettings.useTestingToCompareFunctionArgumentsInProbabilityComparison &&
                            !hasBoolFunctions) {
                        if (expressionComporator.baseOperationsDefinitions.definedFunctionNameNumberOfArgsSet.contains(value + "_" + children.size) ||
                                expressionComporator.baseOperationsDefinitions.definedFunctionNameNumberOfArgsSet.contains(value + "_-1")) { //if function is not 'd' or 'i'
                            if (expressionComporator.probabilityTestComparison(this, expression, ComparisonType.EQUAL)) {
                                nodeExists = true
                                setVariable(variable)
                                break
                            }
                        } else {
                            var hasDifferentArgs = false
                            for (i in 0..children.lastIndex) {
                                if (!expressionComporator.probabilityTestComparison(children[i], expression.children[i], ComparisonType.EQUAL)) {
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

class ExpressionNodeConstructor(val functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
                                val compiledImmediateVariableReplacements: Map<String, String> = mapOf()) {
    fun construct(identifier: String): ExpressionNode {
        if (identifier.contains('(')) {
            val openBracketIndex = identifier.indexOfFirst { it == '(' }
            var newNode = ExpressionNode(NodeType.FUNCTION, identifier.substring(0, openBracketIndex), identifier = identifier)
            var openBracketCount = 0
            var currentChildIdentifier: StringBuilder = StringBuilder("")
            for (i in (openBracketIndex + 1) until identifier.length) {
                if (openBracketCount == 0 && (identifier[i] == ';' || identifier[i] == ')')) {
                    newNode.addChild(construct(currentChildIdentifier.toString()))
                    currentChildIdentifier = StringBuilder("")
                } else {
                    currentChildIdentifier.append(identifier[i])
                    if (identifier[i] == '(') openBracketCount++
                    else if (identifier[i] == ')') openBracketCount--
                }
            }
            newNode.functionStringDefinition = functionConfiguration.fastFindStringDefinitionByNameAndNumberOfArguments(newNode.value, newNode.children.size)
            return newNode
        } else {
            val newValue = compiledImmediateVariableReplacements.get(identifier)
            if (newValue != null)
                return ExpressionNode(NodeType.VARIABLE, newValue, identifier = identifier)
            else
                return ExpressionNode(NodeType.VARIABLE, identifier, identifier = identifier)
        }
    }
}

fun normalizeExpressionsForComparison(left: ExpressionNode, right: ExpressionNode): Pair<ExpressionNode, ExpressionNode> {
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