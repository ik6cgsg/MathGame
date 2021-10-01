package mathhelper.twf.mainpoints

import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.expressiontree.ExpressionNodeConstructor
import mathhelper.twf.expressiontree.ExpressionStructureConditionConstructor
import mathhelper.twf.expressiontree.ParserError
import mathhelper.twf.expressiontree.checkExpressionStructure
import mathhelper.twf.factstransformations.*
import mathhelper.twf.logs.MessageType
import mathhelper.twf.logs.log
import mathhelper.twf.standartlibextensions.findClosestPlaceToTargetOnTheSameLevel
import mathhelper.twf.standartlibextensions.readOpenTagStringIfItPresent
import mathhelper.twf.standartlibextensions.remainingExpressionStartsWith
import mathhelper.twf.visualization.*


data class TexVerificationResult(
        val validatedSolution: String,
        val errorMessage: String
)


fun checkFactsInTex(
        originalTexSolution: String,
        startExpressionIdentifier: String = "", //Expression, from which learner need to start the transformations
        endExpressionIdentifier: String = "",
        targetFactIdentifier: String = "", //Fact that learner need to prove should be here
        targetFactPattern: String = "", //Pattern that specify criteria that learner's answer must meet
        comparisonSign: String = "", //Comparison sign
        additionalFactsIdentifiers: String = "", ///Identifiers split by configSeparator - task condition facts should be here
        shortErrorDescription: String = "0", //crop parsed steps from error description
        skipTrivialCheck: Boolean = false, //do not check completeness of transformations, only correctness
        compiledConfiguration: CompiledConfiguration,
        otherGoalData: Map<String, Any>? // may contains key="hiddenGoalExpressions" with list of possible goal expression values in structure string
): TexVerificationResult {
    log.clear()
    log.factConstructorViewer = FactConstructorViewer(compiledConfiguration)
    log.addMessage({ "input transformations in TEX: '''$originalTexSolution'''" }, level = 0)
    val texSolution = dropPerformedTexBrushing(originalTexSolution)
    log.addMessage({ "input transformations in TEX without brushing: '''$texSolution'''" }, level = 0)
    val transformationChainParser = TransformationChainParser(texSolution,
            nameForRuleDesignationsPossible = false,
            functionConfiguration = compiledConfiguration.functionConfiguration,
            factsLogicConfiguration = compiledConfiguration.factsLogicConfiguration,
            compiledImmediateVariableReplacements = compiledConfiguration.compiledImmediateVariableReplacements,
            isMathML = false)
    log.addMessage({ "input transformations parsing started" }, MessageType.USER, level = 0)
    val error = transformationChainParser.parse()
    if (error != null) {
        return TexVerificationResult(returnParsingError(error, texSolution), "Syntax error (underlined): ${error.description}")
    } else if (transformationChainParser.root.factTransformationChains.isEmpty() && transformationChainParser.root.expressionTransformationChains.isEmpty()) {
        return TexVerificationResult(texSolution, "Error: No transformations found")
    } else {
        log.addMessage({ "input transformations are parsed successfully" }, MessageType.USER, level = 0)
        log.addMessageWithFactDetail({ "parsed input transformations: " }, transformationChainParser.root, MessageType.USER)
        val factComporator = compiledConfiguration.factComporator
        val solutionRoot = combineSolutionRoot(targetFactIdentifier, transformationChainParser, compiledConfiguration, startExpressionIdentifier, endExpressionIdentifier, comparisonSign)
        log.addMessage({ "input transformations checking started" }, MessageType.USER, level = 0)
        val checkingResult = solutionRoot.check(factComporator, false,
                listOf(),
                listOf(),
                log.factConstructorViewer.additionalFactsFromItsIdentifiers(additionalFactsIdentifiers),
                skipTrivialCheck)
        log.addMessage({
            "input transformations checking result: '" +
                    (if (checkingResult.isCorrect) "correct" else "incorrect - ${checkingResult.description}") +
                    "'"
        }, MessageType.USER, level = 0)
        val resultWithColoredTasks = brushTex(transformationChainParser.originalTransformationChain, checkingResult.coloringTasks.filter { it.startPosition > 0 || it.endPosition > 0 })
        val result = setBackgroundColorTex(resultWithColoredTasks, "purple")
        log.addMessage({ "transformations in tex after brushing: '''$result'''" }, level = 0)

        if (!checkingResult.isCorrect) {
            return if (shortErrorDescription == "1") {
                TexVerificationResult(result, "$errorPrefix: Unclear transformation or incomplete solution. Try to fix errors or to write more details")
            } else TexVerificationResult(result, "$errorPrefix: ${checkingResult.description}")
        }

        if (targetFactPattern.isNotBlank() || otherGoalData != null) {
            log.addMessage({ "input transformations checked successfully; answer checking started" }, MessageType.USER, level = 0)
            val resultExpression = solutionRoot.getLastExpression()
            if (resultExpression == null || resultExpression.data.children.isEmpty()) {
                return TexVerificationResult(result, "$errorPrefix: answer is empty")
            }

            var answerPatterFound = false

            if (otherGoalData != null && otherGoalData["hiddenGoalExpressions"] is List<*>) {
                val hiddenGoalExpressions = otherGoalData["hiddenGoalExpressions"] as List<*>
                val expressionNodeConstructor = ExpressionNodeConstructor(compiledConfiguration.functionConfiguration)
                for (hiddenGoalExpression in hiddenGoalExpressions) {
                    log.addMessage({ "compare with hidden goal expression: '''$hiddenGoalExpression'''" }, level = 0)
                    if (hiddenGoalExpression is String) {
                        val hiddenGoalExpressionNode = expressionNodeConstructor.construct(hiddenGoalExpression)
                        log.addMessage({ "compare with hidden goal expression parsed as: '''$hiddenGoalExpressionNode'''" }, level = 0)
                        if (compiledConfiguration.factComporator.expressionComporator.compareAsIs(hiddenGoalExpressionNode, resultExpression.data, withBracketUnification = true)) {
                            log.addMessage({ "answer is matched by hidden goal expression: '''$hiddenGoalExpression'''" }, level = 0)
                            answerPatterFound = true
                        }
                    }
                }
            }

            if (targetFactPattern.isNotBlank()) {
                val expressionStructureConditionConstructor = ExpressionStructureConditionConstructor(compiledConfiguration)
                val patternNode = expressionStructureConditionConstructor.parse(targetFactPattern)
                log.addMessage({ "parsed answer pattern: '''$patternNode'''" }, level = 0)

                if (checkExpressionStructure(resultExpression.data, patternNode)) {
                    log.addMessage({ "answer is matched by pattern: '''$patternNode'''" }, level = 0)
                    answerPatterFound = true
                } else {
                    log.addMessage({ "answer is not matched by pattern: '''$patternNode'''" }, level = 0)
                }
            }

            if (!answerPatterFound) {
                return TexVerificationResult(result, "$errorPrefix: answer does not match given condition")
            }
        }

        log.addMessage({ "Full solution checked successfully" }, MessageType.USER, level = 0)
        return TexVerificationResult(result, "")
    }
}

private fun returnParsingError(error: ParserError, mathML: String) = returnParsingError(error.description, error.position, error.endPosition, mathML)

private fun returnParsingError(description: String, start: Int, end: Int, tex: String): String {
    log.addMessage({ "transformations parsing error: '${description}'" }, MessageType.USER, level = 0)
    val endPosition = findClosestPlaceToTargetOnTheSameLevel(tex, start, end)
    val result = tex.substring(0, start) + "\\underline{" +
            tex.substring(start, endPosition) + "}" +
            tex.substring(endPosition, tex.length)
    return result
}

fun deleteUnsupportedTags(string: String): String {
    val result = StringBuilder()
    var pos = 0
    while (pos < string.length) {
        var toWhile = false
        for (tag in unsupportedTagListMathML) {
            if (remainingExpressionStartsWith("</$tag>", string, pos)) {
                pos += "</$tag>".length
                toWhile = true
                result.append("</mrow>")
            } else if (remainingExpressionStartsWith("<$tag", string, pos)) {
                val actualTag = readOpenTagStringIfItPresent(string, pos)
                pos += actualTag!!.length
                toWhile = true
                result.append("<mrow>")
            }
            if (toWhile) {
                break
            }
        }
        if (toWhile) {
            continue
        }
        result.append(string[pos])
        pos++
    }
    return result.toString()
}