package mathhelper.twf.factstransformations

import mathhelper.twf.config.*
import mathhelper.twf.expressiontree.ExpressionTreeParser
import mathhelper.twf.expressiontree.ParserError
import mathhelper.twf.logs.log
import mathhelper.twf.standartlibextensions.*

class TransformationChainParser(
        val originalTransformationChain: String,
        val nameForRuleDesignationsPossible: Boolean = false,
        val functionConfiguration: FunctionConfiguration = FunctionConfiguration(),
        val factsLogicConfiguration: FactsLogicConfiguration = FactsLogicConfiguration(),
        val compiledImmediateVariableReplacements: Map<String, String> = mapOf(),
        var transformationChain: String = originalTransformationChain,
        var isMathML: Boolean = true
) {
    var root: MainLineAndNode = MainLineAndNode(0, transformationChain.length, null)

    private enum class ParserState {
        EXPRESSION_START,
        IN_EXPRESSION,
        NOT_IN_EXPRESSION_END, //also used for indication where linebreak is used for long expression splitting
        IN_END_OF_FOLLOWING_STATEMENT,
        AFTER_ROW_END
    }

    var parserError: ParserError? = null

    private fun normalisePositionInParseError(parserError: ParserError): ParserError = parserError

    fun parse(): ParserError? {
        val error = splitOnMainLevelParts(root)
        if (error != null) return normalisePositionInParseError(error)
        return null
    }

    private fun logParserStateChange(parserState: ParserState, logLevel: Int, oldValue: ParserState) {
        log.addMessage({ "parserState changed to '${parserState.toString()}' from '${oldValue.toString()}'" }, level = logLevel)
    }

    /**
     * returns first argument and logs it's value; should be used in assigns
     */
    private fun assignAndLogParserState(parserState: ParserState, logLevel: Int, currentValue: ParserState): ParserState {
        if (parserState != currentValue) {
            logParserStateChange(parserState, logLevel, currentValue)
        }
        return parserState
    }

    data class SemanticRangeShift(val rangeStart: Int = 0, val rangeEnd: Int = 0, var start: Int = 0, var end: Int = 0) {
        fun addTagShift(openTagLen: Int, closeTagLen: Int) {
            start += openTagLen
            end += closeTagLen
        }

        fun currentShiftKeepsSemantic(start: Int, end: Int) = ((start <= rangeStart + this.start) && (end >= rangeEnd + this.end))
    }

    private fun splitOnMainLevelParts(
            mainLineNode: MainLineNode
    ): ParserError? {
        log.add(mainLineNode.startPosition, mainLineNode.endPosition, {"parsing part "}, {" : "}, {""}, levelChange = 1)
        val currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)

        //parser state variables:
        var parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, ParserState.EXPRESSION_START)

        //actualStructures:
        var currentMainLineNode = mainLineNode

        //parser position markers:
        var currentPosition = mainLineNode.startPosition
        var currentPartStartPosition = log.assignAndLog(mainLineNode.startPosition, currentLogLevel, { "currentPartStartPosition" })

        val semanticRangeShift = SemanticRangeShift(mainLineNode.startPosition, mainLineNode.endPosition)

        while (currentPosition < mainLineNode.endPosition) {
            if (remainingExpressionStartsWith(StringExtension.openCommentShort, transformationChain, currentPosition) || remainingExpressionStartsWith(StringExtension.openCommentMathML, transformationChain, currentPosition)) {
                val commentStartPosition = currentPosition
                currentPosition += 2
                while (currentPosition < mainLineNode.endPosition && !(remainingExpressionStartsWith(StringExtension.closeCommentShort, transformationChain, currentPosition) || remainingExpressionStartsWith(StringExtension.closeCommentMathML, transformationChain, currentPosition))){
                    currentPosition++
                }
                currentPosition += if (currentPosition < mainLineNode.endPosition && remainingExpressionStartsWith(StringExtension.closeCommentShort, transformationChain, currentPosition)) {
                    StringExtension.closeCommentShort.length
                } else if (currentPosition < mainLineNode.endPosition && remainingExpressionStartsWith(StringExtension.closeCommentMathML, transformationChain, currentPosition)) {
                    StringExtension.closeCommentMathML.length
                } else 0
                log.add(currentPosition, { "comment found and parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                semanticRangeShift.addTagShift(currentPosition - commentStartPosition, 0)
                transformationChain = transformationChain.substring(0, commentStartPosition) + " ".repeat(currentPosition - commentStartPosition) +
                        transformationChain.substring(currentPosition, transformationChain.length)
                continue
            }
            if (remainingExpressionStartsWith(StringExtension.newWhiteSpace, transformationChain, currentPosition)) {
                currentPosition += StringExtension.newWhiteSpace.length
                log.add(currentPosition, { "whitespace found and parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                semanticRangeShift.addTagShift(StringExtension.newWhiteSpace.length, 0)
                continue
            }
            if (transformationChain[currentPosition].isWhitespace()) {
                currentPosition++
                semanticRangeShift.addTagShift(1, 0)
                continue
            }
            if (remainingExpressionStartsWith("<math", transformationChain, currentPosition)) {
                log.add(currentPosition, { "math_tag found at position = '" }, { "'" }, currentLogLevel)
                val currentPositionStamp = currentPosition
                currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, transformationChain, currentPosition)
                currentPosition++
                log.add(currentPosition, { "math_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                semanticRangeShift.addTagShift(currentPosition - currentPositionStamp, "</math>".length)
                continue
            } else if (remainingExpressionStartsWith("</math", transformationChain, currentPosition)) {
                log.add(currentPosition, { "math_closing_tag found at position = '" }, { "'" }, currentLogLevel)
                if (parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT) {
                    //last MainChain (transformation chain in system) ends (with last ExpressionComparison end): program handles last ExpressionComparison and starts new MainChain
                    if (currentPartStartPosition < currentPosition) {
                        log.addMessage({ "currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                        parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                ?: if (parserError!!.description != somethingUnexpectedCode) return parserError
                    }
                    parserState = assignAndLogParserState(ParserState.IN_END_OF_FOLLOWING_STATEMENT, currentLogLevel, parserState)
                }
                currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, transformationChain, currentPosition)
                currentPosition++
                log.add(currentPosition, { "math_closing_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                continue
            } else if (remainingExpressionStartsWith("</mtable", transformationChain, currentPosition)) {
                log.add(currentPosition, { "mtable_closing_tag found at position = '" }, { "'" }, currentLogLevel)
                currentPosition = skipFromRemainingExpressionWhile({ it != '>' }, transformationChain, currentPosition)
                currentPosition++
                log.add(currentPosition, { "mtable_closing_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                continue
            }
            if (parserState == ParserState.IN_EXPRESSION ||
                    parserState == ParserState.EXPRESSION_START ||
                    parserState == ParserState.IN_END_OF_FOLLOWING_STATEMENT ||
                    parserState == ParserState.NOT_IN_EXPRESSION_END) {
                if (remainingExpressionStartsWith("<mo>", transformationChain, currentPosition)) {
                    log.add(currentPosition, { "mo_open_tag found at position = '" }, { "'" }, currentLogLevel)
                    val newCurrentPosition = currentPosition + "<mo>".length
                    if (remainingExpressionStartsWith("<mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/>", transformationChain, currentPosition) ||
                            remainingExpressionStartsWith("<mo>=</mo><mo>&gt;</mo>", transformationChain, currentPosition) ||
                            remainingExpressionStartsWith("<mo>&#x21D2;</mo>", transformationChain, currentPosition)) {
                        log.add(currentPosition, { "fact_divider following_statement found at position = '" }, { "'" }, currentLogLevel)
                        //ExpressionComparison (for example, equation) ends: program handles it
                        if (currentPartStartPosition < currentPosition) {
                            log.addMessage({ "currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                            parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                    semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                    ?: return parserError
                        }
                        currentPosition +=
                                if (remainingExpressionStartsWith("<mo>&#x21D2;</mo>", transformationChain, currentPosition)) {
                                    "<mo>&#x21D2;</mo>".length
                                } else if (remainingExpressionStartsWith("<mo>=</mo><mo>&gt;</mo>", transformationChain, currentPosition)) {
                                    "<mo>=</mo><mo>&gt;</mo>".length
                                } else {
                                    "<mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/>".length
                                }
                        log.add(currentPosition, { "fact_divider following_statement parsed; currentPosition = '" }, { "'" }, currentLogLevel)

                        //handle rule application pointers:
                        var waitingForFollowingSignDoubling = log.assignAndLog(false, currentLogLevel, { "waitingForFollowingSignDoubling" })
                        if (remainingExpressionStartsWith("<mo>[</mo>", transformationChain, currentPosition)) {
                            log.add(currentPosition, { "fact_divider_details_mo found at position = '" }, { "'" }, currentLogLevel)
                            currentPosition += "<mo>[</mo>".length
                            val internalEnd = skipFromRemainingExpressionWhileClosingBracketNotFound("<mo>]</mo>", "<mo>[</mo>", transformationChain, currentPosition)
                            parseRule(currentPosition, internalEnd, currentMainLineNode.getActualChain() as MutableList<ComparableTransformationsPart>, currentMainLineNode)
                            currentPosition = internalEnd + "<mo>]</mo>".length
                            log.add(currentPosition, { "fact_divider_details_mo parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                            waitingForFollowingSignDoubling = log.assignAndLog(true, currentLogLevel, { "waitingForFollowingSignDoubling" })
                        } else if (remainingExpressionStartsWith("<mfenced open=\"[\" close=\"]\">", transformationChain, currentPosition)) {
                            log.add(currentPosition, { "fact_divider_details_mfenced found at position = '" }, { "'" }, currentLogLevel)
                            currentPosition += "<mfenced open=\"[\" close=\"]\">".length
                            val internalEnd = skipFromRemainingExpressionWhileClosingTagNotFound("mfenced", transformationChain, currentPosition)
                            parseRule(currentPosition, internalEnd, currentMainLineNode.getActualChain() as MutableList<ComparableTransformationsPart>, currentMainLineNode)
                            currentPosition = internalEnd + "</mfenced>".length
                            log.add(currentPosition, { "fact_divider_details_mfenced parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                            waitingForFollowingSignDoubling = log.assignAndLog(true, currentLogLevel, { "waitingForFollowingSignDoubling" })
                        } else if (remainingExpressionStartsWith("<mtext>", transformationChain, currentPosition)) {
                            log.add(currentPosition, { "fact_divider_details rule_pointer found at position = '" }, { "'" }, currentLogLevel)
                            val ruleLinkNameStartPosition = currentPosition
                            currentPosition += "<mtext>".length
                            val internalEnd = skipFromRemainingExpressionWhileClosingTagNotFound("mtext", transformationChain, currentPosition)
                            val ruleNameLink = transformationChain.substring(currentPosition, internalEnd - 1)
                            currentPosition = internalEnd + "</mtext>".length
                            currentMainLineNode.getActualChain().add(RulePointer(ruleLinkNameStartPosition, currentPosition, currentMainLineNode, ruleNameLink))
                            log.add(currentMainLineNode.getActualChain().last(), { "RulePointer '''" }, { "''' parsed" }, currentLogLevel)
                            log.add(currentPosition, { "fact_divider_details rule_pointer parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                            waitingForFollowingSignDoubling = log.assignAndLog(true, currentLogLevel, { "waitingForFollowingSignDoubling" })
                        }
                        if (waitingForFollowingSignDoubling) {
                            log.add(currentPosition, { "second following_statement part parsing started; currentPosition = '" }, { "'" }, currentLogLevel)
                            val currentSplittingSign = if (remainingExpressionStartsWith("<mo>&#x21D2;</mo>", transformationChain, currentPosition)) {
                                "<mo>&#x21D2;</mo>"
                            } else if (remainingExpressionStartsWith("<mo>=</mo><mo>&gt;</mo>", transformationChain, currentPosition)) {
                                "<mo>=</mo><mo>&gt;</mo>"
                            } else if (remainingExpressionStartsWith("<mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/>", transformationChain, currentPosition)) {
                                "<mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/>"
                            } else return ParserError(currentPosition, "Following statement duplication expected", currentPosition)
                            currentPosition += currentSplittingSign.length
                            log.add(currentPosition, { "second following_statement part parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                        }
                        currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                        parserState = assignAndLogParserState(ParserState.IN_END_OF_FOLLOWING_STATEMENT, currentLogLevel, parserState)
                        continue
                    } else if (remainingExpressionStartsWith("<mo>[</mo>", transformationChain, currentPosition)) {
                        log.add(currentPosition, { "mo[ found at position = '" }, { "'" }, currentLogLevel)
                        currentPosition += "<mo>[</mo>".length
                        log.add(currentPosition, { "mo[ parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                        if (parserState == ParserState.EXPRESSION_START) {
                            log.add(currentPosition, { "rule_identified; currentPosition = '" }, { "'" }, currentLogLevel)
                            val internalEnd = skipFromRemainingExpressionWhileClosingBracketNotFound("<mo>]</mo>", "<mo>[</mo>", transformationChain, currentPosition)
                            parseRule(currentPosition, internalEnd, currentMainLineNode.rules as MutableList<ComparableTransformationsPart>, currentMainLineNode)
                            currentPosition = internalEnd + "<mo>]</mo>".length
                            log.add(currentPosition, { "rule parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                            currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                            parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                        }
                        continue
                    } else {
                        parserState = assignAndLogParserState(ParserState.IN_EXPRESSION, currentLogLevel, parserState)
                        for (sign in factsLogicConfiguration.signsPointersOnNotEndedExpression) {
                            if (remainingExpressionStartsWith(sign, transformationChain, newCurrentPosition)) {
                                if (remainingExpressionStartsWith("<mo>$sign</mo>", transformationChain, currentPosition)) {
                                    currentPosition += "<mo>$sign</mo>".length
                                    log.add(sign, currentPosition, { "signsPointersOnNotEndedExpression '" }, { "' found and parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                                    parserState = assignAndLogParserState(ParserState.NOT_IN_EXPRESSION_END, currentLogLevel, parserState)
                                    break;
                                }
                            }
                        }
                        if (parserState != ParserState.NOT_IN_EXPRESSION_END) {
                            currentPosition = newCurrentPosition
                            log.add(currentPosition, { "parserState != ParserState.NOT_IN_EXPRESSION_END; currentPosition = '" }, { "'" }, currentLogLevel)
                        }
                        continue
                    }
                } else if (remainingExpressionStartsWith("<mspace linebreak=\"newline\"/>", transformationChain, currentPosition)) {
                    log.add(currentPosition, { "mspace_tag_linebreak_newline found at position = '" }, { "'" }, currentLogLevel)
                    if (parserState != ParserState.NOT_IN_EXPRESSION_END) {
                        //ExpressionComparison (for example, equation) ends: program handles it
                        if (currentPartStartPosition < currentPosition && transformationChain.substring(currentPartStartPosition, currentPosition).isNotBlank()) {
                            log.addMessage({ "currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                            parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                    semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                    ?: return parserError
                        }
                        currentPosition += "<mspace linebreak=\"newline\"/>".length
                        currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                        if (currentMainLineNode.parent != null) {
                            //new MainChain (transformation chain in system) starts (with new ExpressionComparison start): program handles last ExpressionComparison and starts new MainChain
                            currentMainLineNode.addStartNewFactChain()
                            log.addMessage({ "currentMainLineNode.parent != null; currentMainLineNode.addStartNewFactChain() called" }, level = currentLogLevel)
                        }
                        parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                    } else {
                        currentPosition += "<mspace linebreak=\"newline\"/>".length
                    }
                    log.add(currentPosition, { "mspace_tag_linebreak_newline parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                    continue
                } else if (remainingExpressionStartsWith("</mtd></mtr>", transformationChain, currentPosition)) {
                    log.add(currentPosition, { "mtd_mtr_closing_tag found at position = '" }, { "'" }, currentLogLevel)
                    if (parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT && currentPartStartPosition < currentPosition) {
                        log.addMessage({ "parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT && currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                        //last MainChain (transformation chain in system) ends (with last ExpressionComparison end): program handles last ExpressionComparison and starts new MainChain
                        parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                ?: return parserError
                    }
                    currentPosition += "</mtd></mtr>".length
                    log.add(currentPosition, { "mtd_mtr_closing_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                    currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                    parserState = assignAndLogParserState(ParserState.AFTER_ROW_END, currentLogLevel, parserState)
                    continue
                } else if (remainingExpressionStartsWith("</mfenced>", transformationChain, currentPosition)) {
                    log.add(currentPosition, { "mfenced_closing_tag found at position = '" }, { "'" }, currentLogLevel)
                    if (currentPosition == currentMainLineNode.endPosition) {
                        currentPosition += "</mfenced>".length
                        if (currentMainLineNode.isEmpty()){
                            if (parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT && currentPartStartPosition < currentPosition) {
                                log.addMessage({ "parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT && currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                                //last MainChain (transformation chain in system) ends (with last ExpressionComparison end): program handles last ExpressionComparison and starts new MainChain
                                parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                        semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                        ?: return parserError
                            }
                        }
                        log.add(if (currentMainLineNode.parent == null) "null" else "not null",
                                { "currentPosition == currentMainLineNode.endPosition; currentMainLineNode.parent is '" }, { "'" }, currentLogLevel)
                        currentMainLineNode = currentMainLineNode.parent ?: return ParserError(currentPosition, "Unexpected system closing")
                        currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                        parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                    } else {
                        currentPosition += "</mfenced>".length
                    }
                    log.add(currentPosition, { "mfenced_closing_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                    continue
                } else {
                    val actualTag = readOpenTagStringIfItPresent(transformationChain, currentPosition)
                    if (actualTag != null && parserState != ParserState.IN_EXPRESSION) {
                        log.add(actualTag, currentPosition, { "actual_tag '" }, { "' found; currentPosition = '" }, { "'" }, currentLogLevel)
                        val tagData = getTagAttributes(actualTag)
                        log.add(tagData.entries.joinToString { "${it.key}='${it.value}'" }, { "extracted tag_data: '" }, { "'" }, currentLogLevel)
                        if (tagData["name"] == "mfenced") {
                            if (tagData["close"] == "\"\"") {
                                log.addMessage({ "tagData[name] == mfenced && tagData[close] == ''" }, level = currentLogLevel)
                                if (parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT && parserState != ParserState.EXPRESSION_START && currentPartStartPosition < currentPosition) {
                                    log.addMessage({ "currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                                    //last MainChain (transformation chain in system) ends (with last ExpressionComparison end): program handles last ExpressionComparison and starts new MainChain
                                    parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                            semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                            ?: return parserError
                                }
                                currentPosition += actualTag.length
                                log.add(currentPosition, { "actual_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                                val internalEnd = skipFromRemainingExpressionWhileClosingTagNotFound("mfenced", transformationChain, currentPosition)
                                log.add(internalEnd, { "internalEnd (closing_mfenced_tag) = '" }, { "'" }, currentLogLevel)
                                val childMainLineNode = if (tagData["open"] == "\"{\"") {
                                    log.add(currentPosition, internalEnd, { "MainLineAndNode AND child_created 'from " }, { " to " }, { "'" }, currentLogLevel)
                                    MainLineAndNode(currentPosition, internalEnd, currentMainLineNode)
                                } else if (tagData["open"] == "\"[\"") {
                                    log.add(currentPosition, internalEnd, { "MainLineOrNode OR child_created 'from " }, { " to " }, { "'" }, currentLogLevel)
                                    MainLineOrNode(currentPosition, internalEnd, currentMainLineNode)
                                } else return ParserError(currentPosition, "Undefined system")
                                currentMainLineNode.addExpressionComparisonFact(childMainLineNode)
                                log.addMessage({ "currentMainLineNode.addExpressionComparisonFact() called; switch_to_child_node" }, level = currentLogLevel)
                                currentMainLineNode = childMainLineNode
                                parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                            } else if (tagData["name"] == "mfenced" && tagData["close"] == "\"]\"" && tagData["open"] == "\"[\"") {
                                log.addMessage({ "tagData[name] == mfenced && tagData[close] == '' && tagData[open] == ''" }, level = currentLogLevel)
                                //rule start
                                //todo (maybe condition 'parserState == ParserState.EXPRESSION_START' is wrong)
                                if (parserState == ParserState.EXPRESSION_START && currentPartStartPosition < currentPosition) {
                                    log.addMessage({ "currentPartStartPosition < currentPosition; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, level = currentLogLevel)
                                    //last MainChain (transformation chain in system) ends (with last ExpressionComparison end): program handles last ExpressionComparison and starts new MainChain
                                    parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                                            semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                                            ?: return parserError
                                }
                                currentPosition += actualTag.length
                                log.add(currentPosition, { "actual_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                                if (parserState == ParserState.EXPRESSION_START && currentPartStartPosition < currentPosition) {
                                    log.addMessage({ "parserState == ParserState.EXPRESSION_START && currentPartStartPosition < currentPosition" }, level = currentLogLevel)
                                    val internalEnd = skipFromRemainingExpressionWhileClosingTagNotFound("mfenced", transformationChain, currentPosition)
                                    log.add(internalEnd, { "internalEnd (closing_mfenced_tag) = '" }, { "'" }, currentLogLevel)
                                    parseRule(currentPosition, internalEnd, currentMainLineNode.rules as MutableList<ComparableTransformationsPart>, currentMainLineNode)
                                    currentPosition = internalEnd + "</mfenced>".length
                                    log.add(currentPosition, { "rule parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                                    currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                                    parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                                }
                            } else {
                                currentPosition += actualTag.length
                            }
                        } else if (tagData["name"] == "mtable" || tagData["name"] == "mtr" || tagData["name"] == "mtd") {
                            currentPosition += actualTag.length
                            log.add(currentPosition, { "actual_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                            if (parserState == ParserState.EXPRESSION_START) {
                                currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                            }
                        } else {
                            currentPosition += actualTag.length
                            log.add(currentPosition, { "actual_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                        }
                        continue
                    } else {
                        currentPosition++
                        parserState = assignAndLogParserState(ParserState.IN_EXPRESSION, currentLogLevel, parserState)
                        continue
                    }
                }
            } else if (parserState == ParserState.AFTER_ROW_END) {
                log.addMessage({ "parserState == ParserState.AFTER_ROW_END" }, level = currentLogLevel)
                if (remainingExpressionStartsWith("<mtr><mtd>", transformationChain, currentPosition)) {
                    log.add(currentPosition, { "mtd_mtr_tag found at position = '" }, { "'" }, currentLogLevel)
                    currentPosition += "<mtr><mtd>".length
                    currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                    semanticRangeShift.addTagShift("<mtr><mtd>".length, "</mtr></mtd>".length)
                    if (currentMainLineNode.parent != null) {
                        //new MainChain (transformation chain in system) starts (with new ExpressionComparison start): program handles last ExpressionComparison and starts new MainChain
                        currentMainLineNode.addStartNewFactChain()
                        log.addMessage({ "currentMainLineNode.parent != null; currentMainLineNode.addStartNewFactChain() called" }, level = currentLogLevel)
                    }
                    parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                    log.add(currentPosition, { "mtd_mtr_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                    continue
                } else if (remainingExpressionStartsWith("</mfenced>", transformationChain, currentPosition)) {
                    log.add(currentPosition, { "mfenced_closing_tag found at position = '" }, { "'" }, currentLogLevel)
                    if (currentPosition == currentMainLineNode.endPosition) {
                        log.add(if (currentMainLineNode.parent == null) "null" else "not null",
                                { "currentPosition == currentMainLineNode.endPosition; currentMainLineNode.parent is '" }, { "'" }, currentLogLevel)
                        currentMainLineNode = currentMainLineNode.parent ?: return ParserError(currentPosition, "Unexpected system closing")
                        currentPosition += "</mfenced>".length
                        currentPartStartPosition = log.assignAndLog(currentPosition, currentLogLevel, { "currentPartStartPosition" })
                        parserState = assignAndLogParserState(ParserState.EXPRESSION_START, currentLogLevel, parserState)
                    } else {
                        currentPosition += "</mfenced>".length
                    }
                    log.add(currentPosition, { "mfenced_closing_tag parsed; currentPosition = '" }, { "'" }, currentLogLevel)
                    continue
                }
            }

            return ParserError(currentPosition, "Unexpected action")
        }
        log.addMessage({ "exit from 'while (currentPosition < mainLineNode.endPosition)'" }, level = currentLogLevel)
        if (parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT) {
            log.addMessage({ "parserState != ParserState.IN_END_OF_FOLLOWING_STATEMENT" }, level = currentLogLevel)
            //last MainChain (transformation chain in system) ends (with last ExpressionComparison end): program handles last ExpressionComparison and starts new MainChain
            if (currentPartStartPosition < currentPosition) {
                log.add(currentPartStartPosition, currentPosition, { "currentPartStartPosition:'" }, { "' < currentPosition:'" }, { "'; parseExpressionComparisonOrExpressionChainFromTransformationChain" }, currentLogLevel)
                parseExpressionComparisonOrExpressionChainFromTransformationChain(currentPartStartPosition, currentPosition, currentMainLineNode,
                        semanticRangeShift.currentShiftKeepsSemantic(currentPartStartPosition, currentPosition))
                        ?: if (parserError!!.description != somethingUnexpectedCode) return parserError
            }
        }
        log.addMessageWithFactDetail({ "parsed fact: " }, mainLineNode, level = currentLogLevel)
        return null
    }

    private fun parseRule(startPosition: Int,
                          endPosition: Int,
                          ruleChain: MutableList<ComparableTransformationsPart>,
                          parent: MainLineNode?): MutableList<ComparableTransformationsPart>? {
        log.add(transformationChain.substring(startPosition, endPosition), {"parseRule '''"}, {"'''"}, levelChange = 1)
        val currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)

        var currentPosition = startPosition
        var currentEndPosition = endPosition
        if (remainingExpressionStartsWith("<mrow>", transformationChain, currentPosition)) {
            currentPosition += "<mrow>".length
            currentEndPosition -= "</mrow>".length
            log.add(currentPosition, currentEndPosition, { "mrow cropped; currentPosition = '" }, { "', currentEndPosition = '" }, { "'" }, currentLogLevel)
        }
        val ruleName = if (remainingExpressionStartsWith("<mtext>", transformationChain, currentPosition)) {
            val nameStartPosition = currentPosition
            currentPosition += "<mtext>".length
            currentPosition = skipFromRemainingExpressionWhileClosingTagNotFound("mtext", transformationChain, currentPosition)
            if (transformationChain.elementAt(currentPosition - 1) == ':') {
                val name = transformationChain.substring(nameStartPosition + "<mtext>".length, currentPosition - 1)
                currentPosition += "</mtext>".length
                name
            } else ""
        } else ""
        log.add(ruleName, currentPosition, { "ruleName = '" }, { "' parsed; currentPosition = '" }, { "'" }, currentLogLevel)
        if (currentPosition < currentEndPosition) {
            log.addMessage({ "currentPosition < currentEndPosition; parse rule fact" }, level = currentLogLevel)
            var ruleRoot: MainLineAndNode = MainLineAndNode(currentPosition, currentEndPosition, parent)
            val error = splitOnMainLevelParts(ruleRoot)
            if (error != null) {
                parserError = error
                log.add(parserError.toString(), { "FACT_PARSER_ERROR: " }, { "" }, currentLogLevel)
                return null
            }
            ruleChain.add(Rule(startPosition, currentEndPosition, parent, ruleRoot, ruleName))
            log.add(ruleChain.last().toString(), { "Rule: '" }, {"' added"}, currentLogLevel)
        } else {
            if (ruleName.isNotBlank()) {
                ruleChain.add(RulePointer(startPosition, currentEndPosition, parent, ruleName))
                log.add(ruleChain.last().toString(), { "RulePointer: '" }, {"' added"}, currentLogLevel)
            } else {
                parserError = ParserError(startPosition, "Unexpected rule end", endPosition)
                log.add(parserError.toString(), { "BLANK_RULE_POINTER_NAME_ERROR: " }, { "" }, currentLogLevel)
                return null
            }
        }
        log.add(ruleChain.joinToString { it.toString() }, { "current ruleChain: '''" }, {"'''"}, currentLogLevel)
        return ruleChain
    }

    private fun parseExpressionComparisonOrExpressionChainFromTransformationChain(startPosition: Int,
                                                                                  endPosition: Int,
                                                                                  currentMainLineNode: MainLineNode,
                                                                                  expressionChainExpected: Boolean = false)
            : ComparableTransformationsPart? {
        val significantPart = transformationChain.substring(startPosition, endPosition)
        log.add(significantPart,
                { "parseExpressionComparisonOrExpressionChain: '''" }, {"'''"}, levelChange = 1)
        val currentLogLevel = log.currentLevel
        log.add(expressionChainExpected, { "expressionChainExpected: " }, {""}, currentLogLevel)
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)

        log.addMessage({ "Splitting_on_parts_started" }, level = currentLogLevel)
        val allParts = splitBySubstringOnTopLevel(getAllComparisonTypeSignStrings(isMathML), transformationChain, startPosition, endPosition)
        val parts = allParts.mapNotNull {
            log.add(it.startPosition, it.endPosition, it.splittingSubstring.toString(), { "part positions: from '" }, {"' to '"}, { "'; split by '" }, {"'"}, currentLogLevel)
            val partString = transformationChain
                    .substring(it.startPosition, it.endPosition)
                    .trimmedMathML()
            log.add(partString, { "part string: '''" }, { "'''" }, currentLogLevel)
            if (partString.isEmpty()) null
            else it
        }
        log.addMessage({ "Splitting_on_parts_ended" }, level = currentLogLevel)
        if (parts.size == 2 && !expressionChainExpected) {
            log.addMessage({ "ExpressionComparison parsing start" }, level = currentLogLevel)
            val leftPartParser = ExpressionTreeParser(transformationChain.substring(parts[0].startPosition, parts[0].endPosition),
                    functionConfiguration = functionConfiguration,
                    compiledImmediateVariableReplacements = compiledImmediateVariableReplacements)
            log.add(transformationChain.substring(parts[0].startPosition, parts[0].endPosition),
                    { "left expression = '''" }, { "''' parsing started" }, currentLogLevel)
            var error = leftPartParser.parse()
            if (error != null) {
                parserError = ParserError(error.position + parts[0].startPosition, error.description, parts[0].endPosition)
                log.add(parserError.toString(), { "EXPRESSION_PARSER_ERROR: " }, { "" }, currentLogLevel)
                return null
            }
            log.add(leftPartParser.root, { "left expression = '''" }, { "''' parsed" }, currentLogLevel)
            val rightPartParser = ExpressionTreeParser(transformationChain.substring(parts[1].startPosition, parts[1].endPosition),
                    functionConfiguration = functionConfiguration,
                    compiledImmediateVariableReplacements = compiledImmediateVariableReplacements)
            log.add(transformationChain.substring(parts[1].startPosition, parts[1].endPosition),
                    { "right expression = '''" }, { "''' parsing started" }, currentLogLevel)
            error = rightPartParser.parse()
            if (error != null) {
                parserError = ParserError(error.position + parts[1].startPosition, error.description, parts[1].endPosition)
                log.add(parserError.toString(), { "EXPRESSION_PARSER_ERROR: " }, { "" }, currentLogLevel)
                return null
            }
            log.add(rightPartParser.root, { "right expression = '''" }, { "''' parsed" }, currentLogLevel)
            val sign = valueFromSignString(parts[0].splittingSubstring!!)
            log.add(sign, { "sign = '" }, { "' parsed" }, currentLogLevel)
            if (factsLogicConfiguration.alwaysLetTwoPartsComparisonsAsExpressionComparisons) {
                val result = ExpressionComparison(startPosition, endPosition,
                        leftExpression = Expression(parts[0].startPosition, parts[0].endPosition, leftPartParser.root),
                        rightExpression = Expression(parts[1].startPosition, parts[1].endPosition, rightPartParser.root),
                        comparisonType = sign)
                log.addMessageWithFactDetail({ "parsed fact:" }, result, level = currentLogLevel)
                currentMainLineNode.addExpressionComparisonFact(result)
                log.addMessage({ "currentMainLineNode.addExpressionComparisonFact() called" }, level = currentLogLevel)
                return result
            } else {
                log.addMessage({ "unreachable place 2019_01_27_17_33" }, level = currentLogLevel)
                //todo: add check by testing; if expressions are equal - its rule transformation, else - ExpressionComparison
                if (true) {
                } else {
                    val result = ExpressionComparison(startPosition, endPosition,
                            leftExpression = Expression(parts[0].startPosition, parts[0].endPosition, leftPartParser.root),
                            rightExpression = Expression(parts[1].startPosition, parts[1].endPosition, rightPartParser.root),
                            comparisonType = sign)
                    currentMainLineNode.addExpressionComparisonFact(result)
                    log.addMessage({ "currentMainLineNode.addExpressionComparisonFact() called" }, level = currentLogLevel)
                    return result
                }
            }
        } else if (parts.size >= 2) {
            log.addMessage({ "ExpressionChain parsing start" }, level = currentLogLevel)
            val expressions = mutableListOf<ComparableTransformationsPart>()
            var hasEquality = false
            var hasLeftMore = false
            var hasLeftLess = false
            for (part in parts) {
                val partString = transformationChain.substring(part.startPosition, part.endPosition)
                log.add(partString, { "part string: '''" }, { "'''" }, currentLogLevel)
                if (partString.startsWith("<mtext>") && partString.endsWith("</mtext>")) {
                    val ruleNameLink = partString.substring("<mtext>".length, partString.length - ":</mtext>".length)
                    log.add(ruleNameLink, { "rule link: '" }, { "' found and parsed" }, currentLogLevel)
                    expressions.add(RulePointer(part.startPosition, part.endPosition, null, ruleNameLink))
                } else if (partString.startsWith("<mo>[</mo>") && partString.endsWith("<mo>]</mo>")) {
                    log.addMessage({ "fact_divider_details_mo found; parseRule" }, level = currentLogLevel)
                    parseRule(part.startPosition + "<mo>[</mo>".length, part.endPosition - "<mo>]</mo>".length, expressions, null)
                } else if (partString.startsWith("<mfenced open=\"[\" close=\"]\">") && partString.endsWith("</mfenced>")) {
                    log.addMessage({ "fact_divider_details_mfenced found; parseRule" }, level = currentLogLevel)
                    parseRule(part.startPosition + "<mfenced open=\"[\" close=\"]\">".length, part.endPosition - "</mfenced>".length, expressions, null)
                } else {
                    val partParser = ExpressionTreeParser(partString,
                            functionConfiguration = functionConfiguration,
                            compiledImmediateVariableReplacements = compiledImmediateVariableReplacements)
                    val error = partParser.parse()
                    if (error != null) {
                        parserError = ParserError(error.position + part.startPosition, error.description, part.endPosition)
                        log.add(parserError.toString(), { "EXPRESSION_PARSER_ERROR: " }, { "" }, currentLogLevel)
                        return null
                    }
                    log.add(partParser.root, { "parsed part expression: '''" }, { "'''" }, currentLogLevel)
                    log.add(part.splittingSubstring.toString(), { "part.splittingSign: '" }, { "'" }, currentLogLevel)
                    if (part.splittingSubstring != null) {
                        val sign = valueFromSignString(part.splittingSubstring!!)
                        when (sign) {
                            ComparisonType.EQUAL -> hasEquality = log.assignAndLog(true, currentLogLevel, { "hasEquality" })
                            ComparisonType.LEFT_MORE_OR_EQUAL -> {
                                hasEquality = log.assignAndLog(true, currentLogLevel, { "hasEquality" });
                                hasLeftMore = log.assignAndLog(true, currentLogLevel, { "hasLeftMore" })
                            }
                            ComparisonType.LEFT_LESS_OR_EQUAL -> {
                                hasEquality = log.assignAndLog(true, currentLogLevel, { "hasEquality" });
                                hasLeftLess = log.assignAndLog(true, currentLogLevel, { "hasLeftLess" })
                            }
                            ComparisonType.LEFT_MORE -> hasLeftMore = log.assignAndLog(true, currentLogLevel, { "hasLeftMore" })
                            ComparisonType.LEFT_LESS -> hasLeftLess = log.assignAndLog(true, currentLogLevel, { "hasLeftLess" })
                        }
                    }
                    if (hasLeftMore && hasLeftLess) {
                        parserError = ParserError(startPosition, "Expression transformation chain cannot contains both signs '<' and '>'", endPosition)
                        log.add(parserError.toString(), { "CHAIN_SIGN_ERROR: " }, { "" }, currentLogLevel)
                        return null
                    }
                    expressions.add(Expression(part.startPosition, part.endPosition, partParser.root))
                }
            }
            val sign = if (hasEquality) {
                if (hasLeftMore) ComparisonType.LEFT_MORE_OR_EQUAL
                else if (hasLeftLess) ComparisonType.LEFT_LESS_OR_EQUAL
                else ComparisonType.EQUAL
            } else if (hasLeftMore) ComparisonType.LEFT_MORE
            else ComparisonType.LEFT_LESS
            val result = ExpressionChain(startPosition, endPosition, sign, expressions)
            currentMainLineNode.expressionTransformationChains.add(result)
            log.add(result.toString(), { "result chain: '''" }, { "'''" }, currentLogLevel)
            return result
        }
        val actualTag = readOpenTagStringIfItPresent(transformationChain, startPosition)
        if (actualTag != null) {
            val tagData = getTagAttributes(actualTag)
            if (significantPart.endsWith("</${tagData["name"]}>") && (tagData["name"] == "mfenced" || tagData["name"] == "mrow")) {
                //go inside tag
                return parseExpressionComparisonOrExpressionChainFromTransformationChain(
                        startPosition + actualTag.length,
                        endPosition - "</${tagData["name"]}>".length,
                        currentMainLineNode,
                        expressionChainExpected)
            }
        }
        parserError = ParserError(startPosition, somethingUnexpectedCode, endPosition)
        log.add(parserError.toString(), { "LESS_THEN_2_PARTS_ERROR: " }, { "" }, currentLogLevel)
        return null
    }

    private val somethingUnexpectedCode = "Something unexpected"
}