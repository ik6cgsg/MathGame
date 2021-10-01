package mathhelper.twf.expressiontree

import mathhelper.twf.baseoperations.BaseOperationsComputation.Companion.additivelyEqual
import mathhelper.twf.baseoperations.BaseOperationsComputation.Companion.epsilon
import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.config.FunctionConfiguration
import mathhelper.twf.standartlibextensions.*
import kotlin.math.floor


interface ExpressionStructurePart


data class FunctionCondition(
        var name: String = "",
        var numberOfArgumentsInterval: NumberInterval = NumberInterval(),
        var internalFunctionCondition: FunctionsCondition = FunctionsCondition(),
        var internalVariableCondition: VariablesCondition = VariablesCondition(),
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "[" + if (name.isNotEmpty()) {
            "name=${name}"
        } else {
            ""
        } + "," +
                if (numberOfArgumentsInterval.isNotEmpty()) {
                    "numberOfArgumentsInterval=${numberOfArgumentsInterval}"
                } else {
                    ""
                } + "," +
                if (internalFunctionCondition.isNotEmpty()) {
                    "internalFunctionCondition=${internalFunctionCondition}"
                } else {
                    ""
                } + "," +
                if (internalVariableCondition.isNotEmpty()) {
                    "internalVariableCondition=${internalVariableCondition}"
                } else {
                    ""
                } + "]"
    }

    fun isEmpty() = name.isEmpty() && numberOfArgumentsInterval.isEmpty() && internalFunctionCondition.isEmpty() && !(internalVariableCondition.isNotEmpty())

    fun matchFunction(node: ExpressionNode): Boolean {
        if (name == node.value && numberOfArgumentsInterval.matchNumber(node.children.size.toDouble())) {
            if (node.getContainedVariables().all { internalVariableCondition.matchVariable(it) }) {
                for (child in node.children) {
                    if (!internalFunctionCondition.matchFunction(child)) {
                        return false
                    }
                }
                return true
            }
        }
        return false
    }

    fun matchFunctionByNameNumberOfArguments(node: ExpressionNode) = (name.isEmpty() && numberOfArgumentsInterval.isEmpty() && internalFunctionCondition.isEmpty() && !internalVariableCondition.isNotEmpty()) ||
            (name == node.value && numberOfArgumentsInterval.matchNumber(node.children.size.toDouble()))
}

data class FunctionsCondition(
//TODO:        var treeNecessaryFunctions: MutableList<FunctionCondition> = mutableListOf(),
        var treePermittedFunctions: MutableList<FunctionCondition> = mutableListOf(),
        var treeForbiddenFunctions: MutableList<FunctionCondition> = mutableListOf(),
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "[" + if (treePermittedFunctions.isNotEmpty()) {
            "treePermittedFunctions=${treePermittedFunctions}"
        } else {
            ""
        } + "," +
                if (treeForbiddenFunctions.isNotEmpty()) {
                    "treeForbiddenFunctions=${treeForbiddenFunctions}"
                } else {
                    ""
                } + "]"
    }

    fun isEmpty() = !isNotEmpty()
    fun isNotEmpty() = treePermittedFunctions.isNotEmpty() || treeForbiddenFunctions.isNotEmpty()

    fun mergeWithTopFunctionsCondition(topFunctionsCondition: FunctionsCondition) {
        if (isNotEmpty()) {
            treePermittedFunctions.addAll(topFunctionsCondition.treePermittedFunctions)
            treeForbiddenFunctions.addAll(topFunctionsCondition.treeForbiddenFunctions)
        }
    }

    fun matchFunction(node: ExpressionNode): Boolean {
        if (isEmpty() || treeForbiddenFunctions.any { it.matchFunction(node) }) {
            return false
        } else if (treePermittedFunctions.isEmpty() || (treePermittedFunctions.size == 1 && treePermittedFunctions.first().isEmpty())) {
            return true
        } else return treePermittedFunctions.any { it.matchFunction(node) }
    }

    fun matchFunctionByNameNumberOfArguments(node: ExpressionNode): Boolean {
        if (isEmpty() || treeForbiddenFunctions.any { it.matchFunctionByNameNumberOfArguments(node) }) {
            return false
        } else if (treePermittedFunctions.isEmpty()) {
            return true
        } else return treePermittedFunctions.any { it.matchFunctionByNameNumberOfArguments(node) }
    }
}

enum class NumberIntervalType { NATURAL, INTEGER, REAL }

data class NumberInterval(
        var numbersType: NumberIntervalType = NumberIntervalType.NATURAL,
        var leftBorder: Double = Double.NEGATIVE_INFINITY,
        var rightBorder: Double = Double.POSITIVE_INFINITY,
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "[$leftBorder;$rightBorder]:${
        when (numbersType) {
            NumberIntervalType.NATURAL -> "N"
            NumberIntervalType.INTEGER -> "Z"
            NumberIntervalType.REAL -> "R"
        }
        }".replace("Infinity", "Inf")
    }

    fun isEmpty() = !isNotEmpty()
    fun isNotEmpty() = leftBorder != Double.NEGATIVE_INFINITY || rightBorder != Double.POSITIVE_INFINITY || numbersType != NumberIntervalType.NATURAL

    fun matchNumber(number: Double): Boolean {
        if (leftBorder - epsilon < number && number < rightBorder + epsilon) {
            when (numbersType) {
                NumberIntervalType.NATURAL -> return additivelyEqual(floor(number + epsilon / 3), number) && 0.0 - epsilon < number
                NumberIntervalType.INTEGER -> return additivelyEqual(floor(number + epsilon / 3), number)
                NumberIntervalType.REAL -> return true
            }
        } else return false
    }
}

data class NumberCondition(
        var intervals: MutableList<NumberInterval> = mutableListOf(),
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "[" + intervals.toString() + "]"
    }

    fun isEmpty() = !isNotEmpty()
    fun isNotEmpty() = intervals.isNotEmpty() && intervals.any { it.isNotEmpty() }

    fun matchNumber(number: Double) = intervals.isEmpty() || intervals.any { it.matchNumber(number) }
}

data class VariableCondition(
        var intervals: MutableList<NumberInterval> = mutableListOf(),
        var variableName: String = "",
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    fun hasNumberCondition() = intervals.isNotEmpty()
    fun hasVariableCondition() = variableName.isNotEmpty()

    fun intervalsToString() = if (intervals.isNotEmpty()) {
        "intervals=${intervals}"
    } else {
        ""
    }

    fun variableNameToString() = if (variableName.isNotEmpty()) {
        "variableName=${variableName}"
    } else {
        ""
    }

    override fun toString(): String {
        return "[" + if (intervals.isNotEmpty()) {
            "intervals=${intervals}"
        } else {
            ""
        } + "," +
                if (variableName.isNotEmpty()) {
                    "variableName=${variableName}"
                } else {
                    ""
                } + "]"
    }

    fun matchVariable(name: String): Boolean {
        val number = name.toDoubleOrNull() ?: return name == variableName
        return intervals.isEmpty() || intervals.any { it.matchNumber(number) }
    }
}

data class VariablesCondition(
//TODO:        var treeNecessaryVariables: MutableList<VariableCondition> = mutableListOf(),
        var treePermittedVariables: MutableList<VariableCondition> = mutableListOf(),
        var treeForbiddenVariables: MutableList<VariableCondition> = mutableListOf(),
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "[" + if (treePermittedVariables.isNotEmpty()) {
            "treePermittedVariables=${treePermittedVariables}"
        } else {
            ""
        } + "," +
                if (treeForbiddenVariables.isNotEmpty()) {
                    "treeForbiddenVariables=${treeForbiddenVariables.toString()}"
                } else {
                    ""
                } + "]"
    }

    fun isNotEmpty() = treePermittedVariables.isNotEmpty() || treeForbiddenVariables.isNotEmpty()

    fun mergeWithTopVariablesCondition(topVariablesCondition: VariablesCondition) {
        treePermittedVariables.addAll(topVariablesCondition.treePermittedVariables)
        treeForbiddenVariables.addAll(topVariablesCondition.treeForbiddenVariables)
    }

    fun matchVariable(name: String): Boolean {
        if (treeForbiddenVariables.any { it.matchVariable(name) }) {
            return false
        } else if (treePermittedVariables.isEmpty()) {
            return true
        } else return treePermittedVariables.any { it.matchVariable(name) }
    }
}

data class ChildrenCondition(
        var childNodes: MutableList<ExpressionStructureConditionNode> = mutableListOf(),
        var count: NumberCondition = NumberCondition(),
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "[" + if (count.isNotEmpty()) {
            "count=${count}"
        } else {
            ""
        } + "," +
                if (childNodes.isNotEmpty()) {
                    "childNodes=${childNodes}"
                } else {
                    ""
                } + "]"
    }

    fun isEmpty() = !isNotEmpty()
    fun isNotEmpty() = childNodes.isNotEmpty() || count.isNotEmpty()
}

data class ExpressionStructureConditionNode(
        var nodeFunctions: FunctionsCondition = FunctionsCondition(),
        var treeFunctions: FunctionsCondition = FunctionsCondition(),
        var treeVariables: VariablesCondition = VariablesCondition(),
        var children: MutableList<ChildrenCondition> = mutableListOf(),
        var functionsJoinedWithVariables: MutableSet<String> = mutableSetOf(),
        var parent: ExpressionStructurePart? = null
) : ExpressionStructurePart {
    override fun toString(): String {
        return "{ " + if (nodeFunctions.isNotEmpty()) {
            "nodeFunctions=${nodeFunctions}"
        } else {
            ""
        } + "," +
                if (treeFunctions.isNotEmpty()) {
                    "treeFunctions=${treeFunctions}"
                } else {
                    ""
                } + "," +
                if (treeVariables.isNotEmpty()) {
                    "treeVariables=${treeVariables}"
                } else {
                    ""
                } + "," +
                if (children.isNotEmpty()) {
                    "children=${children}"
                } else {
                    ""
                } +
                if (functionsJoinedWithVariables.isNotEmpty()) {
                    ",functionsJoinedWithVariables=${functionsJoinedWithVariables}"
                } else {
                    ""
                } + " }"
    }

    fun forwardTreeFunctionVariablesConditions(topFunctionsCondition: FunctionsCondition = FunctionsCondition(), topVariablesCondition: VariablesCondition = VariablesCondition(), topFunctionsJoinedWithVariables: Set<String> = setOf()) {
        treeFunctions.mergeWithTopFunctionsCondition(topFunctionsCondition)
        treeVariables.mergeWithTopVariablesCondition(topVariablesCondition)
        functionsJoinedWithVariables.addAll(topFunctionsJoinedWithVariables)
        for (child in children) {
            for (node in child.childNodes) {
                node.forwardTreeFunctionVariablesConditions(treeFunctions, treeVariables, functionsJoinedWithVariables)
            }
        }
    }
}


fun checkExpressionStructure(rootNode: ExpressionNode, structureNode: ExpressionStructureConditionNode): Boolean {
    if (rootNode.nodeType == NodeType.VARIABLE) {
        return structureNode.treeVariables.matchVariable(rootNode.value) && (structureNode.children.isEmpty() || structureNode.children.any { it.count.matchNumber(0.0) })
    }
    if (rootNode.value in structureNode.functionsJoinedWithVariables && rootNode.children.all { it.nodeType == NodeType.VARIABLE }) {
        return rootNode.children.all { structureNode.treeVariables.matchVariable(it.value) && (structureNode.children.isEmpty() || structureNode.children.any { it.count.matchNumber(0.0) }) }
    }
    if (rootNode.value == "" || (rootNode.value in listOf("+", "*", "^") && rootNode.children.size == 1)) {
        return checkExpressionStructure(rootNode.children.firstOrNull() ?: return true, structureNode)
    }
    if (!structureNode.nodeFunctions.matchFunctionByNameNumberOfArguments(rootNode)) {
        return structureNode.children.any { it.childNodes.any { checkExpressionStructure(rootNode, it) } }
    }
    if (structureNode.treeVariables.isNotEmpty()) {
        val variables = rootNode.getContainedVariables()
        if (variables.any {
                    val variableName = it
                    structureNode.treeVariables.treeForbiddenVariables.any { it.matchVariable(variableName) }
                }) {
            return false
        }
        if (structureNode.treeVariables.treePermittedVariables.isNotEmpty() &&
                !variables.all {
                    val variableName = it
                    structureNode.treeVariables.treePermittedVariables.any { it.matchVariable(variableName) }
                }) {
            return false
        }
    }
    val childrenMatches = Array<Int>(size = structureNode.children.size, init = { 0 })
    for (child in rootNode.children) {
        var matched = false
        for (i in 0..structureNode.children.lastIndex) {
            if (structureNode.children[i].childNodes.isNotEmpty()) {
                for (childStructureNode in structureNode.children[i].childNodes) {
                    if (checkExpressionStructure(child, childStructureNode)) {
                        childrenMatches[i] = childrenMatches[i] + 1
                        matched = true
                        break
                    }
                }
                if (!matched) {
                    return false
                }
            } else {
                childrenMatches[i] = childrenMatches[i] + 1
            }
        }
        if (!matched) {
            if (child.nodeType == NodeType.VARIABLE || (child.value in structureNode.functionsJoinedWithVariables && child.children.all { it.nodeType == NodeType.VARIABLE })) {
            } else {
                if (!structureNode.treeFunctions.matchFunction(child)) {
                    return false
                }
            }
        }
    }
    for (i in 0..structureNode.children.lastIndex) {
        if (!structureNode.children[i].count.matchNumber(childrenMatches[i].toDouble())) {
            return false
        }
    }

    return true
}


class ExpressionStructureConditionConstructor(val compiledConfiguration: CompiledConfiguration = CompiledConfiguration()) {
    val nameSymbols = compiledConfiguration.functionConfiguration.functionProperties.map { it.function }.filterNot {
        it.firstOrNull()?.isLetterOrDigitOrUnderscore() ?: true
    }.joinToString(separator = "")


    fun parse(pattern: String): ExpressionStructureConditionNode {
        val resultExpressionStructureConditionNode = ExpressionStructureConditionNode()
        parseExpressionStructureConditionNode(pattern, 0, resultExpressionStructureConditionNode)
        resultExpressionStructureConditionNode.forwardTreeFunctionVariablesConditions()
        return resultExpressionStructureConditionNode
    }

    fun parseExpressionStructureConditionNode(pattern: String, startPosition: Int, result: ExpressionStructureConditionNode): Int {
        var currentPosition = startPosition
        currentPosition = parseWhitespaces(pattern, currentPosition)
        if (currentPosition < pattern.length && (pattern[currentPosition].isFunctionNameOrNumberPart() || pattern[currentPosition] == freeValueSign || pattern[currentPosition] == negativeSign)) {
            currentPosition = parseFunctionsCondition(pattern, currentPosition, result.nodeFunctions)
            if (currentPosition < pattern.length && pattern[currentPosition] == childrenListSeparator) {
                return currentPosition + 1
            }
            if (currentPosition < pattern.length && pattern[currentPosition] == tokenSeparator) {
                currentPosition++ //skip separator
            }
        }

        var previousPosition = currentPosition
        while (currentPosition < pattern.length && pattern[currentPosition] != tokenSeparator && pattern[currentPosition] != closeChildBracket) {
            result.children.add(ChildrenCondition(parent = result))
            currentPosition = parseChildCondition(pattern, currentPosition, result.children.last())
            if (result.children.last().isEmpty()) {
                result.children.removeAt(result.children.lastIndex)
            }
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition < pattern.length && (pattern[currentPosition] == listSeparator || pattern[currentPosition] == childrenListSeparator)) {
                currentPosition++
                currentPosition = parseWhitespaces(pattern, currentPosition)
            }
            if (previousPosition == currentPosition) {
                currentPosition++
            }
            previousPosition = currentPosition
        }

        if (currentPosition < pattern.length && (pattern[currentPosition] == childrenListSeparator || pattern[currentPosition] == closeChildBracket)) {
            return currentPosition + 1
        }
        currentPosition++
        currentPosition = parseWhitespaces(pattern, currentPosition)
        currentPosition = parseFunctionsCondition(pattern, currentPosition, result.treeFunctions)
        currentPosition = parseWhitespaces(pattern, currentPosition)

        if (currentPosition < pattern.length && pattern[currentPosition] == childrenListSeparator) {
            return currentPosition + 1
        }
        currentPosition++
        currentPosition = parseWhitespaces(pattern, currentPosition)
        currentPosition = parseVariablesCondition(pattern, currentPosition, result.treeVariables)
        currentPosition = parseWhitespaces(pattern, currentPosition)

        if (currentPosition < pattern.length && pattern[currentPosition] == childrenListSeparator) {
            return currentPosition + 1
        }
        currentPosition++

        previousPosition = currentPosition
        while (currentPosition < pattern.length && pattern[currentPosition] != tokenSeparator && pattern[currentPosition] != closeChildBracket) {
            val value = StringBuilder()
            currentPosition = parseValue(pattern, currentPosition, value, { it -> it.isNameOrNumberPart() })
            result.functionsJoinedWithVariables.add(value.toString())
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition < pattern.length && (pattern[currentPosition] == listSeparator)) {
                currentPosition++
                currentPosition = parseWhitespaces(pattern, currentPosition)
            }
            if (previousPosition == currentPosition) {
                currentPosition++
            }
            previousPosition = currentPosition
        }

        return currentPosition
    }

    fun parseChildCondition(pattern: String, startPosition: Int, result: ChildrenCondition): Int {
        var currentPosition = startPosition
        currentPosition = parseWhitespaces(pattern, currentPosition)
        if (currentPosition < pattern.length && pattern[currentPosition] == freeValueSign || pattern[currentPosition].isNumberPart()) {
            currentPosition = parseNumberCondition(pattern, currentPosition, result.count)
        } else {
            result.count = NumberCondition(mutableListOf(), parent = result)
            result.count.intervals.add(NumberInterval(parent = result.count))
        }
        currentPosition = parseWhitespaces(pattern, currentPosition)
        if (currentPosition < pattern.length && pattern[currentPosition] != openChildBracket) {
            return currentPosition
//            return throwOpenChildBracketExpectException(currentPosition)
        }
        currentPosition++
        currentPosition = parseWhitespaces(pattern, currentPosition)

        var previousPosition = currentPosition
        while (currentPosition < pattern.length && pattern[currentPosition] != childrenListSeparator && pattern[currentPosition] != tokenSeparator) {
            result.childNodes.add(ExpressionStructureConditionNode(parent = result))
            currentPosition = parseExpressionStructureConditionNode(pattern, currentPosition, result.childNodes.last())
            currentPosition = parseSpaces(pattern, currentPosition, { it.isWhitespace() || it == closeChildBracket || it == openChildBracket })
            if (previousPosition == currentPosition) {
                currentPosition++
            }
            previousPosition = currentPosition
        }
        return currentPosition
    }

    fun parseFunctionsCondition(pattern: String, startPosition: Int, result: FunctionsCondition): Int {
        var currentPosition = startPosition
        var currentNotNumber = 0
        currentPosition = parseWhitespaces(pattern, currentPosition)
        var previousPosition = currentPosition
        while (currentPosition < pattern.length && (pattern[currentPosition].isFunctionNameOrNumberPart() || pattern[currentPosition] == freeValueSign || pattern[currentPosition] == negativeSign)) {
            if (pattern[currentPosition] == negativeSign) {
                currentNotNumber++
                currentPosition++
                currentPosition = parseWhitespaces(pattern, currentPosition)
                continue
            }
            val functionCondition = if (currentNotNumber % 2 == 0) {
                result.treePermittedFunctions.add(FunctionCondition(parent = result))
                result.treePermittedFunctions.last()
            } else {
                result.treeForbiddenFunctions.add(FunctionCondition(parent = result))
                result.treeForbiddenFunctions.last()
            }
            currentNotNumber = 0
            currentPosition = parseFunctionCondition(pattern, currentPosition, functionCondition)
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition < pattern.length && pattern[currentPosition] == listSeparator) {
                currentPosition++
            }
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (previousPosition == currentPosition) {
                currentPosition++
            }
            previousPosition = currentPosition
        }
        return currentPosition
    }

    fun parseFunctionCondition(pattern: String, startPosition: Int, result: FunctionCondition): Int {
        var currentPosition = startPosition
        val value = StringBuilder()
        currentPosition = parseValue(pattern, currentPosition, value, { it -> it.isFunctionNameOrNumberPart() })
        result.name = value.toString()
        currentPosition = parseSpaces(pattern, currentPosition, { it -> it.isNamePart() || it == listSeparator })
        if (currentPosition < pattern.length && pattern[currentPosition].isNumberPart()) {
            currentPosition = parseNumberInterval(pattern, currentPosition, result.numberOfArgumentsInterval)
        }
        currentPosition = parseWhitespaces(pattern, currentPosition)
        if (currentPosition < pattern.length && pattern[currentPosition] != openChildBracket) {
            return currentPosition
//            return throwOpenChildBracketExpectException(currentPosition)
        }
        currentPosition++
        currentPosition = parseWhitespaces(pattern, currentPosition)
        if (currentPosition < pattern.length && pattern[currentPosition] != closeChildBracket) {
            currentPosition = parseFunctionsCondition(pattern, currentPosition, result.internalFunctionCondition)
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition < pattern.length && pattern[currentPosition] == tokenSeparator) {
                currentPosition++
                currentPosition = parseVariablesCondition(pattern, currentPosition, result.internalVariableCondition)
                currentPosition = parseWhitespaces(pattern, currentPosition)
            }
            if (currentPosition >= pattern.length || pattern[currentPosition] != closeChildBracket) {
                throwCloseChildBracketExpectException(currentPosition)
            }
        }
        return currentPosition + 1
    }

    fun parseVariablesCondition(pattern: String, startPosition: Int, result: VariablesCondition): Int {
        var currentPosition = startPosition
        var currentNotNumber = 0
        currentPosition = parseWhitespaces(pattern, currentPosition)
        var previousPosition = currentPosition
        while (currentPosition < pattern.length && (pattern[currentPosition].isNamePart() || pattern[currentPosition] == freeValueSign || pattern[currentPosition] == negativeSign)) {
            if (pattern[currentPosition] == negativeSign) {
                currentNotNumber++
                currentPosition++
                currentPosition = parseWhitespaces(pattern, currentPosition)
                continue
            }
            val nextPosition = parseWhitespaces(pattern, currentPosition + 1)
            if (pattern[currentPosition] == freeValueSign && ((nextPosition >= pattern.length) || (pattern[nextPosition] in listOf(listSeparator, childrenListSeparator, openChildBracket, closeChildBracket, tokenSeparator)))) {
                currentPosition++
                currentPosition = nextPosition
                continue
            }
            val variableCondition = if (currentNotNumber % 2 == 0) {
                result.treePermittedVariables.add(VariableCondition(parent = result))
                result.treePermittedVariables.last()
            } else {
                result.treeForbiddenVariables.add(VariableCondition(parent = result))
                result.treeForbiddenVariables.last()
            }
            currentPosition = parseVariableCondition(pattern, currentPosition, variableCondition)
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition < pattern.length && pattern[currentPosition] == listSeparator) {
                currentPosition++
            }
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (previousPosition == currentPosition) {
                currentPosition++
            }
            previousPosition = currentPosition
        }
        return currentPosition
    }

    fun parseVariableCondition(pattern: String, startPosition: Int, result: VariableCondition): Int {
        var currentPosition = startPosition
        if (pattern[currentPosition] == freeValueSign || pattern[currentPosition].isNumberPart()) {
            result.intervals.add(NumberInterval(parent = result))
            currentPosition = parseNumberInterval(pattern, currentPosition, result.intervals.last())
        } else {
            val value = StringBuilder()
            currentPosition = parseValue(pattern, currentPosition, value, { it -> it.isNamePart() })
            result.variableName = value.toString()
        }
        return currentPosition
    }

    fun parseNumberCondition(pattern: String, startPosition: Int, result: NumberCondition): Int {
        var currentPosition = startPosition
        currentPosition = parseWhitespaces(pattern, currentPosition)
        var previousPosition = currentPosition
        while (currentPosition < pattern.length && (pattern[currentPosition].isNumberPart() || pattern[currentPosition] == freeValueSign)) {
            result.intervals.add(NumberInterval(parent = result))
            currentPosition = parseNumberInterval(pattern, currentPosition, result.intervals.last())
            if (result.intervals.last().isEmpty()) {
                result.intervals.removeAt(result.intervals.lastIndex)
            }
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition < pattern.length && pattern[currentPosition] == listSeparator) {
                currentPosition++
            }
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (previousPosition == currentPosition) {
                currentPosition++
            }
            previousPosition = currentPosition
        }
        return currentPosition
    }

    fun parseNumberInterval(pattern: String, startPosition: Int, result: NumberInterval): Int {
        var currentPosition = startPosition
        if (pattern[currentPosition] == freeValueSign) {
            result.leftBorder = Double.NEGATIVE_INFINITY
            result.rightBorder = Double.POSITIVE_INFINITY
            currentPosition++
        } else {
            if (pattern[currentPosition].isNumberPart()) {
                val numberValue = StringBuilder()
                currentPosition = parseValue(pattern, currentPosition, numberValue, { it -> it.isNumberPart() })
                result.leftBorder = numberValue.toString().toDoubleOrNull()
                        ?: return throwNumberExpectException(currentPosition)
                result.rightBorder = result.leftBorder
            } else throwNumberExpectException(currentPosition)
        }
        if (currentPosition < pattern.length && pattern[currentPosition].isLetter()) {
            when (pattern[currentPosition]) {
                'N' -> result.numbersType = NumberIntervalType.NATURAL
                'Z' -> result.numbersType = NumberIntervalType.INTEGER
                'R' -> result.numbersType = NumberIntervalType.REAL
                else -> return throwNumberExpectException(currentPosition)
            }
            currentPosition++
        }
        currentPosition = parseWhitespaces(pattern, currentPosition)
        if (currentPosition < pattern.length && pattern[currentPosition] == '-') {
            currentPosition++
            currentPosition = parseWhitespaces(pattern, currentPosition)
            if (currentPosition >= pattern.length) return throwNumberExpectException(currentPosition)
            if (pattern[currentPosition] == freeValueSign) {
                result.rightBorder = Double.POSITIVE_INFINITY
                currentPosition++
            } else {
                if (pattern[currentPosition].isNumberPart()) {
                    val numberValue = StringBuilder()
                    currentPosition = parseValue(pattern, currentPosition, numberValue, { it -> it.isNumberPart() })
                    result.rightBorder = numberValue.toString().toDoubleOrNull()
                            ?: return throwNumberExpectException(currentPosition)
                } else throwNumberExpectException(currentPosition)
            }
            if (currentPosition < pattern.length && pattern[currentPosition].isLetter()) {
                when (pattern[currentPosition]) {
                    'Z' -> result.numbersType = NumberIntervalType.INTEGER
                    'R' -> result.numbersType = NumberIntervalType.REAL
                    else -> return throwNumberExpectException(currentPosition)
                }
                currentPosition++
            }
        }

        return currentPosition
    }

    fun parseWhitespaces(pattern: String, startPosition: Int): Int {
        var currentPosition = startPosition
        while (currentPosition < pattern.length && pattern[currentPosition].isWhitespace()) {
            currentPosition++
        }
        return currentPosition
    }

    fun parseSpaces(pattern: String, startPosition: Int, condition: (Char) -> Boolean): Int {
        var currentPosition = startPosition
        while (currentPosition < pattern.length && condition(pattern[currentPosition])) {
            currentPosition++
        }
        return currentPosition
    }

    fun parseValue(pattern: String, startPosition: Int, value: StringBuilder, condition: (Char) -> Boolean): Int {
        var currentPosition = startPosition
        while (currentPosition < pattern.length && condition(pattern[currentPosition])) {
            value.append(pattern[currentPosition])
            currentPosition++
        }
        return currentPosition
    }

    private fun Char.isFunctionNameOrNumberPart() = this.isLetterOrDigitOrUnderscore() || nameSymbols.contains(this)

    private fun throwExpectException(expectType: String, position: Int): Int {
        throw IllegalArgumentException("$expectType expected on position '$position'"); return Int.MAX_VALUE
    }

    private fun throwOpenChildBracketExpectException(position: Int) = throwExpectException("'$openChildBracket'", position)
    private fun throwCloseChildBracketExpectException(position: Int) = throwExpectException("'$closeChildBracket'", position)
    private fun throwNumberExpectException(position: Int) = throwExpectException("Number", position)

    val tokenSeparator = ':'
    val listSeparator = ','
    val childrenListSeparator = ';'
    val openChildBracket = '('
    val closeChildBracket = ')'

    val freeValueSign = '?'
    val negativeSign = '!'
}