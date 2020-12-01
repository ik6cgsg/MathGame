package factstransformations

import baseoperations.BaseOperationsDefinitions
import config.*
import config.CheckingKeyWords.Companion.factChainVerified
import config.CheckingKeyWords.Companion.inFact
import config.CheckingKeyWords.Companion.ruleAddedInContext
import config.CheckingKeyWords.Companion.verificationFailed
import expressiontree.*
import logs.MessageType
import logs.log
import standartlibextensions.splitBySubstringOnTopLevel
import standartlibextensions.splitStringByBracketsOnTopLevel
import visualization.ColoringTask


class RulePointer(
        override val startPosition: Int,
        override val endPosition: Int,
        override var parent: MainLineNode? = null,
        val nameLink: String = "",
        override var identifier: String = ""
) : MainChainPart {

    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessage({ "ERROR: This RulePointer method must not been called. Undefined behaviour. " })
        return ComparisonResult(true, mutableListOf(), this, this)
    }

    override fun type() = ComparableTransformationPartType.RULE_POINTER
    override fun toString() = "[$nameLink:]"
    override fun copyNode() = RulePointer(startPosition, endPosition, parent, nameLink)
    override fun clone() = RulePointer(startPosition, endPosition, parent, nameLink)
    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) = RulePointer(startPosition, endPosition, parent, nameLink)

    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        identifier = "[$nameLink:]"
        return identifier
    }

    override fun variableReplacement(replacements: Map<String, String>) {}
    override fun computeInIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean) {}
    override fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {}
    override fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions) {}
    override fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>) {}
    override fun isSolutionForVariables(targetVariables: MutableMap<String, Boolean>, left: Boolean, allowedVariables: Set<String>): GeneralError? = null
    override fun isFactorizationForVariables(minNumberOfMultipliers: Int, targetVariables: Set<String>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? = null
    override fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? = null
    override fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? = null
    override fun getLastExpression() = null
}

class Rule(
        override val startPosition: Int,
        override val endPosition: Int,
        override var parent: MainLineNode? = null,
        val root: MainLineAndNode,
        val name: String = "",
        override var identifier: String = "",
        var factSubstitution: FactSubstitution? = null,
        var expressionSubstitution: ExpressionSubstitution? = null
) : MainChainPart {
    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessageWithFactDetail({ "Start checking rule ${if (name.isNotBlank()) " '$name'" else ""}: " }, this.root, MessageType.USER, levelChange = 1)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        log.logCheckParams(onExpressionLevel = onExpressionLevel, factsTransformations = factsTransformations,
                expressionTransformations = expressionTransformations, additionalFacts = additionalFacts)
        val comparisonResult = root.check(factComporator, false,
                factsTransformations, expressionTransformations, additionalFacts)
        if (comparisonResult.isCorrect) {
            log.addMessage({ "${CheckingKeyWords.rule}${if (name.isNotBlank()) " '$name'" else ""} is correct" }, level = currentLogLevel)
            if (root.expressionTransformationChains.isNotEmpty()) {
                val left = root.expressionTransformationChains.first().chain.first() as Expression
                val right = root.expressionTransformationChains.first().chain.last() as Expression
                left.data.applyAllImmediateSubstitutions(factComporator.compiledConfiguration)
                right.data.applyAllImmediateSubstitutions(factComporator.compiledConfiguration)
                if (!factComporator.expressionComporator.compareAsIs(left.data, right.data)) {
                    expressionSubstitution = ExpressionSubstitution(
                            left.data,
                            right.data,
                            basedOnTaskContext = comparisonResult.additionalFactUsed,
                            name = name,
                            comparisonType = root.expressionTransformationChains.first().comparisonType)
                    log.addMessageWithExpressionSubstitutionShort({ "Expression substitution deduced from rule: " }, expressionSubstitution!!, MessageType.USER, level = currentLogLevel)
                }
            } else if (root.factTransformationChains.isNotEmpty()) {
                val left = root.factTransformationChains.first().chain.first()
                val right = root.factTransformationChains.first().chain.last()
                if (!factComporator.compareAsIs(left, right)) {
                    factSubstitution = FactSubstitution(left, right,
                            basedOnTaskContext = comparisonResult.additionalFactUsed,
                            name = name,
                            factComporator = factComporator)//todo: add direction (and ability for user to declare it)
                    log.addMessageWithFactSubstitutionDetail({ "Fact substitution deduced from rule: " }, factSubstitution!!, MessageType.USER, level = currentLogLevel)
                }
            }
        } else {
            log.addMessage({ "${CheckingKeyWords.rule}${if (name.isNotBlank()) " '$name'" else ""} $verificationFailed" }, level = currentLogLevel)
        }
        return comparisonResult
    }

    override fun type() = ComparableTransformationPartType.RULE

    override fun toString() = "[$name:$root]"
    override fun copyNode() = Rule(startPosition, endPosition, parent, root, name)
    override fun clone() = Rule(startPosition, endPosition, parent, root.clone(), name)
    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) = Rule(startPosition, endPosition, parent, root.cloneWithNormalization(nameArgsMap, sorted), name)
    override fun variableReplacement(replacements: Map<String, String>) {
        root.variableReplacement(replacements)
    }


    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = "[$name:]"
        }
        return identifier
    }

    override fun computeInIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean) {}
    override fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {}
    override fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions) {}
    override fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>) {}
    override fun isSolutionForVariables(targetVariables: MutableMap<String, Boolean>, left: Boolean, allowedVariables: Set<String>): GeneralError? = null
    override fun isFactorizationForVariables(minNumberOfMultipliers: Int, targetVariables: Set<String>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? = null
    override fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? = null
    override fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? = null
    override fun getLastExpression() = null
}

class MainChain(
        val chain: MutableList<MainChainPart> = mutableListOf()
) {
    var identifier = ""
    override fun toString() = chain.joinToString(separator = ";") { "(${it.toString()})" }
    fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank()) {
            identifier = chain.joinToString(separator = ";") { "(${it.computeIdentifier(recomputeIfComputed)})" }
        }
        return identifier
    }

    fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
              factsTransformations: List<FactSubstitution>,
              expressionTransformations: List<ExpressionSubstitution>,
              additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessage({ "Start checking fact chain" }, MessageType.USER, levelChange = 1)
        log.add(chain.toString(), { "fact chain: " }, { "" })
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        log.logCheckParams(onExpressionLevel = onExpressionLevel, factsTransformations = factsTransformations,
                expressionTransformations = expressionTransformations, additionalFacts = additionalFacts)
        val coloringTasks = mutableListOf<ColoringTask>()
        var currentLeftIndex = log.assignAndLog(0, currentLogLevel, { "currentLeftIndex" })
        var currentRightIndex = log.assignAndLog(1, currentLogLevel, { "currentRightIndex" })
        var additionalFactUsed = log.assignAndLog(false, currentLogLevel, { "additionalFactUsed" })
        while (currentRightIndex < chain.size) {
            var actualFactsTransformations: List<FactSubstitution>? = null
            var actualExpressionTransformations: List<ExpressionSubstitution>? = null
            if (chain[currentRightIndex].type() == ComparableTransformationPartType.RULE ||
                    chain[currentRightIndex].type() == ComparableTransformationPartType.RULE_POINTER) {
                if (chain[currentRightIndex].type() == ComparableTransformationPartType.RULE) {
                    val rule = chain[currentRightIndex] as Rule
                    log.addMessageWithFactDetail({ "Check ${CheckingKeyWords.rule}" }, rule.root, MessageType.USER, level = currentLogLevel)
                    val checkingResult = rule.check(factComporator, false, factsTransformations, expressionTransformations, additionalFacts)
                    if (checkingResult.isCorrect) {
                        if (rule.factSubstitution != null) {
                            actualFactsTransformations = listOf(rule.factSubstitution!!)
                            log.addMessageWithFactSubstitutionDetail({ "Fact transformation recognized:" }, rule.factSubstitution!!, MessageType.USER,
                                    level = currentLogLevel)
                            //todo: may be we need to add also some expression substitutions here, like in fact checking
                        } else if (rule.expressionSubstitution != null) {
                            actualExpressionTransformations = listOf(rule.expressionSubstitution!!)
                            log.addMessageWithExpressionSubstitutionShort({ "Expression transformation recognized:" }, rule.expressionSubstitution!!, MessageType.USER,
                                    level = currentLogLevel)
                        }
                    } else {
                        log.addMessage({ "${CheckingKeyWords.rule} $verificationFailed" }, MessageType.USER, level = currentLogLevel)
                        ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex + 1],
                                "${CheckingKeyWords.rule} $verificationFailed")
                    }
                } else {
                    val ruleName = (chain[currentRightIndex] as RulePointer).nameLink
                    log.add(ruleName, { "Handling ${CheckingKeyWords.ruleReference} '" }, { "'" }, messageType = MessageType.USER, level = currentLogLevel)
                    actualFactsTransformations = factsTransformations.filter { it.name == ruleName }
                    actualExpressionTransformations = expressionTransformations.filter { it.name == ruleName }
                    if (actualFactsTransformations.isEmpty() && actualExpressionTransformations.isEmpty()) {
                        log.add(ruleName, { "ERROR: ${CheckingKeyWords.ruleReference} '" }, { "' not found" }, messageType = MessageType.USER, level = currentLogLevel)
                        log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex + 1].startPosition,
                                factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongFactColor,
                                { "Coloring task on positions: '" }, { "' - '" }, { "', wrongFactColor = '" }, { "" }, level = currentLogLevel)
                        coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex + 1].startPosition,
                                factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongFactColor))
                        return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex + 1],
                                "Rule with name '$ruleName' not found. Exists only rules with names: ${expressionTransformations.map { it.name }.filter { it.isNotBlank() }.joinToString { "'$it'" }}")
                    }
                }
                currentRightIndex = log.assignAndLog(currentRightIndex + 1, currentLogLevel, { "currentRightIndex" })
            }

            actualExpressionTransformations = if (actualExpressionTransformations != null) actualExpressionTransformations
            else expressionTransformations// + factComporator.compiledConfiguration.compiledExpressionTreeTransformationRules
            actualFactsTransformations = if (actualFactsTransformations != null) actualFactsTransformations
            else factsTransformations// + factComporator.compiledConfiguration.compiledFactTreeTransformationRules

            if (chain[currentRightIndex].type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE ||
                    chain[currentRightIndex].type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
                for (transformationChain in (chain[currentRightIndex] as MainLineNode).factTransformationChains) {
                    (chain[currentRightIndex] as MainLineNode).inFacts.add(transformationChain.chain.first())
                }
            }

            log.addMessage({ "Check transformation from left fact to right fact:" }, MessageType.USER, level = currentLogLevel)
            currentLogLevel++
            log.addMessageWithFactDetail({ "Left fact: " }, chain[currentLeftIndex], MessageType.USER, level = currentLogLevel)
            log.addMessageWithFactDetail({ "Right fact: " }, chain[currentRightIndex], MessageType.USER, level = currentLogLevel)

            var transformationVerified = log.assignAndLog(false, currentLogLevel, { "transformationVerified" })
            if (chain[currentLeftIndex].type() == chain[currentRightIndex].type()) {
                log.addMessage({ "Left and right facts types are the same" }, MessageType.USER, level = currentLogLevel)
                if (chain[currentLeftIndex].type() == ComparableTransformationPartType.EXPRESSION_COMPARISON) {
                    val leftExpressionComparison = chain[currentLeftIndex] as ExpressionComparison
                    val rightExpressionComparison = chain[currentRightIndex] as ExpressionComparison
                    if (leftExpressionComparison.comparisonType == rightExpressionComparison.comparisonType) {
                        log.addMessage({ "Comparison types are the same" }, MessageType.USER, level = currentLogLevel)
                        val leftSubtraction = subtractionTree(leftExpressionComparison.leftExpression.data, leftExpressionComparison.rightExpression.data)
                        val rightSubtraction = subtractionTree(rightExpressionComparison.leftExpression.data, rightExpressionComparison.rightExpression.data)
                        val subtractionResult = factComporator.expressionComporator.compareWithoutSubstitutions(leftSubtraction, rightSubtraction, ComparisonType.EQUAL)
                        if (subtractionResult) {
                            transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                            log.addMessage({ "Transformation verified: 'left.left - left.right = right.left - right.right'" }, MessageType.USER, level = currentLogLevel)
                        }
                        if (!transformationVerified) {
                            when (leftExpressionComparison.comparisonType) {
                                ComparisonType.EQUAL -> {
                                    val ll = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.leftExpression.data,
                                            rightExpressionComparison.leftExpression.data, actualExpressionTransformations,
                                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                    log.add(ll, { "Left expressions are equal: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                    val rr = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.rightExpression.data,
                                            rightExpressionComparison.rightExpression.data, actualExpressionTransformations,
                                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                    log.add(rr, { "Right expressions are equal: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                    if (ll && rr) {
                                        transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                                        log.addMessage({ "Transformation verified: 'left.left = right.left' and 'left.right = right.right'" }, MessageType.USER, level = currentLogLevel)
                                    } else {
                                        log.addMessage({ "Transformation not verified" })
                                        val lr = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.leftExpression.data,
                                                rightExpressionComparison.rightExpression.data, actualExpressionTransformations,
                                                maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                        log.add(lr, { "left.left are equal to right.right: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                        val rl = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.rightExpression.data,
                                                rightExpressionComparison.leftExpression.data, actualExpressionTransformations,
                                                maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                        log.add(lr, { "left.right are equal to right.left: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                        if (lr && rl) {
                                            transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                                            log.addMessage({ "Transformation verified: 'left.left = right.right' and 'left.right = right.left'" }, MessageType.USER, level = currentLogLevel)
                                        } else {
                                            log.addMessage({ "Transformation not verified" }, MessageType.USER, level = currentLogLevel)
                                        }
                                    }
                                    if (!transformationVerified) {
                                        val leftDivision = divisionTree(leftExpressionComparison.leftExpression.data, leftExpressionComparison.rightExpression.data)
                                        val rightDivision = divisionTree(rightExpressionComparison.leftExpression.data, rightExpressionComparison.rightExpression.data)
                                        val divisionResult = factComporator.expressionComporator.compareWithoutSubstitutions(leftDivision, rightDivision, ComparisonType.EQUAL,
                                                justInDomainsIntersection = true)
                                        if (divisionResult) {
                                            transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                                            log.addMessage({ "Transformation verified: 'left.left / left.right = right.left / right.right'" }, MessageType.USER, level = currentLogLevel)
                                        }
                                    }
                                }
                                ComparisonType.LEFT_MORE, ComparisonType.LEFT_MORE_OR_EQUAL -> {
                                    val ll = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.leftExpression.data,
                                            rightExpressionComparison.leftExpression.data, actualExpressionTransformations,
                                            expressionChainComparisonType = ComparisonType.LEFT_LESS_OR_EQUAL,
                                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                    log.add(ll, { "left.left <= right.left: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                    val rr = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.rightExpression.data,
                                            rightExpressionComparison.rightExpression.data, actualExpressionTransformations,
                                            expressionChainComparisonType = ComparisonType.LEFT_MORE_OR_EQUAL,
                                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                    log.add(rr, { "left.right >= right.right: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                    if (ll && rr) {
                                        transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                                        log.addMessage({ "Transformation verified: 'left.left <= right.left' and 'left.right >= right.right'" }, MessageType.USER, level = currentLogLevel)
                                    } else {
                                        log.addMessage({ "Transformation not verified" }, MessageType.USER, level = currentLogLevel)
                                    }
                                }
                                ComparisonType.LEFT_LESS, ComparisonType.LEFT_LESS_OR_EQUAL -> {
                                    val ll = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.leftExpression.data,
                                            rightExpressionComparison.leftExpression.data, actualExpressionTransformations,
                                            expressionChainComparisonType = ComparisonType.LEFT_MORE_OR_EQUAL,
                                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                    log.add(ll, { "left.left >= right.left: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                    val rr = factComporator.expressionComporator.compareWithTreeTransformationRules(leftExpressionComparison.rightExpression.data,
                                            rightExpressionComparison.rightExpression.data, actualExpressionTransformations,
                                            expressionChainComparisonType = ComparisonType.LEFT_LESS_OR_EQUAL,
                                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                                    log.add(rr, { "left.right <= right.right: '" }, { "'" }, level = currentLogLevel, messageType = MessageType.USER)
                                    if (ll && rr) {
                                        transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                                        log.addMessage({ "Transformation verified: 'left.left >= right.left' and 'left.right <= right.right'" }, MessageType.USER, level = currentLogLevel)
                                    } else {
                                        log.addMessage({ "Transformation not verified" }, MessageType.USER, level = currentLogLevel)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var leftFacts: List<MainChainPart>? = null
                    var rightFacts: List<MainChainPart>? = null
                    if (chain[currentLeftIndex].type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE &&
                            (chain[currentLeftIndex] as MainLineAndNode).outFacts.size == (chain[currentRightIndex] as MainLineAndNode).inFacts.size &&
                            (chain[currentLeftIndex] as MainLineAndNode).outFacts.count { it.type() != ComparableTransformationPartType.EXPRESSION_COMPARISON } == 0 &&
                            (chain[currentRightIndex] as MainLineAndNode).inFacts.count { it.type() != ComparableTransformationPartType.EXPRESSION_COMPARISON } == 0) {
                        leftFacts = (chain[currentLeftIndex] as MainLineAndNode).outFacts
                        rightFacts = (chain[currentRightIndex] as MainLineAndNode).inFacts
                    } else if (chain[currentLeftIndex].type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE &&
                            (chain[currentLeftIndex] as MainLineOrNode).outFacts.size == (chain[currentRightIndex] as MainLineOrNode).inFacts.size &&
                            (chain[currentLeftIndex] as MainLineOrNode).outFacts.count { it.type() != ComparableTransformationPartType.EXPRESSION_COMPARISON } == 0 &&
                            (chain[currentRightIndex] as MainLineOrNode).inFacts.count { it.type() != ComparableTransformationPartType.EXPRESSION_COMPARISON } == 0) {
                        leftFacts = (chain[currentLeftIndex] as MainLineOrNode).outFacts
                        rightFacts = (chain[currentRightIndex] as MainLineOrNode).inFacts
                    }
                    if (leftFacts != null && rightFacts != null) {
                        log.addMessage({ "Two expression comparisons systems with equal sizes identified" }, MessageType.USER, level = currentLogLevel)
                        log.logSystemFacts(chain[currentLeftIndex].type(), leftFacts, { "left" }, MessageType.USER)
                        log.logSystemFacts(chain[currentRightIndex].type(), rightFacts, { "right" }, MessageType.USER)
                        var hasDifferentComparisonType = log.assignAndLog(false, currentLogLevel, { "hasDifferentComparisonType" })
                        var expressionTransformationRulesFromLeftFact = mutableListOf<ExpressionSubstitution>()
                        for (i in 0..leftFacts.lastIndex) {
                            expressionTransformationRulesFromLeftFact.add(ExpressionSubstitution(
                                    left = (leftFacts[i] as ExpressionComparison).leftExpression.data,
                                    right = (leftFacts[i] as ExpressionComparison).rightExpression.data,
                                    basedOnTaskContext = true,
                                    comparisonType = (leftFacts[i] as ExpressionComparison).comparisonType)
                            )
                            log.addMessageWithExpressionSubstitutionShort({ "Expression substitution got from left system: " }, expressionTransformationRulesFromLeftFact.last(), MessageType.USER, level = currentLogLevel)
                        }
                        for (i in 0..leftFacts.lastIndex) {
                            if ((leftFacts[i] as ExpressionComparison).comparisonType != (rightFacts[i] as ExpressionComparison).comparisonType) {
                                log.add(i, { "Two expression comparisons has different signs at facts number '" }, { "'" }, messageType = MessageType.USER, level = currentLogLevel)
                                hasDifferentComparisonType = log.assignAndLog(true, currentLogLevel, { "hasDifferentComparisonType" })
                                break
                            }
                        }
                        if (!hasDifferentComparisonType) {
                            log.addMessage({ "Check expression comparisons pairs on the same positions" }, MessageType.USER, level = currentLogLevel)
                            var uncorrectTransformationFound = log.assignAndLog(false, currentLogLevel, { "uncorrectTransformationFound" })
                            for (i in 0..leftFacts.lastIndex) {
                                val mainChain = MainChain(mutableListOf(leftFacts[i], rightFacts[i]))
                                val result = mainChain.check(factComporator, true, actualFactsTransformations,
                                        actualExpressionTransformations + expressionTransformationRulesFromLeftFact, listOf())
                                if (!result.isCorrect) {
                                    uncorrectTransformationFound = log.assignAndLog(true, currentLogLevel, { "uncorrectTransformationFound" })
                                    log.add(i, { "Uncorrect transformation found at '" }, { "'" }, messageType = MessageType.USER, level = currentLogLevel)
                                    break
                                }
                            }
                            if (!uncorrectTransformationFound) {
                                log.addMessage({ "Transformation verified, all pairs of expression comparisons are transformed correctly" }, MessageType.USER, level = currentLogLevel)
                                transformationVerified = log.assignAndLog(true, currentLogLevel, { "transformationVerified" })
                            }
                        }
                    }
                }
            }
            if (!transformationVerified) {
                log.addMessage({ "Transformation not verified, try to check transformation with rules and additional facts" })
                val additionalFactInCurrentTransformationApplicationUsed = mutableListOf<Boolean>()
                val result = factComporator.compareWithTreeTransformationRules(
                        chain[currentLeftIndex], chain[currentRightIndex], additionalFacts,
                        actualFactsTransformations, additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)
                if (result) {
                    log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factHelpFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', factHelpFactColor = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factHelpFactColor))
                    val isInTaskContext = additionalFactInCurrentTransformationApplicationUsed.isNotEmpty()
                    log.add(isInTaskContext, { "Transformation verified, isInTaskContext: '" }, { "'" }, messageType = MessageType.USER, level = currentLogLevel)
                    if (isInTaskContext) {
                        additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })
                    }
                } else {
                    log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', wrongFactColor = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongFactColor))
                    log.addMessage({ "$verificationFailed" }, MessageType.USER, level = currentLogLevel)
                    return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex],
                            "Unclear transformation between '${chain[currentLeftIndex]}' and '${chain[currentRightIndex]}' ")
                }
            } else {
                log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                        factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor,
                        { "Coloring task on positions: '" }, { "' - '" }, { "', correctFactColor = '" }, { "" }, level = currentLogLevel)
                coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                        factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor))
                log.addMessage({ "${CheckingKeyWords.transformationVerified}" }, MessageType.USER, level = currentLogLevel)
            }

            if (chain[currentRightIndex].type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE ||
                    chain[currentRightIndex].type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
                for (transformationChain in (chain[currentRightIndex] as MainLineNode).factTransformationChains) {
                    (chain[currentRightIndex] as MainLineNode).outFacts.add(transformationChain.chain.last())
                }
            }

            currentLogLevel--
            currentLeftIndex = log.assignAndLog(currentRightIndex, currentLogLevel, { "currentLeftIndex" })
            currentRightIndex = log.assignAndLog(currentRightIndex + 1, currentLogLevel, { "currentRightIndex" })
        }
        log.addMessage({ "$factChainVerified. '${chain.first()}' -> '${chain.last()}'" },
                messageType = MessageType.USER, level = currentLogLevel)
        return ComparisonResult(true, coloringTasks, chain.first(), chain.last(), additionalFactUsed = additionalFactUsed)
    }

    fun variableReplacement(replacements: Map<String, String>) {
        chain.forEach { it.variableReplacement(replacements) }
    }
}

class MainLineAndNode(
        override val startPosition: Int = 0,
        override val endPosition: Int = 0,
        override var parent: MainLineNode? = null,
        override val factTransformationChains: MutableList<MainChain> = mutableListOf(),
        override val inFacts: MutableList<MainChainPart> = mutableListOf(),
        override val outFacts: MutableList<MainChainPart> = mutableListOf(),
        override val expressionTransformationChains: MutableList<ExpressionChain> = mutableListOf(), //correct chains have to be packed in in and out node
        override val rules: MutableList<Rule> = mutableListOf(),
        override var identifier: String = ""
//todo: add not data transformation rules (rules for facts and solution nodes)
) : MainLineNode {
    override fun getLastExpression() = outFacts.last().getLastExpression()

    override fun isSolutionForVariables(targetVariables: MutableMap<String, Boolean>, left: Boolean, allowedVariables: Set<String>): GeneralError? {
        for (fact in outFacts) {
            val error = fact.isSolutionForVariables(targetVariables, allowedVariables = allowedVariables)
            if (error != null) return error
        }
//        for (factTransformationChain in factTransformationChains){
//            val error = factTransformationChain.chain.last().isSolutionForVariables(targetVariables)
//            if (error != null) return error
//        }
        return null
    }

    override fun computeIfNumeric(substitutionInstance: SubstitutionInstance, baseOperationsDefinitions: BaseOperationsDefinitions, checkOutMainLineNodePart: Boolean): Boolean? {
        val actualFacts = if (checkOutMainLineNodePart) getOutFactsFromMainLineNode(this)
        else getInFactsFromMainLineNode(this)
        var isNull = false
        for (fact in actualFacts) {
            val isCorrect = when (fact.type()) {
                ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                    (fact as ExpressionComparison).computeIfNumeric(substitutionInstance, baseOperationsDefinitions)
                }
                ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                    (fact as MainLineNode).computeIfNumeric(substitutionInstance, baseOperationsDefinitions, checkOutMainLineNodePart)
                }
                else -> null
            }
            if (isCorrect == null) {
                isNull = true
            } else if (!isCorrect) {
                return false
            }
        }
        return if (isNull) null else true
    }

    override fun copyNode() = MainLineAndNode(startPosition, endPosition, parent)
    override fun clone() = MainLineAndNode(startPosition, endPosition, parent,
            inFacts = inFacts.map { it.clone() }.toMutableList(),
            outFacts = outFacts.map { it.clone() }.toMutableList())

    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) = MainLineAndNode(startPosition, endPosition, parent,
            inFacts = inFacts.map { it.cloneWithNormalization(nameArgsMap, sorted) }.toMutableList(),
            outFacts = outFacts.map { it.cloneWithNormalization(nameArgsMap, sorted) }.toMutableList())

    override fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean) {
        inFacts.forEach { it.normalizeSubTree(currentDeep, nameArgsMap, sorted) }
        outFacts.forEach { it.normalizeSubTree(currentDeep, nameArgsMap, sorted) }
    }

    override fun variableReplacement(replacements: Map<String, String>) {
        inFacts.forEach { it.variableReplacement(replacements) }
        outFacts.forEach { it.variableReplacement(replacements) }
        rules.forEach { it.variableReplacement(replacements) }
        expressionTransformationChains.forEach { it.variableReplacement(replacements) }
        factTransformationChains.forEach { it.variableReplacement(replacements) }
    }

    override fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {
        inFacts.forEach { it.applyAllExpressionSubstitutions(expressionSubstitutions) }
        outFacts.forEach { it.applyAllExpressionSubstitutions(expressionSubstitutions) }
    }

    override fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions) {
        inFacts.forEach { it.computeExpressionTrees(baseOperationsDefinitions) }
        outFacts.forEach { it.computeExpressionTrees(baseOperationsDefinitions) }
    }

    override fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>) {
        inFacts.forEach { it.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet) }
        outFacts.forEach { it.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet) }
    }

    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessageWithFactDetail({ "Start checking fact: " }, this, MessageType.USER, levelChange = 1)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        log.logCheckParams(onExpressionLevel = onExpressionLevel, factsTransformations = factsTransformations,
                expressionTransformations = expressionTransformations, additionalFacts = additionalFacts)
        val coloringTasks = mutableListOf<ColoringTask>()
        var additionalFactUsed = log.assignAndLog(false, currentLogLevel, { "additionalFactUsed" })
        val nodeExpressionTransformations = mutableListOf<ExpressionSubstitution>()
        val nodeFactsTransformations = mutableListOf<FactSubstitution>()
        log.addMessage({ "0. $inFact extraction" }, MessageType.USER, level = currentLogLevel)
        for (factChain in factTransformationChains) {
            val checkingResult = factChain.chain.first().check(factComporator, false, factsTransformations, expressionTransformations, additionalFacts)
            coloringTasks.addAll(checkingResult.coloringTasks)
            if (checkingResult.isCorrect) {
                log.addMessageWithFactDetail({ "fact checked: " }, factChain.chain.first(), MessageType.USER, level = currentLogLevel)
                if (checkingResult.additionalFactUsed) {
                    additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })
                }
            } else {
                log.addMessage({ "FACT $verificationFailed" }, MessageType.USER, level = currentLogLevel)
                log.add(factChain.chain.first(), { "FACT '''" }, { "''' $verificationFailed" }, level = currentLogLevel)
                return checkingResult
            }
        }
        log.addMessage({ "1. ${CheckingKeyWords.rule} checking" }, MessageType.USER, level = currentLogLevel)
        currentLogLevel += 1
        val knownFacts = (additionalFacts + inFacts).toMutableList()
        for (rule in rules) {
            val checkingResult = rule.check(factComporator, false,
                    factsTransformations + nodeFactsTransformations,
                    expressionTransformations + nodeExpressionTransformations,
                    additionalFacts + inFacts)
            coloringTasks.addAll(checkingResult.coloringTasks)
            if (checkingResult.isCorrect) {
                if ((rule.factSubstitution != null || rule.expressionSubstitution != null)) {
                    if (rule.factSubstitution != null) {
                        nodeFactsTransformations.add(rule.factSubstitution!!)
                        log.addMessageWithFactSubstitutionDetail({ "${CheckingKeyWords.factSubstitution} $ruleAddedInContext:" }, rule.factSubstitution!!, MessageType.USER, level = currentLogLevel)
                        var leftFactIsKnown = log.assignAndLog(false, currentLogLevel, { "leftFactIsKnown" })
                        for (knownFact in knownFacts) {
                            log.addMessageWithFactShort({ "compare left rule part with known fact: " }, knownFact, level = currentLogLevel)
                            if (factComporator.compareAsIs(knownFact, rule.factSubstitution!!.left)) {
                                leftFactIsKnown = log.assignAndLog(true, currentLogLevel, { "leftFactIsKnown" })
                                break
                            }
                        }
                        if (leftFactIsKnown) {
                            knownFacts.add(rule.factSubstitution!!.right)
                            log.addMessage({ "left rule fact is known, so right rule fact is added to known facts" }, MessageType.USER, level = currentLogLevel)
                            if (rule.factSubstitution!!.right.type() == ComparableTransformationPartType.EXPRESSION_COMPARISON) {
                                val ruleData = rule.factSubstitution!!.right as ExpressionComparison
                                val expressionSubstitution = ExpressionSubstitution(
                                        ruleData.leftExpression.data,
                                        ruleData.rightExpression.data,
                                        basedOnTaskContext = true,
                                        comparisonType = ruleData.comparisonType)
                                nodeExpressionTransformations.add(expressionSubstitution)
                                log.addMessageWithExpressionSubstitutionShort({
                                    "rule is expression comparison, so ${CheckingKeyWords.expressionSubstitution} " + ruleAddedInContext
                                }, expressionSubstitution, level = currentLogLevel)
                            }
                        }
                    } else {
                        nodeExpressionTransformations.add(rule.expressionSubstitution!!)
                        log.addMessageWithExpressionSubstitutionShort({ "${CheckingKeyWords.expressionSubstitution} $ruleAddedInContext:" }, rule.expressionSubstitution!!, MessageType.USER, level = currentLogLevel)
                    }
                }
            } else {
                log.addMessage({ "${CheckingKeyWords.rule} $verificationFailed" }, MessageType.USER, level = currentLogLevel)
                log.add(rule, { "${CheckingKeyWords.rule} '''" }, { "''' $verificationFailed" }, level = currentLogLevel)
                return ComparisonResult(false, coloringTasks,
                        this, this, description = checkingResult.description)
            }
        }
        log.addMessage({ "2. ${CheckingKeyWords.expressionChain} checking" }, MessageType.USER, level = currentLogLevel - 1)
        for (expressionChain in expressionTransformationChains) {
            val checkingResult = expressionChain.check(factComporator, false,
                    factsTransformations + nodeFactsTransformations,
                    expressionTransformations + nodeExpressionTransformations,
                    additionalFacts + inFacts)
            coloringTasks.addAll(checkingResult.coloringTasks)
            if (checkingResult.isCorrect) {
                outFacts.add(ExpressionComparison(leftExpression = expressionChain.chain.first() as Expression,
                        rightExpression = expressionChain.chain.last() as Expression,
                        comparisonType = expressionChain.comparisonType,
                        parent = this))
                log.addMessageWithFactDetail({ "To out facts added fact: " }, outFacts.last(), MessageType.USER, level = currentLogLevel)
                if (checkingResult.additionalFactUsed) {
                    additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })
                }
            } else {
                log.addMessage({ "${CheckingKeyWords.expressionChain} $verificationFailed" }, MessageType.USER, level = currentLogLevel)
                log.add(expressionChain, { "${CheckingKeyWords.expressionChain} '''" }, { "''' $verificationFailed" }, level = currentLogLevel)
                return ComparisonResult(false, coloringTasks,
                        this, this, description = checkingResult.description)
            }
        }
        log.addMessage({ "3. ${CheckingKeyWords.factChain} checking" }, MessageType.USER, level = currentLogLevel - 1)
        for (factChain in factTransformationChains) {
            val checkingResult = factChain.check(factComporator, false,
                    factsTransformations + nodeFactsTransformations,
                    expressionTransformations + nodeExpressionTransformations,
                    additionalFacts + inFacts)
            coloringTasks.addAll(checkingResult.coloringTasks)
            if (checkingResult.isCorrect) {
                outFacts.add(factChain.chain.last())
                log.addMessageWithFactDetail({ "To out facts added fact: " }, outFacts.last(), MessageType.USER, level = currentLogLevel)
                if (checkingResult.additionalFactUsed) {
                    additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })
                }
            } else {
                log.addMessage({ "${CheckingKeyWords.factChain} $verificationFailed" }, MessageType.USER, level = currentLogLevel)
                log.add(factChain, { "${CheckingKeyWords.factChain} '''" }, { "''' $verificationFailed" }, level = currentLogLevel)
                return ComparisonResult(false, coloringTasks,
                        this, this, description = checkingResult.description)
            }
        }
        log.addMessageWithFactDetail({ "Fact node checked and it is correct, out facts are computed: " }, this, MessageType.USER, level = currentLogLevel - 1)
        return ComparisonResult(true, coloringTasks, this, this, additionalFactUsed = additionalFactUsed)
    }

    override fun getActualChain() = factTransformationChains.last().chain

    override fun addStartNewFactChain() {
        factTransformationChains.add(MainChain())
    }

    override fun addExpressionComparisonFact(fact: MainChainPart) {
        if (factTransformationChains.isEmpty()) {
            addStartNewFactChain()
        }
        getActualChain().add(fact)
    }

    override fun type() = ComparableTransformationPartType.MAIN_LINE_AND_NODE
    override fun toString() = "AND_NODE(" +
            if (rules.isNotEmpty()) {
                "rules:(${rules.joinToString(separator = ";") { it.toString() }});"
            } else {
                ""
            } +
            if (expressionTransformationChains.isNotEmpty()) {
                "transformation chains:(${expressionTransformationChains.joinToString(separator = ";") { it.toString() }});"
            } else {
                ""
            } +
            if (factTransformationChains.isNotEmpty()) {
                "facts chains:(${factTransformationChains.joinToString(separator = ";") { it.toString() }})"
            } else {
                ""
            } +
            if (inFacts.isNotEmpty()) {
                "in:(${inFacts.joinToString(separator = ";") { it.toString() }})"
            } else {
                ""
            } +
            if (outFacts.isNotEmpty()) {
                "out:(${outFacts.joinToString(separator = ";") { it.toString() }})"
            } else {
                ""
            } +
            ")"


    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = "AND_NODE(${inFacts.joinToString(separator = ";mn;") { it.computeIdentifier(recomputeIfComputed) }}" +
                    "${if (outFacts.isNotEmpty()) ";-->>;(${outFacts.joinToString(separator = ";mn;") { it.computeIdentifier(recomputeIfComputed) }}" else ""})"
        }
        return identifier
    }

    var inIdentifier: String = ""
    var outIdentifier: String = ""
    override fun computeInIdentifier(recomputeIfComputed: Boolean): String {
        if (inIdentifier.isBlank() || recomputeIfComputed) {
            inIdentifier = "AND_NODE(${inFacts.joinToString(separator = ";mn;") { it.computeInIdentifier(recomputeIfComputed) }})"
        }
        return inIdentifier
    }

    override fun computeOutIdentifier(recomputeIfComputed: Boolean): String {
        if (outIdentifier.isBlank() || recomputeIfComputed) {
            outIdentifier = "AND_NODE(${outFacts.joinToString(separator = ";mn;") { it.computeOutIdentifier(recomputeIfComputed) }})"
        }
        return outIdentifier
    }

    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean): String {
        val childrenFactsIdentifiers = outFacts.map { it.computeSortedOutIdentifier(recomputeIfComputed) }
        return "AND_NODE(${childrenFactsIdentifiers.sorted().joinToString(separator = ";mn;") { it }})"
    }

    override fun isFactorizationForVariables(minNumberOfMultipliers: Int, targetVariables: Set<String>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (outFacts.isNotEmpty()) {
            return outFacts.last().isFactorizationForVariables(minNumberOfMultipliers, targetVariables, targetExpression, factComporator)
        }
        return GeneralError("No answer")
    }

    override fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (outFacts.isNotEmpty()) {
            return outFacts.last().isSolutionWithoutFunctions(forbidden, targetExpression, factComporator)
        }
        return GeneralError("No answer")
    }

    override fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (outFacts.isNotEmpty()) {
            return outFacts.last().hasNoFractions(maxNumberOfDivisions, targetExpression, factComporator)
        }
        return GeneralError("No answer")
    }

    companion object {
        fun parseFromFactIdentifier(string: String, parent: MainLineNode? = null, functionConfiguration: FunctionConfiguration = FunctionConfiguration()): MainLineAndNode? {
            val mainPart = string.substring("AND_NODE(".length, string.length - ")".length)
            val parts = splitBySubstringOnTopLevel(listOf(";-->>;"), mainPart).map { mainPart.substring(it.startPosition, it.endPosition) }
            val result = MainLineAndNode(parent = parent)
            result.inFacts.addAll(parsePartsFromIdentifier(parts[0], parent, functionConfiguration))
            result.outFacts.addAll(parsePartsFromIdentifier(if (parts.size > 1) parts[1] else parts[0], parent, functionConfiguration))
            return result
        }
    }

}

class MainLineOrNode(
        override val startPosition: Int = 0,
        override val endPosition: Int = 0,
        override var parent: MainLineNode? = null,
        override val factTransformationChains: MutableList<MainChain> = mutableListOf(),
        override val inFacts: MutableList<MainChainPart> = mutableListOf(),
        override val outFacts: MutableList<MainChainPart> = mutableListOf(),
        override val expressionTransformationChains: MutableList<ExpressionChain> = mutableListOf(), //correct chains have to be packed in in and out node
        override val rules: MutableList<Rule> = mutableListOf(),
        override var identifier: String = ""
        // may be rules should be just in 'MainLineAndNode'. But if it here, notation will be easier
) : MainLineNode {
    override fun getLastExpression() = outFacts.last().getLastExpression()

    override fun isSolutionForVariables(targetVariables: MutableMap<String, Boolean>, left: Boolean, allowedVariables: Set<String>): GeneralError? {
        for (fact in outFacts) {
            val error = fact.isSolutionForVariables(targetVariables, allowedVariables = allowedVariables)
            if (error != null) return error
        }
        return null
    }

    override fun computeIfNumeric(substitutionInstance: SubstitutionInstance, baseOperationsDefinitions: BaseOperationsDefinitions, checkOutMainLineNodePart: Boolean): Boolean? {
        val actualFacts = if (checkOutMainLineNodePart) getOutFactsFromMainLineNode(this)
        else getInFactsFromMainLineNode(this)
        var isNull = false
        for (fact in actualFacts) {
            val isCorrect = when (fact.type()) {
                ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                    (fact as ExpressionComparison).computeIfNumeric(substitutionInstance, baseOperationsDefinitions)
                }
                ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                    (fact as MainLineNode).computeIfNumeric(substitutionInstance, baseOperationsDefinitions, checkOutMainLineNodePart)
                }
                else -> null
            }
            if (isCorrect == null) {
                isNull = true
            } else if (isCorrect) {
                return true
            }
        }
        return if (isNull) null else false
    }

    override fun copyNode() = MainLineOrNode(startPosition, endPosition, parent)
    override fun clone() = MainLineOrNode(startPosition, endPosition, parent,
            inFacts = inFacts.map { it.clone() }.toMutableList(),
            outFacts = outFacts.map { it.clone() }.toMutableList())

    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) = MainLineOrNode(startPosition, endPosition, parent,
            inFacts = inFacts.map { it.cloneWithNormalization(nameArgsMap, sorted) }.toMutableList(),
            outFacts = outFacts.map { it.cloneWithNormalization(nameArgsMap, sorted) }.toMutableList())

    override fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean) {
        inFacts.forEach { it.normalizeSubTree(currentDeep, nameArgsMap, sorted) }
        outFacts.forEach { it.normalizeSubTree(currentDeep, nameArgsMap, sorted) }
    }

    override fun variableReplacement(replacements: Map<String, String>) {
        inFacts.forEach { it.variableReplacement(replacements) }
        outFacts.forEach { it.variableReplacement(replacements) }
        rules.forEach { it.variableReplacement(replacements) }
        expressionTransformationChains.forEach { it.variableReplacement(replacements) }
        factTransformationChains.forEach { it.variableReplacement(replacements) }
    }

    override fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {
        inFacts.forEach { it.applyAllExpressionSubstitutions(expressionSubstitutions) }
        outFacts.forEach { it.applyAllExpressionSubstitutions(expressionSubstitutions) }
    }

    override fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions) {
        inFacts.forEach { it.computeExpressionTrees(baseOperationsDefinitions) }
        outFacts.forEach { it.computeExpressionTrees(baseOperationsDefinitions) }
    }

    override fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>) {
        inFacts.forEach { it.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet) }
        outFacts.forEach { it.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet) }
    }

    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActualChain() = factTransformationChains.last().chain

    override fun addStartNewFactChain() {
        factTransformationChains.add(MainChain())
    }

    override fun addExpressionComparisonFact(fact: MainChainPart) {
        if (factTransformationChains.isEmpty()) {
            addStartNewFactChain()
        }
        getActualChain().add(fact)
    }

    override fun type() = ComparableTransformationPartType.MAIN_LINE_OR_NODE
    override fun toString() = "OR_NODE(" +
            if (rules.isNotEmpty()) {
                "rules:(${rules.joinToString(separator = ";") { it.toString() }});"
            } else {
                ""
            } +
            if (expressionTransformationChains.isNotEmpty()) {
                "transformation chains:(${expressionTransformationChains.joinToString(separator = ";") { it.toString() }});"
            } else {
                ""
            } +
            if (factTransformationChains.isNotEmpty()) {
                "facts chains:(${factTransformationChains.joinToString(separator = ";") { it.toString() }})"
            } else {
                ""
            } +
            if (inFacts.isNotEmpty()) {
                "in:(${inFacts.joinToString(separator = ";") { it.toString() }})"
            } else {
                ""
            } +
            if (outFacts.isNotEmpty()) {
                "out:(${outFacts.joinToString(separator = ";") { it.toString() }})"
            } else {
                ""
            } +
            ")"

    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = "OR_NODE(${inFacts.joinToString(separator = ";mn;") { it.computeIdentifier(recomputeIfComputed) }}" +
                    "${if (outFacts.isNotEmpty()) ";-->>;(${outFacts.joinToString(separator = ";mn;") { it.computeIdentifier(recomputeIfComputed) }}" else ""})"
        }
        return identifier
    }

    var inIdentifier: String = ""
    var outIdentifier: String = ""
    override fun computeInIdentifier(recomputeIfComputed: Boolean): String {
        if (inIdentifier.isBlank() || recomputeIfComputed) {
            inIdentifier = "OR_NODE(${inFacts.joinToString(separator = ";mn;") { it.computeInIdentifier(recomputeIfComputed) }})"
        }
        return inIdentifier
    }

    override fun computeOutIdentifier(recomputeIfComputed: Boolean): String {
        if (outIdentifier.isBlank() || recomputeIfComputed) {
            outIdentifier = "OR_NODE(${outFacts.joinToString(separator = ";mn;") { it.computeOutIdentifier(recomputeIfComputed) }})"
        }
        return outIdentifier
    }

    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean): String {
        val childrenFactsIdentifiers = outFacts.map { it.computeSortedOutIdentifier(recomputeIfComputed) }
        return "OR_NODE(${childrenFactsIdentifiers.sorted().joinToString(separator = ";mn;") { it }})"
    }

    companion object {
        fun parseFromFactIdentifier(string: String, parent: MainLineNode? = null, functionConfiguration: FunctionConfiguration = FunctionConfiguration()): MainLineOrNode? {
            val mainPart = string.substring("OR_NODE(".length, string.length - ")".length)
            val parts = splitBySubstringOnTopLevel(listOf(";-->>;"), mainPart).map { mainPart.substring(it.startPosition, it.endPosition) }
            val result = MainLineOrNode(parent = parent)
            result.inFacts.addAll(parsePartsFromIdentifier(parts[0], parent, functionConfiguration))
            result.outFacts.addAll(parsePartsFromIdentifier(if (parts.size > 1) parts[1] else parts[0], parent, functionConfiguration))
            return result
        }
    }

    override fun isFactorizationForVariables(minNumberOfMultipliers: Int, targetVariables: Set<String>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (outFacts.isNotEmpty()) {
            return outFacts.last().isFactorizationForVariables(minNumberOfMultipliers, targetVariables, targetExpression, factComporator)
        }
        return GeneralError("No answer")
    }

    override fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (outFacts.isNotEmpty()) {
            return outFacts.last().isSolutionWithoutFunctions(forbidden, targetExpression, factComporator)
        }
        return GeneralError("No answer")
    }

    override fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (outFacts.isNotEmpty()) {
            return outFacts.last().hasNoFractions(maxNumberOfDivisions, targetExpression, factComporator)
        }
        return GeneralError("No answer")
    }
}

fun getInFactsFromMainLineNode(factNode: ComparableTransformationsPart) =
        if (factNode.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE) (factNode as MainLineAndNode).inFacts
        else (factNode as MainLineOrNode).inFacts

fun getOutFactsFromMainLineNode(factNode: ComparableTransformationsPart) =
        if (factNode.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE) (factNode as MainLineAndNode).outFacts
        else (factNode as MainLineOrNode).outFacts


fun parsePartsFromIdentifier(string: String, parent: MainLineNode? = null, functionConfiguration: FunctionConfiguration): MutableList<MainChainPart> {
    val parts = splitBySubstringOnTopLevel(listOf(";mn;"), string).map { string.substring(it.startPosition, it.endPosition) }
    val result = mutableListOf<MainChainPart>()
    for (part in parts) {
        result.add(parseFromFactIdentifier(part, parent, functionConfiguration) ?: continue)
    }
    return result
}

fun parseFromFactIdentifier(string: String, parent: MainLineNode? = null, functionConfiguration: FunctionConfiguration = FunctionConfiguration())
        = if (string.startsWith("AND_NODE(")) {
    MainLineAndNode.parseFromFactIdentifier(string, parent, functionConfiguration)
} else if (string.startsWith("OR_NODE(")) {
    MainLineOrNode.parseFromFactIdentifier(string, parent, functionConfiguration)
} else if (string.contains(";ec;")) {
    ExpressionComparison.parseFromFactIdentifier(string, parent, functionConfiguration)
} else {
    Expression.parseFromFactIdentifier(string, parent, functionConfiguration)
}

fun normalizeFactsForComparison(left: MainChainPart, right: MainChainPart): Pair<MainChainPart, MainChainPart> {
    val ordered = if (left.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE) Pair(left, right)
    else if (right.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE) Pair(right, left)
    else return Pair(left, right)

    if (ordered.second.type() != ComparableTransformationPartType.MAIN_LINE_AND_NODE) {
        return Pair(ordered.first, MainLineAndNode().apply {
            inFacts.add(ordered.second)
            outFacts.add(ordered.second)
        })
    } else return ordered
}

fun factWrapperForCheckingTransformations(fact: MainChainPart, checkOutMainLineNodePart: Boolean): MainChainPart {
    val actualFacts = if (fact.type() != ComparableTransformationPartType.MAIN_LINE_AND_NODE && fact.type() != ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
        mutableListOf(fact)
    } else if (checkOutMainLineNodePart) {
        getOutFactsFromMainLineNode(fact)
    } else {
        getInFactsFromMainLineNode(fact)
    }
    val wrapper = if (fact.type() != ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
        MainLineAndNode(inFacts = actualFacts, outFacts = actualFacts)
    } else {
        MainLineOrNode(inFacts = actualFacts, outFacts = actualFacts)
    }
    return MainLineAndNode(inFacts = mutableListOf(wrapper), outFacts = mutableListOf(wrapper))
}

fun MainLineAndNode.checkTransformationChain(
        factComporator: FactComporator,
        onExpressionLevel: Boolean,
        additionalFacts: List<MainChainPart>): ComparisonResult {
    val compiledConfiguration = factComporator.compiledConfiguration
    this.variableReplacement(compiledConfiguration.compiledImmediateVariableReplacements)
    additionalFacts.forEach { it.variableReplacement(compiledConfiguration.compiledImmediateVariableReplacements) }
    return this.check(factComporator,
            onExpressionLevel,
            compiledConfiguration.compiledFactTreeTransformationRules,
            compiledConfiguration.compiledExpressionTreeTransformationRules,
            additionalFacts)
}


class FactConstructorViewer(
        val compiledConfiguration: CompiledConfiguration = CompiledConfiguration(),
        val expressionNodeConstructor: ExpressionNodeConstructor = ExpressionNodeConstructor(
                compiledConfiguration.functionConfiguration,
                compiledConfiguration.compiledImmediateVariableReplacements),
        val openBracket: Char = '{',
        val closeBracket: Char = '}',
        val mainLineNodePartSuffix: String = ", "
) {
    fun constructFactUserView(fact: MainChainPart): String {
        val result = StringBuilder()
        when (fact.type()) {
            ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                result.append(fact.type().toString().split("_")[2] + "{")
                fact as MainLineNode
                if (fact.inFacts.isNotEmpty() && fact.outFacts.isNotEmpty()) {
                    val inFactsIdentifier = fact.inFacts.joinToString(separator = "; ") { constructFactUserView(it) }
                    val outFactsIdentifier = fact.outFacts.joinToString(separator = "; ") { constructFactUserView(it) }

                    if (inFactsIdentifier == outFactsIdentifier) {
                        result.append(inFactsIdentifier)
                    } else {
                        result.append(inFactsIdentifier)
                        result.append(" --> ")
                        result.append(outFactsIdentifier)
                    }
                    result.append(mainLineNodePartSuffix)
                }
                if (fact.rules.isNotEmpty()) {
                    result.append("RULES: ")
                    result.append(fact.rules.joinToString(separator = "") {
                        constructFactUserView(it)
                    })
                    result.append(mainLineNodePartSuffix)
                }
                if (fact.factTransformationChains.isNotEmpty()) {
                    result.append("FACTS_CHAINS: ")
                    result.append(fact.factTransformationChains.joinToString(separator = "; ") {
                        it.chain.joinToString(separator = "") {
                            "[${constructFactUserView(it)}]"
                        }
                    })
                    result.append(mainLineNodePartSuffix)
                }
                if (fact.expressionTransformationChains.isNotEmpty()) {
                    result.append("EXPRESSION_CHAINS: ")
                    result.append(fact.expressionTransformationChains.joinToString(separator = "; ") {
                        it.chain.joinToString(separator = it.comparisonType.string) {
                            if (it.type() == ComparableTransformationPartType.EXPRESSION) {
                                constructFactUserView(it as Expression)
                            } else if (it.type() == ComparableTransformationPartType.RULE) {
                                constructFactUserView(it as Rule)
                            } else {
                                constructFactUserView(it as RulePointer)
                            }
                        }
                    })
                    result.append(mainLineNodePartSuffix)
                }
                return if (result.endsWith('{')) {
                    result.toString() + "}"
                } else {
                    (result.dropLast(mainLineNodePartSuffix.length).toString() + "}")
                }
            }
            ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                result.append(fact.type().toString() + ": ")
                fact as ExpressionComparison
                result.append(constructFactUserView(fact.leftExpression))
                result.append(" ${fact.comparisonType.string} ")
                result.append(constructFactUserView(fact.rightExpression))
            }
            ComparableTransformationPartType.EXPRESSION -> {
                result.append((fact as Expression).data.toString())
            }
            ComparableTransformationPartType.RULE -> {
                fact as Rule
                if (fact.factSubstitution != null || fact.expressionSubstitution != null) {
                    if (fact.factSubstitution != null) {
                        "[${constructFactUserView(fact.factSubstitution!!.left)} ${fact.factSubstitution!!.direction.toUserString()} ${constructFactUserView(fact.factSubstitution!!.right)}" +
                                ", actual " +
                                "${if (fact.factSubstitution!!.basedOnTaskContext) "only in task context" else "everywhere"}]"
                    } else "" +
                            if (fact.expressionSubstitution != null) {
                                "[${fact.expressionSubstitution!!.left} -> ${fact.expressionSubstitution!!.right}, " +
                                        "actual in ${fact.expressionSubstitution!!.comparisonType} context" +
                                        "${if (fact.expressionSubstitution!!.basedOnTaskContext) "only in task" else "everywhere"}]"
                            } else ""
                } else {
                    "[${fact.name}:${constructFactUserView(fact.root)}]"
                }
            }
            ComparableTransformationPartType.RULE_POINTER -> {
                fact as RulePointer
                "[${fact.nameLink}:]"
            }
            else -> {
                TODO("unexpected behaviour, should be handled better")
            }
        }
        return result.toString()
    }

    fun Boolean.data() = if (this) "1" else "0"
    fun String.booleanFromData() = (this != "0")

    fun constructIdentifierByFact(fact: MainChainPart): String {
        val result = StringBuilder()
        when (fact.type()) {
            ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                result.append(fact.type().toString() + "{")
                fact as MainLineNode
                result.append("RULES{")
                result.append(fact.rules.joinToString(separator = "") {
                    constructIdentifierByFact(it)
                })
                result.append("}FACT_TRANSFORMATION_CHAINS{")
                result.append(fact.factTransformationChains.joinToString(separator = "") {
                    "{${it.chain.joinToString(separator = "") {
                        "{${constructIdentifierByFact(it)}}"
                    }}}"
                })
                result.append("}EXPRESSION_TRANSFORMATION_CHAINS{")
                result.append(fact.expressionTransformationChains.joinToString(separator = "") {
                    "${it.comparisonType.string}{${it.chain.joinToString(separator = "") {
                        if (it.type() == ComparableTransformationPartType.EXPRESSION) {
                            "{${constructIdentifierByFact(it as Expression)}}"
                        } else if (it.type() == ComparableTransformationPartType.RULE) {
                            "{${constructIdentifierByFact(it as Rule)}}"
                        } else {
                            "{${constructIdentifierByFact(it as RulePointer)}}"
                        }
                    }}}"
                })
                result.append("}IN_FACTS{")
                result.append(fact.inFacts.joinToString(separator = "") {
                    "{${constructIdentifierByFact(it)}}"
                })
                result.append("}OUT_FACTS{")
                result.append(fact.outFacts.joinToString(separator = "") {
                    "{${constructIdentifierByFact(it)}}"
                })
                result.append('}')
                result.append('}')
            }
            ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                result.append(fact.type().toString())
                fact as ExpressionComparison
                result.append("${openBracket}${constructIdentifierByFact(fact.leftExpression)}$closeBracket")
                result.append("${openBracket}${fact.comparisonType.string}$closeBracket")
                result.append("${openBracket}${constructIdentifierByFact(fact.rightExpression)}$closeBracket")
            }
            ComparableTransformationPartType.EXPRESSION -> {
                result.append((fact as Expression).data.toString())
            }
            ComparableTransformationPartType.RULE -> {
                fact as Rule
                result.append("{${fact.name}{${constructIdentifierByFact(fact.root)}}" +
                        "{EXPRESSION_SUBSTITUTION${if (fact.expressionSubstitution != null) {
                            val subst = fact.expressionSubstitution!!
                            "{${subst.left}}{${subst.right}}{${subst.comparisonType.string}}{${subst.basedOnTaskContext.data()}}{${subst.weight}}"
                        } else ""}}" +
                        "{FACT_SUBSTITUTION${if (fact.factSubstitution != null) {
                            val subst = fact.factSubstitution!!
                            "{${constructIdentifierByFact(subst.left)}}{${constructIdentifierByFact(subst.right)}}{${subst.direction}}{${subst.basedOnTaskContext.data()}}{${subst.weight}}"
                        } else ""}}" +
                        "}")
            }
            ComparableTransformationPartType.RULE_POINTER -> {
                fact as RulePointer
                "{${fact.nameLink}:}"
            }
            else -> {
                TODO("unexpected behaviour, should be handled better")
            }
        }
        return result.toString()
    }

    private fun splitStringByBracketsOnTopLevel(identifier: String, startPosition: Int = 0) = splitStringByBracketsOnTopLevel(identifier, openBracket, closeBracket, startPosition)

    fun constructFactByIdentifier(identifier: String, parent: MainLineNode? = null): MainChainPart {
        if (identifier.contains('{')) {
            val identifierData = splitStringByBracketsOnTopLevel(identifier)
            val factType = identifierData.name
            when (factType) {
                ComparableTransformationPartType.MAIN_LINE_AND_NODE.toString(), ComparableTransformationPartType.MAIN_LINE_OR_NODE.toString() -> {
                    val newFact = if (factType == ComparableTransformationPartType.MAIN_LINE_AND_NODE.toString()) {
                        MainLineAndNode(0, 0, parent, identifier = identifier)
                    } else {
                        MainLineOrNode(0, 0, parent, identifier = identifier)
                    }
                    var i = 0
                    val mainNodeData = identifierData.list.first()
                    while (i < mainNodeData.length) {
                        val mainNodePartData = splitStringByBracketsOnTopLevel(mainNodeData, i)
                        val mainLineNodePartName = mainNodePartData.name
                        val mainLineNodePartIdentifier = mainNodePartData.list.first()
                        i = mainNodePartData.endPosition
                        when (mainLineNodePartName.trim()) {
                            "FACT_TRANSFORMATION_CHAINS" -> {
                                val chains = splitStringByBracketsOnTopLevel(mainLineNodePartIdentifier)
                                for (chain in chains.list) {
                                    newFact.factTransformationChains.add(MainChain())
                                    val factIdentifiers = splitStringByBracketsOnTopLevel(chain)
                                    for (factIdentifier in factIdentifiers.list) {
                                        newFact.factTransformationChains.last().chain.add(constructFactByIdentifier(factIdentifier, newFact))
                                    }
                                }
                            }
                            "EXPRESSION_TRANSFORMATION_CHAINS" -> {
                                val chains = splitStringByBracketsOnTopLevel(mainLineNodePartIdentifier)
                                for (chain in chains.list) {
                                    val factIdentifiers = splitStringByBracketsOnTopLevel(chain)
                                    newFact.expressionTransformationChains.add(ExpressionChain(comparisonType = valueOfComparisonType(factIdentifiers.name)))
                                    for (factIdentifier in factIdentifiers.list) {
                                        newFact.expressionTransformationChains.last().chain.add(constructFactByIdentifier(factIdentifier, newFact))
                                    }
                                }
                            }
                            "RULES" -> {
                                val rules = splitStringByBracketsOnTopLevel(mainLineNodePartIdentifier)
                                for (rule in rules.list) {
                                    val ruleIdentifier = splitStringByBracketsOnTopLevel(rule)
                                    val expressionSubstitutionData = splitStringByBracketsOnTopLevel(ruleIdentifier.list[1])
                                    val expressionSubstitution = if (expressionSubstitutionData.list.isEmpty()) null else ExpressionSubstitution(
                                            left = expressionNodeConstructor.construct(expressionSubstitutionData.list[0]),
                                            right = expressionNodeConstructor.construct(expressionSubstitutionData.list[1]),
                                            comparisonType = valueOfComparisonType(expressionSubstitutionData.list[2]),
                                            basedOnTaskContext = expressionSubstitutionData.list[3].booleanFromData(),
                                            weight = expressionSubstitutionData.list[4].toDouble()
                                    )
                                    val factSubstitutionData = splitStringByBracketsOnTopLevel(ruleIdentifier.list[2])
                                    val factSubstitution = if (factSubstitutionData.list.isEmpty()) null else FactSubstitution(
                                            left = constructFactByIdentifier(factSubstitutionData.list[0]),
                                            right = constructFactByIdentifier(factSubstitutionData.list[1]),
                                            direction = SubstitutionDirection.valueOf(factSubstitutionData.list[2]),
                                            basedOnTaskContext = factSubstitutionData.list[3].booleanFromData(),
                                            weight = factSubstitutionData.list[4].toDouble(),
                                            factComporator = compiledConfiguration.factComporator
                                    )
                                    newFact.rules.add(Rule(0, 0, parent = newFact, name = ruleIdentifier.name,
                                            root = constructFactByIdentifier(ruleIdentifier.list[0], newFact) as MainLineAndNode,
                                            expressionSubstitution = expressionSubstitution,
                                            factSubstitution = factSubstitution))
                                }
                            }
                            "IN_FACTS" -> {
                                val factIdentifiers = splitStringByBracketsOnTopLevel(mainLineNodePartIdentifier)
                                for (factIdentifier in factIdentifiers.list) {
                                    newFact.inFacts.add(constructFactByIdentifier(factIdentifier, newFact))
                                }
                            }
                            "OUT_FACTS" -> {
                                val factIdentifiers = splitStringByBracketsOnTopLevel(mainLineNodePartIdentifier)
                                for (factIdentifier in factIdentifiers.list) {
                                    newFact.outFacts.add(constructFactByIdentifier(factIdentifier, newFact))
                                }
                            }
                        }
                    }
                    return newFact
                }
                ComparableTransformationPartType.EXPRESSION_COMPARISON.toString() -> {
                    val newFact = ExpressionComparison(parent = parent, leftExpression = emptyExpression(), rightExpression = emptyExpression(),
                            comparisonType = valueOfComparisonType(identifierData.list[1]))
                    newFact.leftExpression = constructFactByIdentifier(identifierData.list[0], parent) as Expression
                    newFact.rightExpression = constructFactByIdentifier(identifierData.list[2], parent) as Expression
                    return newFact
                }
                else -> {
                    TODO("unexpected behaviour, should be handled better")
                }
            }
        } else {
            return Expression(0, 0, expressionNodeConstructor.construct(identifier), identifier, parent)
        }
    }
}