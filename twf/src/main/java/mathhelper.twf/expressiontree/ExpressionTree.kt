package mathhelper.twf.expressiontree

import mathhelper.twf.config.*
import mathhelper.twf.standartlibextensions.*

data class MathMlTagTreeNode(
        var type: Type,
        var value: String,
        var startPosition: Int = -1,
        var endPosition: Int = -1,
        var parent: MathMlTagTreeNode? = null,
        var stringDefinitionType: StringDefinitionType? = StringDefinitionType.FUNCTION,
        var isNumberRead: Boolean = false,
        var needDoubleChild: Boolean = false,
        var needOneMoreBracket: Boolean = false,
        var isLatexArgument: Boolean = false,
        var texArgumentType: TexArgumentType = TexArgumentType.USUAL
) {
    enum class Type {
        EXPRESSION_PART_STRING, MATH_ML_FUNCTION, BRACKET_FUNCTION,
        NAME, OPERATION
    }

    enum class TexArgumentType {
        USUAL, UNDERLINED, POW
    }

    var children: kotlin.collections.ArrayList<MathMlTagTreeNode> = ArrayList()

    fun addChild(newNode: MathMlTagTreeNode) {
        children.add(newNode)
        newNode.parent = this
    }

    fun getNodeString(): String {
        if (children.size == 0) return value
        var value = this.value + "{"
        for (child in children) value += child.getNodeString() + ';'
        if (value.last() == ';') value = value.substring(0, value.lastIndex)
        value += '}'
        return value
    }

    override fun toString(): String = getNodeString()
}

fun kotlin.collections.ArrayList<MathMlTagTreeNode>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}

data class ExpressionParserNode(
        var type: Type,
        var value: String,
        var startPosition: Int = -1,
        var endPosition: Int = -1,
        var subValue: String = "", //value on bottom lines <msub>
        var parent: ExpressionParserNode? = null,
        var functionStringDefinition: FunctionStringDefinition? = null
) {
    enum class Type {
        FUNCTION, VARIABLE, BINARY_OPERATION, UNARY_OPERATION
    }

    var children: ArrayList<ExpressionParserNode> = ArrayList()

    fun addChild(newNode: ExpressionParserNode) {
        children.add(newNode)
        newNode.parent = this
    }

    fun getFirstListNodeAfterFunction(functionMet: Boolean = false): ExpressionParserNode? {
        if (children.isEmpty()) {
            return if (functionMet) this else null
        } else
            return children[0].getFirstListNodeAfterFunction(functionMet || (type == Type.FUNCTION && value.isNotEmpty()))
    }

    fun isFunction() = (type == Type.FUNCTION || type == Type.BINARY_OPERATION || type == Type.UNARY_OPERATION)

    fun getNonEmptyChildren(resultChildren: ArrayList<ExpressionParserNode>, actualStartPosition: Int = -1, actualEndPosition: Int = -1) {
        for (child in children) {
            if (child.startPosition < 0) {
                child.startPosition = actualStartPosition
            }
            if (child.endPosition < 0) {
                child.endPosition = actualEndPosition
            }
            if (child.value.isNotEmpty()) {
                resultChildren.add(child)
            } else {
                child.getNonEmptyChildren(resultChildren, child.startPosition, child.endPosition)
            }
        }
    }

    fun getNodeString(): String {
        if (children.size == 0) return value
        var value = this.value + "("
        for (child in children) value += child.getNodeString() + ';'
        value += ')'
        return value
    }

    override fun toString(): String = getNodeString()

    fun getMinPriorityOfBinaryOperationsWithoutOperands(): Double {
        if (type == Type.BINARY_OPERATION && children.size == 0) return functionStringDefinition!!.function.priority
        var minPriority = Double.MAX_VALUE
        for (child in children) {
            val priority = child.getMinPriorityOfBinaryOperationsWithoutOperands()
            if (priority < minPriority)
                minPriority = priority
        }
        return minPriority
    }

    fun resolveBinaryOperationsWithPriorityRecursive(priority: Double, functionConfiguration: FunctionConfiguration) { //TODO: investigate why we have startPosition = -1 if operation "+" in root; fix positions; example: "a*b + 5"
        var operationWithMinPriority = ExpressionParserNode(Type.BINARY_OPERATION, "", startPosition, endPosition)
        for (child in children) {
            child.resolveBinaryOperationsWithPriorityRecursive(priority, functionConfiguration)
            if (child.type == Type.BINARY_OPERATION &&
                    child.functionStringDefinition!!.function.priority <= priority && child.children.size == 0) {
                operationWithMinPriority.functionStringDefinition = functionConfiguration.fastFindStringDefinitionByNameAndNumberOfArguments(child.functionStringDefinition!!.function.mainFunction, child.functionStringDefinition!!.function.numberOfArguments)
                operationWithMinPriority.value = child.functionStringDefinition!!.function.mainFunction
            }
        }
        if (operationWithMinPriority.value.isEmpty()) return
        operationWithMinPriority.addChild(ExpressionParserNode(Type.FUNCTION, ""))
        for (child in children) {
            if (child.type == Type.BINARY_OPERATION &&
                    child.functionStringDefinition!!.function.mainFunction == operationWithMinPriority.value) {
                if (child.functionStringDefinition!!.function.function == operationWithMinPriority.value) {
                    operationWithMinPriority.addChild(ExpressionParserNode(Type.FUNCTION, ""))
                } else {
                    operationWithMinPriority.addChild(child)
                }
            } else {
                operationWithMinPriority.children.last().addChild(child)
            }
        }
        children.clear()
        addChild(operationWithMinPriority)
    }
}

class ExpressionTreeParser(
        val originalExpression: String,
        val nameForRuleDesignationsPossible: Boolean = false,
        val functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
        val compiledImmediateVariableReplacements: Map<String, String> = mapOf(),
        val isMathML: Boolean = originalExpression.trim().startsWith("<") || originalExpression.trim().endsWith(">") || originalExpression.contains("&#")
) {
    val newLinePlaces = findNewLinePlaces(originalExpression)
    val expression = texStringPrefiltering(removeNewLinesFromExpression(originalExpression))

    private enum class ParserState { WAIT_FOR_NUMBER, NUMBER_READ }
    enum class TokenParserState { UNDEFINED, UNARY_OPERATION, BINARY_OPERATION, NUMBER, NAME, NAME_BREAK, COMMA }

    private var bracketCompleteTagTree: MathMlTagTreeNode = MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", 0, originalExpression.length)
    private var parsedTree: MathMlTagTreeNode = MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", 0, originalExpression.length)
    private var parsedTreeWithMultiples: MathMlTagTreeNode = MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", 0, originalExpression.length)
    private var rootNotPrioritized: ExpressionParserNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", 0, originalExpression.length)
    private var rootNotPrioritizedUnaries: ExpressionParserNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", 0, originalExpression.length)
    private var rootNotPrioritizedWithComplexes: ExpressionParserNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", 0, originalExpression.length)
    private var rootNotPrioritizedWithMultiplications: ExpressionParserNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", 0, originalExpression.length)


    var root: ExpressionNode = ExpressionNode(NodeType.FUNCTION, "", 0, originalExpression.length)
    private var currentPosition = 0

    private fun normalisePositionInParseError(parserError: ParserError): ParserError {
        var shift = 0
        for (i in newLinePlaces) {
            if (parserError.position >= i) {
                shift++
            }
        }
        val endPosition = if (parserError.endPosition < 0) parserError.endPosition
        else parserError.endPosition + shift * StringExtension.newLineMspace.length
        root.nodeType = NodeType.ERROR
        root.startPosition = parserError.position + shift * StringExtension.newLineMspace.length
        root.endPosition = endPosition
        root.value = parserError.description
        return ParserError(parserError.position + shift * StringExtension.newLineMspace.length, parserError.description, endPosition)
    }

    fun parse(): ParserError? {
//        if (expression.contains("/>"))
//            return ParserError(expression.length, "data is not complete")
        // 1. Parse main structure of data: Split data by mathML tags, brackets, '=', ',' and built tree from gotten function and data part nodes
        val parseMathMlTagTreeError = parseMathMlTagTree()
        if (parseMathMlTagTreeError != null) return normalisePositionInParseError(parseMathMlTagTreeError)

        // 2. Parse leaved data parts - as a result we have all data parsed to primary tree with operations and operands from one data part mixed as ordered children of one node
        val parseLeaveStringExpressionPartsError = parseLeaveStringExpressionParts(bracketCompleteTagTree, parsedTree)
        if (parseLeaveStringExpressionPartsError != null) return normalisePositionInParseError(parseLeaveStringExpressionPartsError)

        // 3. Move last multiple functions argument in them (only if expression is in mathML)
        if (isMathML) {
            val multipleFunctionsHandlingError = multipleFunctionsHandling(parsedTree, parsedTreeWithMultiples)
            if (multipleFunctionsHandlingError != null) return normalisePositionInParseError(multipleFunctionsHandlingError)
        } else {
            parsedTreeWithMultiples = parsedTree
        }

        // 4. Resolve functions by its names and number of arguments
        val mathMlTreeToExpressionTreeError = mathMlTreeToExpressionTree(parsedTreeWithMultiples, rootNotPrioritized)
        if (mathMlTreeToExpressionTreeError != null) return normalisePositionInParseError(mathMlTreeToExpressionTreeError)

        // 5. Handle unary operations - translate them to function nodes
        val combineUnaryError = combineUnary(rootNotPrioritized, rootNotPrioritizedUnaries)
        if (combineUnaryError != null) return normalisePositionInParseError(combineUnaryError)

        // 6. Add complexes nodes
        val replaceComplexOneError = replaceComplexOne(rootNotPrioritizedUnaries, rootNotPrioritizedWithComplexes)
        if (replaceComplexOneError != null) return normalisePositionInParseError(replaceComplexOneError)

        // 7. Add multiplications if two neighboring children are not binary operations
        val addMultiplicationsError = addMultiplications(rootNotPrioritizedWithComplexes, rootNotPrioritizedWithMultiplications)
        if (addMultiplicationsError != null) return normalisePositionInParseError(addMultiplicationsError)

        // 8. Resolve binary operations according to customized priorities
        resolveBinaryOperations(rootNotPrioritizedWithMultiplications)
        toExpressionTree(rootNotPrioritizedWithMultiplications, root)
        root.correctPositions()
        root.computeIdentifier()
        root.variableReplacement(compiledImmediateVariableReplacements)
        root.computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot()
        return null
    }

    private fun parseMathMlTagTree(): ParserError? {
        if (expression.isBlank()) {
            return ParserError(0, "No expression found")
        }
        var actualParent = bracketCompleteTagTree
        var numberOfOpenBrackets = 0
        var currentPosition = 0
        var value = ""
        var inTag = false
        var skipMrowCount = 0 //to skip mrow to handle case in test 'testMrowInEnd()'
        actualParent.startPosition = currentPosition
        while (currentPosition < expression.length) {
            if (expression.isWhiteSpace(currentPosition)) {
                currentPosition++
                continue
            }
            val startPosition = currentPosition
            if (isMathML && expression[currentPosition] == '<' && (isComplicatedTag(expression, currentPosition) || actualParent.type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION)) {
                val tagStartPosition = currentPosition
                currentPosition++ // skip '<'
                if (remainingExpressionStartsWith("/", expression, currentPosition)) { //close tag
                    currentPosition++
                    value = readFromRemainingExpressionWhile({ it.isLetter() }, expression, currentPosition)
                    currentPosition += value.length
                    if (!value.startsWith('m')) { //incorrect tag name, but such cases happen sometimes, for example with 'sup'
                        value = 'm' + value
                    }
                    currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, expression, currentPosition)
                    if (value == "mrow" && skipMrowCount > 0 && actualParent.value.substringBefore('_') != "mrow") {
                        skipMrowCount--
                    } else {
                        if (actualParent.value.substringBefore('_') != value) {
                            if (value != "mrow") {
                                return ParserError(startPosition, "Unexpected: '</$value'")
                            }
                        } else { //.substringBefore('_') added to handle cases, where open or close tag attributes value was added to value
                            actualParent.endPosition = tagStartPosition
                            if (value == "mrow" && !actualParent.needDoubleChild) {
                                val node = actualParent
                                val parent = actualParent.parent!!
                                parent.children.removeAt(parent.children.lastIndex)
                                parent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", startPosition))
                                parent.children.last().addChild(node)
                                actualParent = parent
                            } else {
                                actualParent = actualParent.parent!!
                            }
                        }
                    }
                    currentPosition++ // read '>'
                } else {
                    currentPosition-- // undo skip '<'
                    val actualTag = readOpenTagStringIfItPresent(expression, currentPosition)
                    val tagData = getTagAttributes(actualTag!!)
                    val openValue = tagData["open"]?.replace("\"", "")
                    val closeValue = tagData["close"]?.replace("\"", "")
                    value = tagData["name"]!! + if (openValue != null) {
                        "_$openValue"
                    } else {
                        ""
                    } + if (closeValue != null) {
                        "__$closeValue"
                    } else {
                        ""
                    }
                    var nameNotStartWithM = false
                    if (!value.startsWith('m')) { //incorrect tag name, but such cases happen sometimes, for example with 'sup'
                        value = 'm' + value
                        nameNotStartWithM = true
                    }
                    if (value == "mrow" &&
                            actualParent.value !in mathMlTags/* previous version:  && getBracketLevelChangeBeforeClosingTag("mrow", expression, currentPosition) != 0*/) {
                        //skip tag
                        currentPosition += actualTag!!.length
                        skipMrowCount++
                    } else {
                        currentPosition += actualTag!!.length
                        val newParentNode = if (value == "msqrt" || value == "mrow" || value == "math" || value == "mfenced_|__|")
                            MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, value, startPosition)
                        else
                            MathMlTagTreeNode(MathMlTagTreeNode.Type.MATH_ML_FUNCTION, value, startPosition)
                        if (nameNotStartWithM && actualParent.children.isNotEmpty()) {
                            if (actualParent.children.last().type == MathMlTagTreeNode.Type.EXPRESSION_PART_STRING &&
                                    (actualParent.children.last().value.endsWith("</mi>") ||
                                            actualParent.children.last().value.endsWith("</mn>") ||
                                            actualParent.children.last().value.endsWith("</mo>")) &&
                                    actualParent.children.last().value.lastIndexOfAny(listOf("<mi>", "<mo>", "<mn>")) > 0) {
                                val lastSignIndex = actualParent.children.last().value.lastIndexOfAny(listOf("<mi>", "<mo>", "<mn>"))
                                val suffix = actualParent.children.last().value.substring(lastSignIndex)
                                actualParent.children.last().value = actualParent.children.last().value.removeSuffix(suffix)
                                actualParent.addChild(newParentNode)
                                actualParent = actualParent.children.last()
                                actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.EXPRESSION_PART_STRING, suffix, currentPosition, currentPosition))
                            } else {
                                val currentLastChild = actualParent.children.last()
                                actualParent.children.removeAt(actualParent.children.lastIndex)
                                actualParent.addChild(newParentNode)
                                actualParent = actualParent.children.last()
                                actualParent.addChild(currentLastChild)
                            }
                        } else {
                            actualParent.addChild(newParentNode)
                            actualParent = actualParent.children.last()
                        }
                    }
                }
            } else if (expression[currentPosition] == '(' || expression[currentPosition] == '{') {
                actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", startPosition, isLatexArgument = (expression[currentPosition] == '{')))
                actualParent = actualParent.children.last()
                numberOfOpenBrackets++
                currentPosition++
            } else if (expression[currentPosition] == ')' || expression[currentPosition] == '}') {
                if (actualParent.needOneMoreBracket) {
                    actualParent.endPosition = currentPosition + 1
                    actualParent = actualParent.parent!!
                }
                if (actualParent.type != MathMlTagTreeNode.Type.BRACKET_FUNCTION) {
                    if (actualParent.value == "mo") {
                        val upFunctionNode = actualParent.parent!!
                        var upBracketNode = upFunctionNode.parent!!
                        upBracketNode.children.removeAt(upBracketNode.children.lastIndex)
                        val parent = upBracketNode.parent!!
                        parent.children.removeAt(parent.children.lastIndex)
                        upBracketNode.parent = null
                        parent.addChild(upFunctionNode)
                        upBracketNode.endPosition = currentPosition + 1
                        actualParent.addChild(upBracketNode)
                    } else
                        return ParserError(startPosition, "Unexpected ')'")
                } else if (!actualParent.needDoubleChild) {
                    val node = actualParent
                    actualParent.endPosition = currentPosition + 1
                    val parent = actualParent.parent ?: return ParserError(startPosition, "Unexpected ')'")
                    parent.children.removeAt(parent.children.lastIndex)
                    parent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", startPosition, currentPosition + 1))
                    parent.children.last().addChild(node)
                    actualParent = parent
                } else {
                    actualParent.endPosition = currentPosition + 1
                    actualParent = actualParent.parent ?: return ParserError(startPosition, "Unexpected ')'")
                }
                numberOfOpenBrackets--
                currentPosition++
            } else if (expression[currentPosition] == ',') {
                if (actualParent.needOneMoreBracket) {
                    actualParent.endPosition = currentPosition
                    actualParent = actualParent.parent!!
                }
                val parent = actualParent.parent ?: return ParserError(startPosition, "Unexpected ','")
                if (!actualParent.needDoubleChild && parent.type == MathMlTagTreeNode.Type.BRACKET_FUNCTION) {
                    parent.children.removeAt(parent.children.lastIndex)
                    parent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", actualParent.startPosition, currentPosition))
                    parent.children.last().addChild(actualParent)
                    actualParent = parent.children.last()
                    actualParent.needDoubleChild = true
                }
                if (actualParent.type != MathMlTagTreeNode.Type.BRACKET_FUNCTION)
                    return ParserError(startPosition, "Unexpected ','")
                currentPosition++
            } else if (expression[currentPosition] == '=') {
                if (actualParent.needOneMoreBracket) {
                    actualParent.endPosition = currentPosition
                    actualParent = actualParent.parent!!
                }
                if (actualParent.type != MathMlTagTreeNode.Type.BRACKET_FUNCTION)
                    return ParserError(startPosition, "Unexpected '='")
                currentPosition++
            } else { //read data part to string
                while (currentPosition < expression.length) {
                    if (!inTag && (isComplicatedTag(expression, currentPosition) ||
                                    (expression[currentPosition] == '(') ||
                                    (expression[currentPosition] == ')') ||
                                    (expression[currentPosition] == '{') ||
                                    (expression[currentPosition] == '}') ||
                                    (expression[currentPosition] == ',') ||
                                    (expression[currentPosition] == '=') ||
                                    ((expression[currentPosition] == '<') && (actualParent.type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION))))
                        break
                    if (expression[currentPosition] == '<') inTag = true
                    if (expression[currentPosition] == '>') inTag = false
                    if (remainingExpressionStartsWith("\\left(", expression, currentPosition)) {
                        currentPosition += "\\left".length
                        continue
                    } else if (remainingExpressionStartsWith("\\right)", expression, currentPosition)) {
                        currentPosition += "\\right".length
                        continue
                    }
                    value += expression[currentPosition]
                    currentPosition++
                }
                if (currentPosition < expression.length && expression[currentPosition] == '=' && actualParent.isLatexArgument) {
                    actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.EXPRESSION_PART_STRING, value, startPosition, currentPosition))
                    actualParent.endPosition = currentPosition
                    val parent = actualParent.parent ?: return ParserError(startPosition, "Unexpected '='")
                    parent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", startPosition, isLatexArgument = true))
                    actualParent = parent.children.last()
                    currentPosition++
                } else {
                    if (currentPosition < expression.length &&
                            ((expression[currentPosition] == ',') || (expression[currentPosition] == '=') || (expression[currentPosition] == ')'))) {
                        if (actualParent.needOneMoreBracket) {
                            actualParent.endPosition = currentPosition
                            actualParent = actualParent.parent!!
                        }
                    }
                    if (currentPosition < expression.length &&
                            ((expression[currentPosition] == ',') || (expression[currentPosition] == '=') || actualParent.needDoubleChild)) {
                        if (actualParent.children.isEmpty() || actualParent.needDoubleChild || actualParent.children.last().type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION) {
                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", startPosition))
                        }
                        actualParent.needDoubleChild = true
                        actualParent.children.last().addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.EXPRESSION_PART_STRING, value, startPosition, currentPosition))
                        if (expression[currentPosition] == '(') {
                            actualParent = actualParent.children.last()
                            actualParent.needOneMoreBracket = true
                        }
                    } else if (value.isNotBlank()){
                        actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.EXPRESSION_PART_STRING, value, startPosition, currentPosition))
                    }
                }
            }
            value = ""
        }
        if (numberOfOpenBrackets > 0)
            return ParserError(expression.length - 1, "closing bracket missing")
        if (!(actualParent === bracketCompleteTagTree))
            return ParserError(expression.length - 1, "data not ended")
        actualParent.endPosition = currentPosition
        return null
    }

    private fun parseLeaveStringExpressionParts(oldTreeActualParent: MathMlTagTreeNode, newTreeActualParent: MathMlTagTreeNode): ParserError? {
        var state = ParserState.WAIT_FOR_NUMBER
        var newTreeActualParent = newTreeActualParent
        if (newTreeActualParent.value == "mrow") {
            newTreeActualParent = newTreeActualParent.parent!!
            newTreeActualParent.children.removeAt(newTreeActualParent.children.lastIndex)
        }
        var newNodeCurrentPosition = 0
        var leavedActualParentExpectedArgumentsCount = 0
        var nextTexArgumentType = MathMlTagTreeNode.TexArgumentType.USUAL
        var additionPowArgumentExpected = false
        var nodeIndex = 0
        while (nodeIndex < oldTreeActualParent.children.size) {
            val node = oldTreeActualParent.children[nodeIndex]
            var isNewTexFunction = false
            if (node.type == MathMlTagTreeNode.Type.EXPRESSION_PART_STRING) {
                var actualParent = newTreeActualParent
                var currentPosition = newNodeCurrentPosition
                var value = ""
                node.value += " "
                while (currentPosition < node.value.length) {
                    if (node.value.isWhiteSpace(currentPosition)) {
                        currentPosition++
                        continue
                    }
                    if (isMathML && remainingExpressionStartsWith("&#xA0;", node.value, currentPosition)) {
                        currentPosition += 6
                        continue
                    }
                    if (remainingExpressionStartsWith("\\text", node.value, currentPosition)) {
                        currentPosition += 5
                        continue
                    }
                    if (isMathML && node.value[currentPosition] == '<' && !isComplicatedTag(node.value, currentPosition)) {
                        currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, node.value, currentPosition)
                        currentPosition++
                        continue
                    }
                    if (leavedActualParentExpectedArgumentsCount > 0) {
                        var isFunctionArgumentPart = leavedActualParentExpectedArgumentsCount == 1
                        if (node.value[currentPosition].isTexArgumentsSeparator()) {
                            if (node.value[currentPosition] == '^') {
                                nextTexArgumentType = MathMlTagTreeNode.TexArgumentType.POW
                                if (additionPowArgumentExpected) {
                                    leavedActualParentExpectedArgumentsCount++
                                    additionPowArgumentExpected = false
                                }
                            } else if (node.value[currentPosition] == '_') {
                                nextTexArgumentType = MathMlTagTreeNode.TexArgumentType.UNDERLINED
                            }
                            isFunctionArgumentPart = true
                            currentPosition++
                        }
                        if (currentPosition >= node.value.length || node.value.isWhiteSpace(currentPosition)) {
                            currentPosition++
                            continue
                        }
                        if (isFunctionArgumentPart) {
                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.NAME, node.value[currentPosition].toString(),
                                    node.startPosition + currentPosition, node.startPosition + currentPosition + 1, texArgumentType = nextTexArgumentType))
                            nextTexArgumentType = MathMlTagTreeNode.TexArgumentType.USUAL
                            leavedActualParentExpectedArgumentsCount--
                            isNewTexFunction = true
                            if (leavedActualParentExpectedArgumentsCount == 0) {
                                if (newTreeActualParent.parent != null) {
                                    newTreeActualParent = newTreeActualParent.parent!!
                                    actualParent = newTreeActualParent
                                } else {
                                    return ParserError(node.startPosition + currentPosition, "Expected for closing bracket")
                                }
                            }
                            currentPosition++
                            continue
                        }
                    }
                    val startPosition = currentPosition
                    if (node.value[currentPosition] == '\\' && currentPosition + 1 < node.value.length && node.value[currentPosition+1].isLatinLetter()) {
                        val texName = '\\' + readFromRemainingExpressionWhile({ it.isLatinLetter() }, node.value, currentPosition + 1)
                        val texFunction =  functionConfiguration.slashToSpaceFunctionDefinitionsMap.get(texName)  //getTexFunction(node.value, currentPosition)
                        if (texFunction != null) {
                            currentPosition += texName.length
                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, texName,
                                    node.startPosition + startPosition, node.startPosition + currentPosition - 1,
                                    stringDefinitionType = texFunction.definitionType))
                            leavedActualParentExpectedArgumentsCount = texFunction.definitionArgumentsCount
                            additionPowArgumentExpected = texFunction.powSeparatedAsPow
                            if (leavedActualParentExpectedArgumentsCount > 0) {
                                newTreeActualParent = actualParent.children.last()
                                actualParent = newTreeActualParent
                                isNewTexFunction = true
                            }
                            continue
                        }
                        val variable = texToUnicode.get(texName)
                        if (variable != null) {
                            currentPosition += texName.length
                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.NAME, variable,
                                    node.startPosition + startPosition, node.startPosition + startPosition - 1))
                            state = ParserState.NUMBER_READ
                            continue
                        }
                    }
                    val completeSign = getCompleteSign(node.value, currentPosition)
                    if (completeSign != null) {
                        currentPosition += completeSign.first.length
                        state = ParserState.WAIT_FOR_NUMBER
                        actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.OPERATION, completeSign!!.first,
                                node.startPosition + startPosition, node.startPosition + currentPosition - 1,
                                stringDefinitionType = completeSign.second))
                        if (completeSign.second == StringDefinitionType.BINARY_OPERATION){
                            state = ParserState.WAIT_FOR_NUMBER
                        }
                    } else if (isBinarySignPart(node.value[currentPosition], node.value.prevCharOrSpace(currentPosition), isMathML, functionConfiguration.hasUpAnd())) {
                        value += node.value[currentPosition]
                        currentPosition++
                        while (currentPosition < node.value.length) {
                            if (isMathML && node.value[currentPosition] == '<' && !isComplicatedTag(node.value, currentPosition)) {
                                currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, node.value, currentPosition)
                                currentPosition++
                                continue
                            }
                            if (isBinarySignPart(node.value[currentPosition], node.value.prevCharOrSpace(currentPosition), isMathML, functionConfiguration.hasUpAnd()) &&
                                    getTexFunction(node.value, currentPosition) == null) {
                                value += node.value[currentPosition]
                                currentPosition++
                            } else {
                                actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.OPERATION, value,
                                        node.startPosition + startPosition, node.startPosition + currentPosition - 1,
                                        stringDefinitionType = StringDefinitionType.BINARY_OPERATION))
                                break
                            }
                        }
                        state = ParserState.WAIT_FOR_NUMBER
                    } else if (isUnarySignPart(node.value[currentPosition])) {
                        value += node.value[currentPosition]
                        currentPosition++
                        while (currentPosition < node.value.length) {
                            if (isMathML && node.value[currentPosition] == '<' && !isComplicatedTag(node.value, currentPosition)) {
                                currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, node.value, currentPosition)
                                currentPosition++
                                continue
                            }
                            if (isUnarySignPart(node.value[currentPosition])) {
                                value += node.value[currentPosition]
                                currentPosition++
                            } else {
                                actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.OPERATION, value,
                                        node.startPosition + startPosition, node.startPosition + currentPosition - 1,
                                        stringDefinitionType =
                                        if (state == ParserState.WAIT_FOR_NUMBER) StringDefinitionType.UNARY_LEFT_OPERATION
                                        else StringDefinitionType.UNARY_RIGHT_OPERATION))
                                break
                            }
                        }
                    } else if (node.value[currentPosition].isNamePart() || (node.value[currentPosition] == '&' && isMathML)) {
                        state = ParserState.NUMBER_READ
                        if (node.value[currentPosition].isNamePart()) {
                            value += node.value[currentPosition]
                            currentPosition++
                        } else {
                            val specialSymbol = readFromRemainingExpressionWhile({ it != ';' }, node.value, currentPosition)
                            currentPosition += specialSymbol.length + 1
                            value += specialSymbol + ";"
                        }
                        while (currentPosition < node.value.length) {
                            if (isMathML && node.value[currentPosition] == '<' && !isComplicatedTag(node.value, currentPosition)) {
                                currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, node.value, currentPosition)
                                currentPosition++
                                continue
                            }
                            if (node.value[currentPosition].isNamePart() ||
                                    node.value[currentPosition] == '.' || (node.value[currentPosition] == '&' && isMathML)) {
                                if (node.value[currentPosition] != '&') {
                                    value += node.value[currentPosition]
                                    currentPosition++
                                } else {
                                    val specialSymbol = readFromRemainingExpressionWhile({ it != ';' }, node.value, currentPosition)
                                    currentPosition += specialSymbol.length + 1
                                    value += specialSymbol + ";"
                                }
                            } else {
                                var i = 0
                                while (i < value.length) {
                                    var currentValue = StringBuilder()
                                    val startI = i
                                    if (value[i].isDigit() || value[i] == '.') {
                                        while (i < value.length && (value[i].isDigit() || value[i] == '.')) {
                                            currentValue.append(value[i])
                                            i++
                                        }
                                        if (currentValue.isNotEmpty())
                                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.NAME, currentValue.toString(),
                                                    node.startPosition + startPosition + startI, node.startPosition + startPosition + i))
                                    } else if (value[i] == '&') {
                                        val specialSymbol = readFromRemainingExpressionWhile({ it != ';' }, value, i)
                                        i += specialSymbol.length + 1
                                        currentValue.append(specialSymbol)
                                        if (!currentValue.startsWith("&#xA0"))
                                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.NAME, currentValue.toString(),
                                                    node.startPosition + startPosition + startI, node.startPosition + startPosition + i))
                                    } else {
                                        while (i < value.length && value[i] != '.' && value[i] != '&') {
                                            currentValue.append(value[i])
                                            i++
                                        }
                                        if (currentValue.isNotEmpty())
                                            actualParent.addChild(MathMlTagTreeNode(MathMlTagTreeNode.Type.NAME, currentValue.toString(),
                                                    node.startPosition + startPosition + startI, node.startPosition + startPosition + i))
                                    }
                                }
                                break
                            }
                        }
                    } else {
                        return ParserError(node.startPosition + startPosition, "Unexpected: '" + node.value[currentPosition] + "'")
                    }
                    value = ""
                }
                if (newTreeActualParent.value.isNotEmpty() && !isNewTexFunction && actualParent.children.size == 0) {
                    newTreeActualParent.children.removeAt(newTreeActualParent.children.lastIndex)
                }
            } else {
                if (isMathML && (node.value == "math" || node.value == "mn" || node.value == "mo" || node.value == "mi" ||
                                node.value == "mspace" || node.value == "mtext" || node.value == "mstack" || node.value == "maction"))
                    node.value = ""
                if (!isMathML || (node.value != "mstyle")) {
                    newTreeActualParent.addChild(node.copy())
                    state = ParserState.NUMBER_READ
                    val res = parseLeaveStringExpressionParts(node, newTreeActualParent.children.last())
                    if (res != null) return res
                    if (newTreeActualParent.children.last().value.isEmpty() && newTreeActualParent.children.last().children.isEmpty())
                        newTreeActualParent.children.removeAt(newTreeActualParent.children.lastIndex)
                } else {
                    state = ParserState.NUMBER_READ
                    val res = parseLeaveStringExpressionParts(node, newTreeActualParent)
                    if (res != null) return res
                }
            }
            if (leavedActualParentExpectedArgumentsCount > 0 && !isNewTexFunction) {
                newTreeActualParent.children.last().texArgumentType = nextTexArgumentType
                nextTexArgumentType = MathMlTagTreeNode.TexArgumentType.USUAL
                leavedActualParentExpectedArgumentsCount--
                if (leavedActualParentExpectedArgumentsCount == 0) {
                    if (newTreeActualParent.parent != null) {
                        newTreeActualParent = newTreeActualParent.parent!!
                    } else {
                        return ParserError(node.startPosition + currentPosition, "Expected for closing bracket")
                    }
                }
            }
            nodeIndex++
        }
        return null
    }

    private fun multipleFunctionsHandling(oldTreeActualParent: MathMlTagTreeNode, newTreeActualParent: MathMlTagTreeNode, curIndex: Int = 0): ParserError? {
        var i = 0
        var currentNewNode: MathMlTagTreeNode? = null
        if (oldTreeActualParent.type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION &&
                isMathMlMultipleTag(oldTreeActualParent.value, 0)) {
            if (oldTreeActualParent.children.isNotEmpty() && oldTreeActualParent.children[0].children.isNotEmpty() && oldTreeActualParent.children[0].children[0].value.isNotBlank() &&
                    !oldTreeActualParent.toString().contains('\'')) {
                newTreeActualParent.value += oldTreeActualParent.children[0].children[0].value
                if (oldTreeActualParent.children[0].children.size > 1) {
                    val exprParent = MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", parent = oldTreeActualParent.parent)
                    for (j in 1 until oldTreeActualParent.children[0].children.size) {
                        exprParent.addChild(oldTreeActualParent.children[0].children[j])
                    }
                    oldTreeActualParent.parent!!.children.add(curIndex + 1, exprParent)
                }
                i++
            } else if (oldTreeActualParent.value == "msubsup") {
                newTreeActualParent.value += oldTreeActualParent.children.last().children[0].value
                newTreeActualParent.type = MathMlTagTreeNode.Type.BRACKET_FUNCTION
                oldTreeActualParent.children.removeAt(oldTreeActualParent.children.lastIndex)
            }
        }
        while (i < oldTreeActualParent.children.size) {
            if (oldTreeActualParent.children[i].value == "mfenced") {  //while we does not need functions like [], {}
                oldTreeActualParent.children[i].value = ""
            }
            if (i + 1 < oldTreeActualParent.children.size && oldTreeActualParent.children[i + 1].value == "msup" &&
                    oldTreeActualParent.children[i].type == MathMlTagTreeNode.Type.NAME &&
                    oldTreeActualParent.children[i].value.isNotEmpty() &&
                    (oldTreeActualParent.children[i].value[0].isLetterOrUnderscore()) &&
                    oldTreeActualParent.children[i + 1].children[0].children.size > 0 && oldTreeActualParent.children[i + 1].children[0].value.isEmpty() &&
                    oldTreeActualParent.children[i + 1].children[0].children[0].type == MathMlTagTreeNode.Type.NAME &&
                    oldTreeActualParent.children[i + 1].children[0].children[0].value.isNotEmpty() &&
                    (oldTreeActualParent.children[i + 1].children[0].children[0].value[0].isLetterOrUnderscore())) {
                oldTreeActualParent.children[i + 1].children[0].children[0].value = oldTreeActualParent.children[i].value +
                        oldTreeActualParent.children[i + 1].children[0].children[0].value
                i++
                continue
            }
            var newNode = oldTreeActualParent.children[i].copy()
            multipleFunctionsHandling(oldTreeActualParent.children[i], newNode, i)
            if (oldTreeActualParent.children[i].type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION &&
                    isMathMlMultipleTag(oldTreeActualParent.children[i].value, 0) &&
                    !newNode.toString().contains('\'')) {
                if (i + 1 >= oldTreeActualParent.children.size ||
                        oldTreeActualParent.children[i + 1].type == MathMlTagTreeNode.Type.OPERATION)
                    return ParserError(oldTreeActualParent.children[i].startPosition, "data is not complete")
                if (currentNewNode == null) {
                    newTreeActualParent.addChild(newNode)
                } else {
                    currentNewNode.addChild(newNode)
                }
                currentNewNode = newNode
            } else {
                if (currentNewNode == null) {
                    newTreeActualParent.addChild(newNode)
                } else {
                    currentNewNode.addChild(newNode)
                }
                currentNewNode = null
            }
            i++
        }
        return null
    }

    private fun getExpressionParserNodeOperationType(value: String, type: StringDefinitionType?) = if (isUnarySignPart(value[0]) || type == StringDefinitionType.UNARY_LEFT_OPERATION || type == StringDefinitionType.UNARY_RIGHT_OPERATION) ExpressionParserNode.Type.UNARY_OPERATION
    else ExpressionParserNode.Type.BINARY_OPERATION

    private fun mathMlTreeToExpressionTree(oldTreeActualParent: MathMlTagTreeNode, newTreeActualParent: ExpressionParserNode): ParserError? {
        var i = 0
        val cashedChildExpressionTreesMap = mutableMapOf<Int, ExpressionParserNode>()
        while (i < oldTreeActualParent.children.size) {
            if (oldTreeActualParent.children[i].value == "") {
                if (i > 0 && oldTreeActualParent.children[i - 1].type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION &&
                        newTreeActualParent.children.size > 0 && newTreeActualParent.children.last().children.size > 0) {
                    val possibleFunctionNode = newTreeActualParent.children.last().getFirstListNodeAfterFunction()
                    if (possibleFunctionNode != null && possibleFunctionNode.type == ExpressionParserNode.Type.VARIABLE) {
                        val functionDefinition = functionConfiguration.findFunctionStringDefinition(possibleFunctionNode.value,
                                StringDefinitionType.FUNCTION, oldTreeActualParent.children[i].children.size, nameForRuleDesignationsPossible)
                        if (functionDefinition != null) {
                            possibleFunctionNode.type = ExpressionParserNode.Type.FUNCTION
                            possibleFunctionNode.functionStringDefinition = functionDefinition
                            possibleFunctionNode.endPosition = oldTreeActualParent.children[i].endPosition
                            val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], possibleFunctionNode)
                            if (res != null) return res
                            i++
                            continue
                        }
                    }
                }
                if (cashedChildExpressionTreesMap[i] != null) {
                    newTreeActualParent.addChild(cashedChildExpressionTreesMap[i]!!)
                } else {
                    newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "",
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition))
                    val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                    if (res != null) return res
                }
                i++
                continue
            } else if (oldTreeActualParent.children[i].value == "msub") {
                val mainValue = oldTreeActualParent.children[i].children[0].getNodeString()
                val subValue = oldTreeActualParent.children[i].children[1].getNodeString()
                val newValue = mainValue + "," + subValue
                if (isUnarySignPart(newValue[0]) || isBinarySignPart(newValue[0], ' ', isMathML) || isCompleteBinarySign(newValue, 0)) {
                    val functionDefinition = functionConfiguration.findFunctionStringDefinition(newValue,
                            oldTreeActualParent.children[i].stringDefinitionType!!, -1)
                    if (functionDefinition == null)
                        return ParserError(oldTreeActualParent.children[i].startPosition, "Unknown operation: '$newValue'")
                    else
                        newTreeActualParent.addChild(ExpressionParserNode(getExpressionParserNodeOperationType(newValue, null), functionDefinition.function.function,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                                functionStringDefinition = functionDefinition))
                } else {
                    var functionDefinition: FunctionStringDefinition? = null
                    if ((i + 1) < oldTreeActualParent.children.size)
                        functionDefinition = functionConfiguration.findFunctionStringDefinition(newValue, StringDefinitionType.FUNCTION,
                                oldTreeActualParent.children[i + 1].children.size, nameForRuleDesignationsPossible)
                    if (functionDefinition == null || oldTreeActualParent.children[i + 1].value != "") {
                        if ((i + 1) < oldTreeActualParent.children.size)
                            functionDefinition = functionConfiguration.findFunctionStringDefinition(mainValue, StringDefinitionType.FUNCTION,
                                    oldTreeActualParent.children[i + 1].children.size + 1, nameForRuleDesignationsPossible, true)
                        if (functionDefinition == null || oldTreeActualParent.children[i + 1].value != "")
                            newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, newValue,
                                    oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition, subValue))
                        else {
                            if (oldTreeActualParent.children[i + 1].children.isEmpty()) {
                                oldTreeActualParent.children[i + 1].addChild(oldTreeActualParent.children[i + 1].copy())
                                oldTreeActualParent.children[i + 1].value = ""
                                oldTreeActualParent.children[i + 1].type = MathMlTagTreeNode.Type.BRACKET_FUNCTION
                            }
                            oldTreeActualParent.children[i + 1].addChild(oldTreeActualParent.children[i].children[1])
                            newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                                    oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 1].endPosition,
                                    functionStringDefinition = functionDefinition))
                            i++
                            val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                            if (res != null) return res
                        }
                    } else {
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 1].endPosition,
                                functionStringDefinition = functionDefinition))
                        i++
                        val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                        if (res != null) return res
                    }
                }
                i++
                continue
            } else if (oldTreeActualParent.children[i].type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION) {
                val functionDefinition = functionConfiguration.findFunctionStringDefinition(oldTreeActualParent.children[i].value,
                        StringDefinitionType.FUNCTION, oldTreeActualParent.children[i].children.size, nameForRuleDesignationsPossible)
                        ?: return ParserError(oldTreeActualParent.children[i].startPosition,
                                "Unknown function: '" + oldTreeActualParent.children[i].value + "' with '" +
                                        oldTreeActualParent.children[i].children.size + "' arguments")
                newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                        oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                        functionStringDefinition = functionDefinition))
                val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                if (res != null) return res
                i++
                continue
            } else if (i + 1 < oldTreeActualParent.children.size &&
                    oldTreeActualParent.children[i + 1].type == MathMlTagTreeNode.Type.MATH_ML_FUNCTION &&
                    oldTreeActualParent.children[i + 1].value == "msub") {
                val mainValue = oldTreeActualParent.children[i + 1].children[0].getNodeString()
                var functionDefinition: FunctionStringDefinition? = null
                if ((i + 2) < oldTreeActualParent.children.size) {
                    functionDefinition = functionConfiguration.findFunctionStringDefinition(mainValue, StringDefinitionType.FUNCTION,
                            oldTreeActualParent.children[i + 2].children.size + 1, nameForRuleDesignationsPossible, true)
                    if (functionDefinition != null) {
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, oldTreeActualParent.children[i].value,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition))
                        if (oldTreeActualParent.children[i + 2].children.isEmpty()) {
                            oldTreeActualParent.children[i + 2].addChild(oldTreeActualParent.children[i + 2].copy())
                            oldTreeActualParent.children[i + 2].value = ""
                            oldTreeActualParent.children[i + 2].type = MathMlTagTreeNode.Type.BRACKET_FUNCTION
                        }
                        oldTreeActualParent.children[i + 2].addChild(oldTreeActualParent.children[i + 1].children[1])
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 2].endPosition,
                                functionStringDefinition = functionDefinition))
                        i += 2
                        val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                        if (res != null) return res
                        i++
                        continue
                    }
                }
                val iPlusOneTypeOperation = (isUnarySignPart(mainValue[0]) || isBinarySignPart(mainValue[0], ' ', isMathML) ||
                        isCompleteBinarySign(mainValue, 0))
                val iTypeOperation = (isUnarySignPart(oldTreeActualParent.children[i].value[0]) || isBinarySignPart(oldTreeActualParent.children[i].value[0], ' ', isMathML) ||
                        isCompleteBinarySign(oldTreeActualParent.children[i].value, 0))
                if (iPlusOneTypeOperation == iTypeOperation) {
                    val subValue = oldTreeActualParent.children[i + 1].children[1].getNodeString()
                    val newValue = oldTreeActualParent.children[i].value + mainValue + "," + subValue
                    if (isUnarySignPart(newValue[0]) || isBinarySignPart(newValue[0], ' ', isMathML) || isCompleteBinarySign(newValue, 0)) {
                        val functionDefinition = functionConfiguration.findFunctionStringDefinition(newValue,
                                oldTreeActualParent.children[i].stringDefinitionType!!, -1)
                        if (functionDefinition == null)
                            return ParserError(oldTreeActualParent.children[i].startPosition, "Unknown operation: '$newValue'")
                        else
                            newTreeActualParent.addChild(ExpressionParserNode(getExpressionParserNodeOperationType(newValue, null), functionDefinition.function.function,
                                    oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 1].endPosition,
                                    functionStringDefinition = functionDefinition))
                    } else {
                        val functionDefinition = functionConfiguration.findFunctionStringDefinition(newValue, StringDefinitionType.FUNCTION,
                                oldTreeActualParent.children[i + 2].children.size, nameForRuleDesignationsPossible)
                        if (functionDefinition == null || (i + 2) >= oldTreeActualParent.children.size ||
                                oldTreeActualParent.children[i + 2].value != "")
                            newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, newValue,
                                    oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 1].endPosition, subValue))
                        else {
                            newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                                    oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 2].endPosition,
                                    functionStringDefinition = functionDefinition))
                            i++
                            val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i + 2], newTreeActualParent.children.last())
                            if (res != null) return res
                        }
                    }
                    i++
                    continue
                }
            }
            if (oldTreeActualParent.children[i].type == MathMlTagTreeNode.Type.OPERATION) {
                val functionDefinition = functionConfiguration.findFunctionStringDefinition(oldTreeActualParent.children[i].value,
                        oldTreeActualParent.children[i].stringDefinitionType!!,
                        if (oldTreeActualParent.children[i].stringDefinitionType!! == StringDefinitionType.BINARY_OPERATION) -1 else 1)
                if (functionDefinition == null)
                    return ParserError(oldTreeActualParent.children[i].startPosition, "Unknown operation: '" + oldTreeActualParent.children[i].value + "'")
                else if (i == oldTreeActualParent.children.lastIndex && oldTreeActualParent.children[i].stringDefinitionType!! != StringDefinitionType.UNARY_RIGHT_OPERATION) {
                    return ParserError(oldTreeActualParent.children[i].startPosition, "Operation: '" + oldTreeActualParent.children[i].value + "' is not right unary")
                } else
                    newTreeActualParent.addChild(ExpressionParserNode(getExpressionParserNodeOperationType(oldTreeActualParent.children[i].value, oldTreeActualParent.children[i].stringDefinitionType), functionDefinition.function.function,
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                            functionStringDefinition = functionDefinition))
                i++
                continue
            } else if (oldTreeActualParent.children[i].type == MathMlTagTreeNode.Type.NAME || oldTreeActualParent.children[i].children.isEmpty()) {
                if (oldTreeActualParent.children[i].value.startsWith("&")) {
                    var functionDefinition = functionConfiguration.findFunctionStringDefinition(oldTreeActualParent.children[i].value,
                            StringDefinitionType.BINARY_OPERATION, -1)
                    if (functionDefinition == null)
                        functionDefinition = functionConfiguration.findFunctionStringDefinition(oldTreeActualParent.children[i].value,
                                StringDefinitionType.UNARY_LEFT_OPERATION, 1)
                    else {
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.BINARY_OPERATION, functionDefinition.function.function,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                                functionStringDefinition = functionDefinition))
                        i++
                        continue
                    }
                    if (functionDefinition != null) {
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.UNARY_OPERATION, functionDefinition.function.function,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                                functionStringDefinition = functionDefinition))
                        i++
                        continue
                    }
                }
                if ((i + 1) >= oldTreeActualParent.children.size ||
                        oldTreeActualParent.children[i + 1].value != "")
                    newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, oldTreeActualParent.children[i].value,
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition))
                else {
                    val newParentNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "")
                    val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i + 1], newParentNode)
                    if (res != null) return res
                    val functionDefinition = functionConfiguration.findFunctionStringDefinition(oldTreeActualParent.children[i].value,
                            StringDefinitionType.FUNCTION, newParentNode/*oldTreeActualParent.children[i + 1]*/.children.size, nameForRuleDesignationsPossible)
                    if (functionDefinition == null) {
                        cashedChildExpressionTreesMap.put(i + 1, newParentNode)
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, oldTreeActualParent.children[i].value,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition))
                    } else {
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i + 1].endPosition,
                                functionStringDefinition = functionDefinition))
// it does not work; todo: investigate, why
//                        for (child in newParentNode.children){
//                            newTreeActualParent.children.last().addChild(child)
//                        }
                        val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i + 1], newTreeActualParent.children.last())
                        if (res != null) return res
                        i++
                    }
                }
                i++
                continue
            } else {
                val functionDefinition = functionConfiguration.findFunctionStringDefinition(oldTreeActualParent.children[i].value,
                        StringDefinitionType.FUNCTION, oldTreeActualParent.children[i].children.size, nameForRuleDesignationsPossible)
                        ?: return ParserError(oldTreeActualParent.children[i].startPosition,
                                "Unknown function: '" + oldTreeActualParent.children[i].value + "' with '" +
                                        oldTreeActualParent.children[i].children.size + "' arguments")
                if (functionDefinition.powSeparatedAsPow && oldTreeActualParent.children[i].children.any { it.texArgumentType == MathMlTagTreeNode.TexArgumentType.POW }){
                    newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "^",
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                            functionStringDefinition = functionConfiguration.fastFindStringDefinitionByNameAndNumberOfArguments("^", -1)!!))
                    val powIndex = oldTreeActualParent.children[i].children.indexOfFirst { it.texArgumentType == MathMlTagTreeNode.TexArgumentType.POW }
                    val pow = MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", oldTreeActualParent.children[i].children[powIndex].startPosition, oldTreeActualParent.children[i].children[powIndex].endPosition)
                    pow.addChild(oldTreeActualParent.children[i].children[powIndex])
                    oldTreeActualParent.children[i].children.removeAt(powIndex)
                    val currentFunctionParent = MathMlTagTreeNode(MathMlTagTreeNode.Type.BRACKET_FUNCTION, "", oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition)
                    currentFunctionParent.addChild(oldTreeActualParent.children[i])
                    var res = mathMlTreeToExpressionTree(currentFunctionParent, newTreeActualParent.children.last())
                    if (res != null) return res
                    res = mathMlTreeToExpressionTree(pow, newTreeActualParent.children.last())
                    if (res != null) return res
                } else {
                    newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, functionDefinition.function.function,
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition,
                            functionStringDefinition = functionDefinition))
                    if (functionDefinition.underlinedAsLast) {
                        val underlinedIndex = oldTreeActualParent.children[i].children.indexOfFirst { it.texArgumentType == MathMlTagTreeNode.TexArgumentType.UNDERLINED }
                        oldTreeActualParent.children[i].children.swap(underlinedIndex, oldTreeActualParent.children[i].children.lastIndex)
                    }
                    val res = mathMlTreeToExpressionTree(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                    if (res != null) return res
                }
                i++
                continue
            }
        }
        return null
    }

    private val completeBinarySigns = listOf("&amp;", "->", "-</mo><mo>&gt;")

    private fun isCompleteBinarySign(value: String, currentPosition: Int) = completeBinarySigns.any { remainingExpressionStartsWith(it, value, currentPosition) }

    private fun getCompleteSign(value: String, currentPosition: Int): Pair<String, StringDefinitionType>? {
        for (sign in completeBinarySigns) {
            if (remainingExpressionStartsWith(sign, value, currentPosition)) {
                return Pair(sign, StringDefinitionType.BINARY_OPERATION)
            }
        }
        for (sign in functionConfiguration.slashToSpaceDefinitions) {
            if (remainingExpressionStartsWith(sign.first, value, currentPosition)) {
                return sign
            }
        }
        return null
    }

    private fun getTexFunction(value: String, currentPosition: Int): Pair<String, FunctionStringDefinition>? {
        for (function in functionConfiguration.slashToSpaceFunctionDefinitions) {
            if (remainingExpressionStartsWith(function.first, value, currentPosition)) {
                return function
            }
        }
        return null
    }

    private var isNeedReplace: Boolean = true
    private var SorPLevels = ArrayList<Int>()
    private var notReplacementVariables = mutableMapOf<Int, String>()
    private val complexVar: String = "sys_def_i_complex"
    private val lenOfComplexVar: Int = complexVar.length

    private fun replaceComplexOne(
        oldTreeActualParent: ExpressionParserNode,
        newTreeActualParent: ExpressionParserNode,
        currentLevel: Int = 0,
        onlyZeroNumberOfChildInSorP: Boolean = false
    ): ParserError? {
        if (oldTreeActualParent.children.size == 0) {
            return null
        }
        var i = 0
        if (((oldTreeActualParent.value == "P") || (oldTreeActualParent.value == "S")) && (oldTreeActualParent.children.size == 4)) {
            SorPLevels.add(currentLevel)
        }
        if (SorPLevels.isNotEmpty() && (SorPLevels[SorPLevels.lastIndex] < currentLevel) && onlyZeroNumberOfChildInSorP && (oldTreeActualParent.children[0].value.isNotEmpty()) && (oldTreeActualParent.children[0].value[0] == 'i' || oldTreeActualParent.children[0].value.last() == 'i')){
            notReplacementVariables[SorPLevels[SorPLevels.lastIndex]] = oldTreeActualParent.children[0].value
        }
        while (i < oldTreeActualParent.children.size) {
            if ((oldTreeActualParent.children[i].type == ExpressionParserNode.Type.VARIABLE) && !notReplacementVariables.containsValue(oldTreeActualParent.children[i].value)) {
                when(oldTreeActualParent.children[i].value) {
                    "i" -> {
                        newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, complexVar,
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].startPosition + 1))
                    }
                    "pi" -> {
                        newTreeActualParent.addChild(oldTreeActualParent.children[i].copy())
                    }
                    "ipi" -> {
                        val newNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition + 1)
                        newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, complexVar,
                            oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].startPosition + 1))
                        newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.BINARY_OPERATION, "*",
                            oldTreeActualParent.children[i].startPosition + 1, oldTreeActualParent.children[i].startPosition + 2,
                            functionStringDefinition = functionConfiguration.findFunctionStringDefinition("*", StringDefinitionType.BINARY_OPERATION, -1)))
                        newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, oldTreeActualParent.children[i].value.substring(1),
                            oldTreeActualParent.children[i].startPosition + 2, oldTreeActualParent.children[i].endPosition + 1))
                        newTreeActualParent.addChild(newNode)
                    } else -> {
                    when('i') {
                        oldTreeActualParent.children[i].value.last() -> {
                            val newNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition + 1)
                            newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, oldTreeActualParent.children[i].value.substring(0, oldTreeActualParent.children[i].value.lastIndex),
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition - 1))
                            newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.BINARY_OPERATION, "*",
                                oldTreeActualParent.children[i].endPosition - 1, oldTreeActualParent.children[i].endPosition,
                                functionStringDefinition = functionConfiguration.findFunctionStringDefinition("*", StringDefinitionType.BINARY_OPERATION, -1)))
                            newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, complexVar,
                                oldTreeActualParent.children[i].endPosition, oldTreeActualParent.children[i].endPosition + 1))
                            newTreeActualParent.addChild(newNode)
                        }
                        oldTreeActualParent.children[i].value[0] -> {
                            val newNode = ExpressionParserNode(ExpressionParserNode.Type.FUNCTION, "", oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].endPosition + 1)
                            newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, complexVar,
                                oldTreeActualParent.children[i].startPosition, oldTreeActualParent.children[i].startPosition + 1))
                            newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.BINARY_OPERATION, "*",
                                oldTreeActualParent.children[i].startPosition + 1, oldTreeActualParent.children[i].startPosition + 2,
                                functionStringDefinition = functionConfiguration.findFunctionStringDefinition("*", StringDefinitionType.BINARY_OPERATION, -1)))
                            newNode.addChild(ExpressionParserNode(ExpressionParserNode.Type.VARIABLE, oldTreeActualParent.children[i].value.substring(1),
                                oldTreeActualParent.children[i].startPosition + 2, oldTreeActualParent.children[i].endPosition + 1))
                            newTreeActualParent.addChild(newNode)
                        } else -> {
                        newTreeActualParent.addChild(oldTreeActualParent.children[i].copy())
                    }
                    }
                }
                }
            } else {
                newTreeActualParent.addChild(oldTreeActualParent.children[i].copy())
            }
            val res = replaceComplexOne(oldTreeActualParent.children[i], newTreeActualParent.children.last(), currentLevel + 1, onlyZeroNumberOfChildInSorP = ((i == 0) && (onlyZeroNumberOfChildInSorP || (SorPLevels.isNotEmpty() && currentLevel == SorPLevels[SorPLevels.lastIndex]))))
            if (res != null) {
                return res
            }
            i++
        }
        if (SorPLevels.isNotEmpty() && SorPLevels[SorPLevels.lastIndex] == currentLevel) {
            notReplacementVariables.remove(currentLevel)
            SorPLevels.remove(currentLevel)
        }
        return null
    }

    private fun addMultiplications(oldTreeActualParent: ExpressionParserNode, newTreeActualParent: ExpressionParserNode): ParserError? {
        if (oldTreeActualParent.children.size == 0) return null
        var i = 0
        if (oldTreeActualParent.value.isNotEmpty()) {
            while (i < oldTreeActualParent.children.size) {
                newTreeActualParent.addChild(oldTreeActualParent.children[i].copy())
                val res = addMultiplications(oldTreeActualParent.children[i], newTreeActualParent.children[i])
                if (res != null) return res
                i++
            }
        } else {
            newTreeActualParent.addChild(oldTreeActualParent.children[0].copy())
            val res = addMultiplications(oldTreeActualParent.children[0], newTreeActualParent.children[0])
            if (res != null) return res
            i++
            while (i < oldTreeActualParent.children.size) {
                if (!(oldTreeActualParent.children[i].type == ExpressionParserNode.Type.BINARY_OPERATION) &&
                        !(oldTreeActualParent.children[i - 1].type == ExpressionParserNode.Type.BINARY_OPERATION)) {
                    newTreeActualParent.addChild(ExpressionParserNode(ExpressionParserNode.Type.BINARY_OPERATION, "*",
                            oldTreeActualParent.children[i].startPosition - 1, oldTreeActualParent.children[i].startPosition,
                            functionStringDefinition = functionConfiguration.findFunctionStringDefinition("*", StringDefinitionType.BINARY_OPERATION, -1)))
                } else if (oldTreeActualParent.children[i].type == ExpressionParserNode.Type.BINARY_OPERATION &&
                        oldTreeActualParent.children[i - 1].type == ExpressionParserNode.Type.BINARY_OPERATION)
                    return ParserError(oldTreeActualParent.children[i].startPosition,
                            "Unexpected binary operation '" + oldTreeActualParent.children[i].functionStringDefinition!!.definition + "'")
                newTreeActualParent.addChild(oldTreeActualParent.children[i].copy())
                val res = addMultiplications(oldTreeActualParent.children[i], newTreeActualParent.children.last())
                if (res != null) return res
                i++
            }
        }
        return null
    }

    private fun combineUnary(oldTreeActualParent: ExpressionParserNode, newTreeActualParent: ExpressionParserNode): ParserError? {
        if (oldTreeActualParent.children.size == 0) return null
        var i = 0
        var state: TokenParserState = TokenParserState.BINARY_OPERATION
        var currentBottomToken: ExpressionParserNode? = null
        while (i < oldTreeActualParent.children.size) {
            val node = oldTreeActualParent.children[i].copy()
            if (oldTreeActualParent.children[i].type == ExpressionParserNode.Type.VARIABLE || oldTreeActualParent.children[i].type == ExpressionParserNode.Type.FUNCTION) {
                state = TokenParserState.NAME
                if (currentBottomToken != null) {
                    currentBottomToken.addChild(node)
                    currentBottomToken = node
                } else
                    newTreeActualParent.addChild(node)
                val res = combineUnary(oldTreeActualParent.children[i], node)
                if (res != null) return res
            } else if (oldTreeActualParent.children[i].type == ExpressionParserNode.Type.BINARY_OPERATION) {
                state = TokenParserState.BINARY_OPERATION
                currentBottomToken = null
                newTreeActualParent.addChild(node)
            } else if (oldTreeActualParent.children[i].type == ExpressionParserNode.Type.UNARY_OPERATION) {
                if (state == TokenParserState.BINARY_OPERATION) {
                    if (currentBottomToken == null) {
                        newTreeActualParent.addChild(node)
                        currentBottomToken = node
                    } else {
                        currentBottomToken.addChild(node)
                        currentBottomToken = node
                    }
                } else {
                    if (currentBottomToken == null) {
                        currentBottomToken = node
                        val lastElement = newTreeActualParent.children.last()
                        newTreeActualParent.children.removeAt(newTreeActualParent.children.lastIndex)
                        node.addChild(lastElement)
                        newTreeActualParent.addChild(node)
                    } else {
                        val parent = currentBottomToken.parent!!
                        node.addChild(currentBottomToken)
                        parent.children.removeAt(parent.children.lastIndex)
                        parent.addChild(node)
                        currentBottomToken = node
                    }
                }
            }
            i++
        }
        return null
    }

    private fun resolveBinaryOperations(actualParent: ExpressionParserNode) {
        while (true) {
            val minPriority = actualParent.getMinPriorityOfBinaryOperationsWithoutOperands()
            if (minPriority == Double.MAX_VALUE) break
            actualParent.resolveBinaryOperationsWithPriorityRecursive(minPriority, functionConfiguration)
        }
    }

    private fun toExpressionTree(oldTreeActualParent: ExpressionParserNode, newTreeActualParent: ExpressionNode) {
        var oldChildren = ArrayList<ExpressionParserNode>()
        oldTreeActualParent.getNonEmptyChildren(oldChildren)
        for (child in oldChildren) {
            if (child.type == ExpressionParserNode.Type.VARIABLE) {
                newTreeActualParent.addChild(ExpressionNode(NodeType.VARIABLE,
                        child.value,
                        child.startPosition,
                        child.endPosition,
                        child.subValue,
                        newTreeActualParent))
            } else {
                newTreeActualParent.addChild(ExpressionNode(NodeType.FUNCTION,
                        child.value,
                        child.startPosition,
                        child.endPosition,
                        child.subValue,
                        newTreeActualParent,
                        child.functionStringDefinition))
                toExpressionTree(child, newTreeActualParent.children.last())
            }
        }
    }

    private fun isComplicatedTag(expression: String = this.expression, currentPosition: Int = this.currentPosition) = (remainingExpressionStartsWith("<", expression, currentPosition) && (!(
            remainingExpressionStartsWith("<mo", expression, currentPosition) ||
                    remainingExpressionStartsWith("<mi", expression, currentPosition) ||
                    remainingExpressionStartsWith("<mn", expression, currentPosition) ||
                    remainingExpressionStartsWith("</mo", expression, currentPosition) ||
                    remainingExpressionStartsWith("</mi", expression, currentPosition) ||
                    remainingExpressionStartsWith("</mn", expression, currentPosition)
            ) ||
            remainingExpressionStartsWith("<minus", expression, currentPosition) ||
            remainingExpressionStartsWith("<mover", expression, currentPosition) ||
            remainingExpressionStartsWith("</minus", expression, currentPosition) ||
            remainingExpressionStartsWith("</mover", expression, currentPosition)))

    private fun isMathMlMultipleTag(expression: String = this.expression, currentPosition: Int = this.currentPosition) = (remainingExpressionStartsWith("munderover", expression, currentPosition) ||
            remainingExpressionStartsWith("msubsup", expression, currentPosition))

    private fun isTexMultipleTag(expression: String = this.expression, currentPosition: Int = this.currentPosition) = (remainingExpressionStartsWith("\\sum", expression, currentPosition) ||
            remainingExpressionStartsWith("\\prod", expression, currentPosition))

    companion object {
        val mathMlTags = listOf("mfenced", "mfenced_|__|", "mfrac", "mi", "minus", "mn", "mo", "mover", "mroot", "msqrt", "msub", "msubsup", "msup", "munderover")
    }
}