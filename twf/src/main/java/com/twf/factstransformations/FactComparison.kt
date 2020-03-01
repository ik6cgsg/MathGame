package com.twf.factstransformations

import com.twf.config.CheckingKeyWords.Companion.comparisonWithoutSubstitutions
import com.twf.config.CheckingKeyWords.Companion.taskContextFactUsed
import com.twf.config.CheckingKeyWords.Companion.transformationNotFound
import com.twf.config.CheckingKeyWords.Companion.transformationVerified
import com.twf.config.ComparisonType
import com.twf.config.CompiledConfiguration
import com.twf.expressiontree.ExpressionComporator
import com.twf.expressiontree.ExpressionNode
import com.twf.logs.MessageType
import com.twf.logs.log

class FactComporator(
) {
    lateinit var compiledConfiguration: CompiledConfiguration
    lateinit var expressionComporator: ExpressionComporator

    fun init(compiledConfiguration: CompiledConfiguration, expressionComporator: ExpressionComporator = ExpressionComporator()) {
        this.compiledConfiguration = compiledConfiguration
        this.expressionComporator = expressionComporator
        this.expressionComporator.init(compiledConfiguration)
    }

    fun compareAsIs(left: MainChainPart, right: MainChainPart,
                    additionalFactsSortedIdentifiers: List<String> = listOf(),
                    compareExpressionsWithProbabilityTest: Boolean = false): Boolean { //todo: add additional facts, it can appears in the right part without existing in the left
        log.addMessage({ "compareAsIs started" }, levelChange = 1)
        log.logFactsCompareAsIsParams(left, right, additionalFactsSortedIdentifiers, compareExpressionsWithProbabilityTest)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        if (left.type() != right.type()) {
            return false
        }
        return when (left.type()) {
            ComparableTransformationPartType.EXPRESSION -> {
                log.addMessage({ "Expression pribability comparison" }, level = currentLogLevel)
                expressionComporator.probabilityTestComparison((left as Expression).data.clone(), (right as Expression).data.clone())
            }
//            ComparableTransformationPartType.EXPRESSION_CHAIN -> {
//                val leftChain = (left as ExpressionChain).chain
//                val rightChain = (right as ExpressionChain).chain
//                val leftOriginal = compareAsIs(leftChain.first(), rightChain.first())
//                val rightOriginal = compareAsIs(leftChain.last(), rightChain.last())
//                if (leftOriginal && rightOriginal && left.defaultComparisonType == right.defaultComparisonType){
//                    true
//                } else {
//                    val leftEqualRight = compareAsIs(leftChain.first(), rightChain.last())
//                    val rightEqualLeft = compareAsIs(leftChain.last(), rightChain.first())
//                    if (!leftEqualRight || !rightEqualLeft) false
//                    else when (left.defaultComparisonType){
//                        ComparisonType.EQUAL -> left.defaultComparisonType == ComparisonType.EQUAL
//                        ComparisonType.LEFT_MORE_OR_EQUAL -> left.defaultComparisonType == ComparisonType.LEFT_LESS_OR_EQUAL
//                        ComparisonType.LEFT_LESS_OR_EQUAL -> left.defaultComparisonType == ComparisonType.LEFT_MORE_OR_EQUAL
//                        ComparisonType.LEFT_MORE -> left.defaultComparisonType == ComparisonType.LEFT_LESS
//                        ComparisonType.LEFT_LESS -> left.defaultComparisonType == ComparisonType.LEFT_MORE
//                    }
//                }
//            }
            ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                val leftComparison = left as ExpressionComparison
                val rightComparison = right as ExpressionComparison
                val leftOriginal = compareAsIs(leftComparison.leftExpression, rightComparison.leftExpression, additionalFactsSortedIdentifiers)
                log.add(leftOriginal, { "Result of comparison left.left with right.left: '" }, { "'" }, level = currentLogLevel)
                val rightOriginal = compareAsIs(leftComparison.rightExpression, rightComparison.rightExpression, additionalFactsSortedIdentifiers)
                log.add(rightOriginal, { "Result of comparison left.right with right.right: '" }, { "'" }, level = currentLogLevel)
                if (leftOriginal && rightOriginal && leftComparison.comparisonType == rightComparison.comparisonType) {
                    log.addMessage({ "Expression Comparisons are equal" }, level = currentLogLevel)
                    true
                } else {
                    log.addMessage({ "Simple check failed. Try to reverse comparison" }, level = currentLogLevel)
                    val leftEqualRight = compareAsIs(leftComparison.leftExpression, rightComparison.rightExpression, additionalFactsSortedIdentifiers)
                    log.add(leftEqualRight, { "Result of comparison left.left with right.right: '" }, { "'" }, level = currentLogLevel)
                    val rightEqualLeft = compareAsIs(leftComparison.rightExpression, rightComparison.leftExpression, additionalFactsSortedIdentifiers)
                    log.add(rightEqualLeft, { "Result of comparison left.right with right.left: '" }, { "'" }, level = currentLogLevel)
                    if (!leftEqualRight || !rightEqualLeft) false
                    else when (leftComparison.comparisonType) {
                        ComparisonType.EQUAL -> rightComparison.comparisonType == ComparisonType.EQUAL
                        ComparisonType.LEFT_MORE_OR_EQUAL -> rightComparison.comparisonType == ComparisonType.LEFT_LESS_OR_EQUAL
                        ComparisonType.LEFT_LESS_OR_EQUAL -> rightComparison.comparisonType == ComparisonType.LEFT_MORE_OR_EQUAL
                        ComparisonType.LEFT_MORE -> rightComparison.comparisonType == ComparisonType.LEFT_LESS
                        ComparisonType.LEFT_LESS -> rightComparison.comparisonType == ComparisonType.LEFT_MORE
                    }
                }
            }
            ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                val leftNodeOutFacts = getOutFactsFromMainLineNode(left)
                val rightNodeInFacts = getInFactsFromMainLineNode(right)
                log.logSystemFacts(left.type(), leftNodeOutFacts, { "left" })
                log.logSystemFacts(right.type(), rightNodeInFacts, { "right" })
                var orderIsDifferent = false
                if (leftNodeOutFacts.size == rightNodeInFacts.size) {
                    log.addMessage({ "Comparison in exist order" }, level = currentLogLevel)
                    for (i in 0..leftNodeOutFacts.lastIndex) {
                        if (!compareAsIs(leftNodeOutFacts[i], rightNodeInFacts[i], additionalFactsSortedIdentifiers)) {
                            orderIsDifferent = true
                            log.add(i, { "facts at position '" }, { "' are not equal. Order may be different" }, level = currentLogLevel)
                            break
                        }
                    }
                }
                if (!orderIsDifferent && leftNodeOutFacts.size == rightNodeInFacts.size) {
                    true
                } else {
                    log.addMessage({ "Comparison in sorted order" })
                    val leftNodeOutFactsSorted = leftNodeOutFacts.sortedBy { it.computeSortedOutIdentifier(true) }
                    val rightNodeOutFactsSorted = rightNodeInFacts.sortedBy { it.computeSortedOutIdentifier(true) }
                    log.logSystemFacts(left.type(), leftNodeOutFactsSorted, { "sorted left" })
                    log.logSystemFacts(right.type(), rightNodeOutFactsSorted, { "sorted right" })
                    var discrepancyFound = false
                    var leftCounter = 0
                    var rightCounter = 0
                    while (leftCounter < leftNodeOutFactsSorted.size && rightCounter < rightNodeOutFactsSorted.size) {
                        val leftIdentifier = leftNodeOutFactsSorted[leftCounter].computeSortedOutIdentifier(true)
                        val rightIdentifier = rightNodeOutFactsSorted[rightCounter].computeSortedOutIdentifier(true)
                        if (leftIdentifier == rightIdentifier) {
                            leftCounter++
                            rightCounter++
                        } else if (left.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE) {
                            if (leftIdentifier < rightIdentifier) {
                                if (additionalFactsSortedIdentifiers.binarySearch(leftIdentifier) >= 0) {
                                    log.add(leftCounter, rightCounter, { "Additional fact used to match left fact at left: '" }, { "' and right '" }, { "'" }, level = currentLogLevel)
                                    leftCounter++
                                } else {
                                    discrepancyFound = true
                                    log.add(leftCounter, rightCounter, { "DiscrepancyFound at left: '" }, { "' and right '" }, { "'" }, level = currentLogLevel)
                                    break
                                }
                            } else if (leftIdentifier > rightIdentifier) {
                                if (additionalFactsSortedIdentifiers.binarySearch(rightIdentifier) >= 0) {
                                    log.add(leftCounter, rightCounter, { "Additional fact used to match right fact at left: '" }, { "' and right '" }, { "'" }, level = currentLogLevel)
                                    rightCounter++
                                } else {
                                    log.add(leftCounter, rightCounter, { "DiscrepancyFound at left: '" }, { "' and right '" }, { "'" }, level = currentLogLevel)
                                    discrepancyFound = true
                                    break
                                }
                            }
                        } else {
                            log.add(leftCounter, rightCounter, { "DiscrepancyFound at left: '" }, { "' and right '" }, { "' in not MAIN_LINE_AND_NODE" }, level = currentLogLevel)
                            discrepancyFound = true
                            break
                        }
                    }
                    if (left.type() != ComparableTransformationPartType.MAIN_LINE_AND_NODE &&
                            (leftCounter < leftNodeOutFactsSorted.size || rightCounter < rightNodeOutFactsSorted.size)) {
                        log.add(leftCounter, rightCounter, { "DiscrepancyFound at left: '" }, { "' and right '" }, { "'" }, level = currentLogLevel)
                        discrepancyFound = true
                    }
                    while (!discrepancyFound && leftCounter < leftNodeOutFactsSorted.size) {
                        if (additionalFactsSortedIdentifiers.binarySearch(leftNodeOutFactsSorted[leftCounter].computeSortedOutIdentifier(true)) >= 0) {
                            leftCounter++
                        } else {
                            log.add(leftCounter, rightCounter, { "DiscrepancyFound at left: '" }, { "' and right '" }, { "' during search in left facts" }, level = currentLogLevel)
                            discrepancyFound = true
                            break
                        }
                    }
                    while (!discrepancyFound && rightCounter < rightNodeOutFactsSorted.size) {
                        if (additionalFactsSortedIdentifiers.binarySearch(rightNodeOutFactsSorted[rightCounter].computeSortedOutIdentifier(true)) >= 0) {
                            rightCounter++
                        } else {
                            log.add(leftCounter, rightCounter, { "DiscrepancyFound at left: '" }, { "' and right '" }, { "' during search in right facts" }, level = currentLogLevel)
                            discrepancyFound = true
                            break
                        }
                    }
                    log.add(discrepancyFound, { "result: '" }, { "'" }, level = currentLogLevel)
                    !discrepancyFound
                }
            }
            else -> true //it's needn't to compare RULEs and RULE_POINTERs: both of them should be derived or declared once
        }
    }

    fun compareWithoutSubstitutions(left: MainChainPart, right: MainChainPart, additionalFacts: List<MainChainPart>,
                                    additionalFactsSortedIdentifiers: List<String> = additionalFacts.map { it.computeOutIdentifier(true) }.sorted()): Boolean {
        log.addMessage({ "compareWithoutSubstitutions started" }, levelChange = 1)
        log.logFactsCompareAsIsParams(left, right, additionalFactsSortedIdentifiers)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        val l = left.cloneWithNormalization(mutableMapOf(), sorted = true)
        val r = right.cloneWithNormalization(mutableMapOf(), sorted = true)
        log.addMessageWithFactShort({ "Left fact after normalization: " }, l, level = currentLogLevel)
        log.addMessageWithFactShort({ "Right fact after normalization: " }, r, level = currentLogLevel)
        if (compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules) {
            TODO("implement")
//            val functionIdentifierToVariableMap = mutableMapOf<ExpressionNode, String>()
//            l.replaceNotDefinedFunctionsOnVariables (functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet)
//            r.replaceNotDefinedFunctionsOnVariables (functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet)
//            changeMessageLevel(MessageLevelChange.TO_PARENT_LEVEL)
//            return probabilityTestComprarison(l, r)
        } else if (compiledConfiguration.comparisonSettings.compareExpressionsWithProbabilityRulesWhenComparingFacts) {
            val functionIdentifierToVariableMap = mutableMapOf<ExpressionNode, String>()
            l.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, expressionComporator.definedFunctionNameNumberOfArgsSet)
            r.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, expressionComporator.definedFunctionNameNumberOfArgsSet)
            log.addMessageWithFactShort({ "Left fact after complicated function replacement: " }, l, level = currentLogLevel)
            log.addMessageWithFactShort({ "Right fact after complicated function replacement: " }, r, level = currentLogLevel)
            if (compareAsIs(l, r, additionalFactsSortedIdentifiers, true)) {
                return true
            }
            l.computeExpressionTrees(expressionComporator.baseOperationsDefinitions)
            r.computeExpressionTrees(expressionComporator.baseOperationsDefinitions)
            log.addMessage({ "Expression numeric trees computed. " }, level = currentLogLevel)
            val res = compareAsIs(l, r, additionalFactsSortedIdentifiers, true)
            return res
        } else {
            val res = compareAsIs(l, r, additionalFactsSortedIdentifiers)
            return res
        }
    }

    //use function compareWithTreeTransformationRules as api if user specify additional rules - user thought that system should check rules
    fun compareWithTreeTransformationRules(leftOriginal: MainChainPart, rightOriginal: MainChainPart, additionalFacts: List<MainChainPart>,
                                           transformations: Collection<FactSubstitution>,
                                           maxTransformationWeight: Double = compiledConfiguration.comparisonSettings.maxTransformationWeight,
                                           maxBustCount: Int = compiledConfiguration.comparisonSettings.maxBustCount,
                                           minPossibleTransformationWeight: Double = transformations.minBy { it.weight }?.weight
                                                   ?: 1.0,
                                           additionalFactsSortedIdentifiers: List<String> = additionalFacts.map { it.computeOutIdentifier(true) }.sorted(),
                                           additionalFactUsed: MutableList<Boolean>): Boolean {
        val left = leftOriginal.clone()
        val right = rightOriginal.clone()
        log.addMessage({ "compareWithTreeTransformationRules called" }, MessageType.USER, levelChange = 1)
        log.logFactsCompareAsIsParams(left, right, additionalFacts, maxTransformationWeight, maxBustCount,
                minPossibleTransformationWeight, additionalFactUsed)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        left.applyAllExpressionSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        right.applyAllExpressionSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        log.addMessageWithFactShort({ "Left fact after first normalization: " }, left, level = currentLogLevel)
        log.addMessageWithFactShort({ "Right fact after first normalization: " }, right, level = currentLogLevel)
        if (compareWithoutSubstitutions(left, right, additionalFacts, additionalFactsSortedIdentifiers)) return true
        log.addMessage({ "$comparisonWithoutSubstitutions FAILED; Transformation rules search started." }, MessageType.USER, level = currentLogLevel)
        if (maxTransformationWeight < minPossibleTransformationWeight) return false
        for (transformation in transformations.filter { it.weight <= maxTransformationWeight }) {
            log.addMessageWithFactSubstitutionDetail({ "Considering rule: " }, transformation, MessageType.USER, level = currentLogLevel)
            val transformationLogLevel = currentLogLevel + 1
            val l = left.clone()
            val r = right.clone()
            val lParent = factWrapperForCheckingTransformations(l, true)//need parent because findAllPossibleSubstitutionPlaces returns pointers on changes
            val rParent = factWrapperForCheckingTransformations(r, false)//and changes can be apply to current root ('l' or 'r')

            log.addMessageWithFactShort({ "Left fact after fact wrapping: " }, lParent, level = transformationLogLevel)
            log.addMessageWithFactShort({ "Right fact after fact wrapping: " }, rParent, level = transformationLogLevel)

            val substitutionPlaces = transformation.findAllPossibleSubstitutionPlaces(lParent, checkOutMainLineNodePart = true, additionalFacts = additionalFacts) +
                    transformation.findAllPossibleSubstitutionPlaces(rParent, checkOutMainLineNodePart = false, additionalFacts = additionalFacts)
            log.add(substitutionPlaces.size, { "" }, { " possible applications found" })
            val bitMaskCount = 1 shl substitutionPlaces.size
            log.add(substitutionPlaces.size, bitMaskCount, { "" }, { " possible applications found => " }, { " possible combinations" },
                    messageType = MessageType.USER, level = transformationLogLevel)
            if (bitMaskCount * transformations.size > maxBustCount) {
                log.add(maxBustCount / transformations.size, { "Possible application places count more than max count for boosting: '" },
                        { "'. Apply in all of them together" }, messageType = MessageType.USER, level = transformationLogLevel)
                val additionalFactInCurrentTransformationApplicationUsed = mutableListOf<Boolean>()
                transformation.applySubstitution(substitutionPlaces, additionalFacts, additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)
                log.addMessageWithFactDetail({ "Left fact after transformation: " }, lParent, MessageType.USER, level = transformationLogLevel)
                log.addMessageWithFactDetail({ "Right fact after transformation: " }, rParent, MessageType.USER, level = transformationLogLevel)
                if (compareWithTreeTransformationRules(lParent, rParent, additionalFacts,
                                transformations, maxTransformationWeight - transformation.weight, maxBustCount, minPossibleTransformationWeight,
                                additionalFactsSortedIdentifiers, additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)) {
                    log.addMessage({ "$transformationVerified. " }, MessageType.USER, level = transformationLogLevel)
                    if (additionalFactInCurrentTransformationApplicationUsed.isNotEmpty()) {
                        additionalFactUsed.add(true)
                        log.addMessage({ "$taskContextFactUsed. " }, MessageType.USER, level = transformationLogLevel)
                    }
                    return true
                }
            } else {
                for (bitMask in 1 until bitMaskCount) {
                    log.add(bitMask.toString(2), bitMaskCount.toString(2), { "bitmask: '" }, { "' of '" }, { "'" },
                            messageType = MessageType.USER, level = transformationLogLevel)
                    val additionalFactInCurrentTransformationApplicationUsed = mutableListOf<Boolean>()
                    transformation.applySubstitutionByBitMask(substitutionPlaces, bitMask, additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)
                    log.addMessageWithFactDetail({ "Left fact after transformation: " }, lParent, MessageType.USER, level = transformationLogLevel)
                    log.addMessageWithFactDetail({ "Right fact after transformation: " }, rParent, MessageType.USER, level = transformationLogLevel)
                    if (compareWithTreeTransformationRules(
                                    lParent.cloneWithNormalization(mutableMapOf(), sorted = false),
                                    rParent.cloneWithNormalization(mutableMapOf(), sorted = false), additionalFacts, //can be used just clone()
                                    transformations, maxTransformationWeight - transformation.weight, maxBustCount, minPossibleTransformationWeight,
                                    additionalFactsSortedIdentifiers, additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)) {
                        log.addMessage({ "$transformationVerified. " }, MessageType.USER, level = transformationLogLevel)
                        if (additionalFactInCurrentTransformationApplicationUsed.isNotEmpty()) {
                            additionalFactUsed.add(true)
                            log.addMessage({ "$taskContextFactUsed. " }, MessageType.USER, level = transformationLogLevel)
                        }
                        return true
                    }
                }
            }
        }
        log.addMessage({ "applicable $transformationNotFound" }, MessageType.USER, level = currentLogLevel)
        return false
    }

    //use this function if there are no user rules; now it used only in tests
    //todo: resolve question about base facts: why we trying to compute expression perts here, but not in 'compareWithTreeTransformationRules'
    fun fullFactsCompare(left: MainChainPart, right: MainChainPart, additionalFacts: List<MainChainPart>, additionalFactUsed: MutableList<Boolean>): Boolean {
        left.applyAllExpressionSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        right.applyAllExpressionSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        if (compiledConfiguration.comparisonSettings.isComparisonWithRules) {
            val additionalFactInCurrentTransformationApplicationUsed = mutableListOf<Boolean>()
            if (compareWithTreeTransformationRules(left, right, additionalFacts, compiledConfiguration.compiledFactTreeTransformationRules,
                            compiledConfiguration.comparisonSettings.maxTransformationWeight, compiledConfiguration.comparisonSettings.maxBustCount,
                            additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)) {
                if (additionalFactInCurrentTransformationApplicationUsed.isNotEmpty()) {
                    additionalFactUsed.add(true)
                }
                return true
            }
            additionalFactInCurrentTransformationApplicationUsed.clear()
            left.computeExpressionTrees(expressionComporator.baseOperationsDefinitions)
            right.computeExpressionTrees(expressionComporator.baseOperationsDefinitions)

            if (compareWithTreeTransformationRules(left, right, additionalFacts, compiledConfiguration.compiledFactTreeTransformationRules,
                            compiledConfiguration.comparisonSettings.maxTransformationWeight, compiledConfiguration.comparisonSettings.maxBustCount,
                            additionalFactUsed = additionalFactInCurrentTransformationApplicationUsed)) {
                if (additionalFactInCurrentTransformationApplicationUsed.isNotEmpty()) {
                    additionalFactUsed.add(true)
                }
                return true
            }
        } else {
            if (compareWithoutSubstitutions(left, right, additionalFacts)) {
                return true
            }
        }
        return false
    }
}