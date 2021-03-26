package factstransformations

import config.ComparisonType
import expressiontree.*
import logs.log

enum class SubstitutionDirection(val beginString: String, val endString: String) { LEFT_TO_RIGHT("-", "->"), RIGHT_TO_LEFT("<-", "-"), ALL_TO_ALL("<-", "->") }

fun SubstitutionDirection.toUserString() = beginString + endString

data class FactSubstitutionPlace(
        val parentChain: MutableList<MainChainPart>,
        val replaceableNodeIndex: Int,
        val substitutionInstance: SubstitutionInstance,
        val checkOutMainLineNodePart: Boolean,
        val originalValue: MainChainPart
)

class FactSubstitution( //try to make substitution only on top level, not on ExpressionNode
        val left: MainChainPart, //based on out facts in MainLineNodes
        val right: MainChainPart, //based on in facts in MainLineNodes
        val weight: Double = 1.0,
        val basedOnTaskContext: Boolean = false,
        val direction: SubstitutionDirection = SubstitutionDirection.ALL_TO_ALL, //todo: support
        val name: String = "",
        val factComporator: FactComporator
) {
    var identifier = ""
    fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank()) {
            identifier = "[${left.computeIdentifier(recomputeIfComputed)}${direction.beginString}${if (basedOnTaskContext) "InTaskContext" else ""}${direction.endString}${right.computeIdentifier(recomputeIfComputed)}]"
        }
        return identifier
    }

    fun checkCondition(factNode: ComparableTransformationsPart, inputConditionNode: ComparableTransformationsPart,
                       substitutionInstance: SubstitutionInstance,
                       nameArgsMap: MutableMap<String, String> = mutableMapOf(),
                       checkOutMainLineNodePart: Boolean, //if we try to apply substitution to left part - we try to replace out facts, otherwise: in. if conditionNode we always use out facts
                       additionalFacts: List<MainChainPart> = listOf(),
                       correspondingIndexes: MutableList<MatchedNode> = mutableListOf()) {
        var conditionNode = inputConditionNode
        if (inputConditionNode.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE ||
                inputConditionNode.type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
            if ((inputConditionNode as MainLineNode).outFacts.size == 1 &&
                    factNode.type() != ComparableTransformationPartType.MAIN_LINE_AND_NODE &&
                    factNode.type() != ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
                conditionNode = inputConditionNode.outFacts.first()
            }
        } else if (factNode.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE ||
                factNode.type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
            val actualFacts = if (checkOutMainLineNodePart) getOutFactsFromMainLineNode(factNode)
            else getInFactsFromMainLineNode(factNode)
            if (actualFacts.size == 1) {
                conditionNode = when (factNode.type()) {
                    ComparableTransformationPartType.MAIN_LINE_AND_NODE -> MainLineAndNode(outFacts = mutableListOf(inputConditionNode as MainChainPart))
                    ComparableTransformationPartType.MAIN_LINE_OR_NODE -> MainLineOrNode(outFacts = mutableListOf(inputConditionNode as MainChainPart))
                    else -> MainLineAndNode()
                }
            }
        }
        if (conditionNode.type() == ComparableTransformationPartType.EXPRESSION) {
            conditionNode as Expression
            if (basedOnTaskContext) {
                if (factNode.type() != ComparableTransformationPartType.EXPRESSION ||
                        !(conditionNode.data.isNodeSubtreeEquals((factNode as Expression).data))) {
                    substitutionInstance.isApplicable = false
                    return
                }
            } else {
                val designationName = conditionNode.getDesignation()
                if (designationName.isNotEmpty()) {
                    val varValue = substitutionInstance.getComparableVar(designationName)
                    if (varValue == null) {
                        if (substitutionInstance.getExprVar(designationName) == null) {
                            substitutionInstance.putComparableVar(designationName, factNode)
                        } else {
                            substitutionInstance.isApplicable = false
                            return
                        }
                    } else {
                        if (!factComporator.compareAsIs(varValue as MainChainPart, factNode as MainChainPart, listOf())) {
                            substitutionInstance.isApplicable = false
                            return
                        }
                    }
                } else {
                    if (conditionNode.type() != factNode.type()) {
                        substitutionInstance.isApplicable = false
                        return
                    }
                    ExpressionSubstitution.checkConditionCompanion((factNode as Expression).data, (conditionNode as Expression).data,
                            substitutionInstance, nameArgsMap, basedOnTaskContext)
                    if (!substitutionInstance.isApplicable) return
                }
            }
        } else {
            if (conditionNode.type() != factNode.type()) { //todo: handle situation when types are different only because of wrapper
                substitutionInstance.isApplicable = false
                return
            }
            when (conditionNode.type()) {
                ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                    var isApplicable = false
                    val currentTime = substitutionInstance.varNamesTimeStorage.time
                    checkCondition((factNode as ExpressionComparison).leftExpression, (conditionNode as ExpressionComparison).leftExpression, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                    if (substitutionInstance.isApplicable && factNode.comparisonType == conditionNode.comparisonType) {
                        checkCondition((factNode).rightExpression, (conditionNode).rightExpression, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                        if (substitutionInstance.isApplicable) {
                            isApplicable = true
                        }
                    }
                    if (!isApplicable) {
                        substitutionInstance.isApplicable = false
                        when (conditionNode.comparisonType) {
                            ComparisonType.EQUAL -> if (factNode.comparisonType != ComparisonType.EQUAL) return
                            ComparisonType.LEFT_MORE_OR_EQUAL -> if (factNode.comparisonType != ComparisonType.LEFT_LESS_OR_EQUAL) return
                            ComparisonType.LEFT_LESS_OR_EQUAL -> if (factNode.comparisonType != ComparisonType.LEFT_MORE_OR_EQUAL) return
                            ComparisonType.LEFT_MORE -> if (factNode.comparisonType != ComparisonType.LEFT_LESS) return
                            ComparisonType.LEFT_LESS -> if (factNode.comparisonType != ComparisonType.LEFT_MORE) return
                        }
                        substitutionInstance.dropExtraVarsAfter(currentTime)
                        substitutionInstance.isApplicable = true
                        checkCondition((factNode).leftExpression, (conditionNode).rightExpression, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                        if (substitutionInstance.isApplicable) {
                            checkCondition((factNode).rightExpression, (conditionNode).leftExpression, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                            if (substitutionInstance.isApplicable) {
                                isApplicable = true
                            }
                        }
                        if (!isApplicable) {
                            substitutionInstance.isApplicable = false
                            return
                        }
                    }
                }
                ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                    val actualFacts = if (checkOutMainLineNodePart) getOutFactsFromMainLineNode(factNode)
                    else getInFactsFromMainLineNode(factNode)
                    val conditionFacts = getOutFactsFromMainLineNode(conditionNode)
                    val nodesCorrespondingToConditionFacts = Array<MutableSet<Int>>(conditionFacts.size, { mutableSetOf() })
                    val notMatchedConditionFactsIndexes = mutableSetOf<Int>()
                    for (j in 0..conditionFacts.lastIndex) {
                        val conditionFact = conditionFacts[j]
                        for (i in 0..actualFacts.lastIndex) {
                            val actualFact = actualFacts[i]
                            val startFactCheckingTime = substitutionInstance.varNamesTimeStorage.time
                            substitutionInstance.isApplicable = true
                            checkCondition(actualFact, conditionFact, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                            if (substitutionInstance.isApplicable) {
                                nodesCorrespondingToConditionFacts[j].add(i)
                            }
                            substitutionInstance.dropExtraVarsAfter(startFactCheckingTime)
                        }
                        for (i in 0..additionalFacts.lastIndex) {
                            val actualFact = additionalFacts[i]
                            val startFactCheckingTime = substitutionInstance.varNamesTimeStorage.time
                            substitutionInstance.isApplicable = true
                            checkCondition(actualFact, conditionFact, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                            if (substitutionInstance.isApplicable) {
                                nodesCorrespondingToConditionFacts[j].add(actualFacts.size + i)
                            }
                            substitutionInstance.dropExtraVarsAfter(startFactCheckingTime)
                        }
                        if (nodesCorrespondingToConditionFacts[j].isEmpty()) {
                            notMatchedConditionFactsIndexes.add(j)
                        }
                    }
                    if (nodesCorrespondingToConditionFacts.all { it.size <= 1 }) {
                        val startGeneratingResultTime = substitutionInstance.varNamesTimeStorage.time
                        for (j in 0..conditionFacts.lastIndex) {
                            if (j in notMatchedConditionFactsIndexes) {
                                continue
                            }
                            val conditionFact = conditionFacts[j]
                            val matchingFactIndex = nodesCorrespondingToConditionFacts[j].first()
                            val actualFact = if (matchingFactIndex >= actualFacts.size) {
                                substitutionInstance.varNamesTimeStorage.addVarName(additionalFactUsedVarName, SubstitutionInstanceVarType.INFO)
                                additionalFacts[matchingFactIndex - actualFacts.size]
                            } else {
                                actualFacts[matchingFactIndex]
                            }
                            substitutionInstance.isApplicable = true
                            val currentStartGeneratingResultTime = substitutionInstance.varNamesTimeStorage.time
                            checkCondition(actualFact, conditionFact, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                            if (!substitutionInstance.isApplicable) {
                                substitutionInstance.dropExtraVarsAfter(currentStartGeneratingResultTime)
                                notMatchedConditionFactsIndexes.add(j)
                            }
                            correspondingIndexes.add(MatchedNode(matchingFactIndex, actualFact.type()))
                        }
                        for (j in notMatchedConditionFactsIndexes) {
                            val conditionFact = conditionFacts[j]
                            val isNumericAndCorrect = when (conditionFact.type()) {
                                ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                                    (conditionFact as ExpressionComparison).computeIfNumeric(substitutionInstance, factComporator.expressionComporator.baseOperationsDefinitions)
                                }
                                ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                                    (conditionFact as MainLineNode).computeIfNumeric(substitutionInstance, factComporator.expressionComporator.baseOperationsDefinitions, true)
                                }
                                else -> false
                            }
                            if (isNumericAndCorrect != true) {
                                substitutionInstance.isApplicable = false
                                substitutionInstance.dropExtraVarsAfter(startGeneratingResultTime)
                                return
                            }
                        }
                        substitutionInstance.isApplicable = true
                    } else {
                        //KNF SAT problem: more than NPC task. todo: rewrite code bellow
                        var j = 0
                        val startGeneratingResultTime = substitutionInstance.varNamesTimeStorage.time
                        while (j < conditionFacts.size) {
                            val conditionFact = conditionFacts[j]
                            if (nodesCorrespondingToConditionFacts[j].isEmpty()) {
                                substitutionInstance.isApplicable = false
                                substitutionInstance.dropExtraVarsAfter(startGeneratingResultTime)
                                return
                            }
                            val matchingFactIndex = nodesCorrespondingToConditionFacts[j].first()
                            val actualFact = if (matchingFactIndex >= actualFacts.size) {
                                substitutionInstance.varNamesTimeStorage.addVarName(additionalFactUsedVarName, SubstitutionInstanceVarType.INFO)
                                additionalFacts[matchingFactIndex - actualFacts.size]
                            } else {
                                actualFacts[matchingFactIndex]
                            }
                            substitutionInstance.isApplicable = true
                            val startFactCheckingTime = substitutionInstance.varNamesTimeStorage.time
                            checkCondition(actualFact, conditionFact, substitutionInstance, nameArgsMap, checkOutMainLineNodePart)
                            if (!substitutionInstance.isApplicable) {
                                substitutionInstance.dropExtraVarsAfter(startFactCheckingTime)
                                nodesCorrespondingToConditionFacts[j].remove(matchingFactIndex)
                                continue
                            }
                            nodesCorrespondingToConditionFacts.forEach { it.remove(matchingFactIndex) }
                            correspondingIndexes.add(MatchedNode(matchingFactIndex, actualFact.type()))
                            j++
                        }
                    }
                }
            }
        }
    }

    fun checkLeftCondition(factNode: ComparableTransformationsPart,
                           checkOutMainLineNodePart: Boolean,
                           additionalFacts: List<MainChainPart>): SubstitutionInstance {
        val substitutionInstance = SubstitutionInstance()
        checkCondition(factNode, left, substitutionInstance, checkOutMainLineNodePart = checkOutMainLineNodePart,
                additionalFacts = additionalFacts, correspondingIndexes = substitutionInstance.correspondingIndexes.children)
        substitutionInstance.correspondingIndexes.nodeType = factNode.type()
        substitutionInstance.correspondingIndexes.matchedFactIndex = 0
        return substitutionInstance
    }

    private fun findAllPossibleSubstitutionPlaces(root: MainChainPart,
                                                  checkOutMainLineNodePart: Boolean,
                                                  additionalFacts: List<MainChainPart>,
                                                  result: MutableList<FactSubstitutionPlace>) {
        log.addMessageWithFactShort({ "findAllPossibleSubstitutionPlaces in fact: " }, root, levelChange = 1)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        if (root.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE || root.type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
            val actualFacts = if (checkOutMainLineNodePart) getOutFactsFromMainLineNode(root)
            else getInFactsFromMainLineNode(root)
            log.logSystemFacts(root.type(), actualFacts, { "actualFacts" })
            for (i in 0 until actualFacts.size) {
                findAllPossibleSubstitutionPlaces(actualFacts[i], checkOutMainLineNodePart, additionalFacts, result)
                val substitutionInstance = checkLeftCondition(actualFacts[i], checkOutMainLineNodePart, additionalFacts)
                if (substitutionInstance.isApplicable) {
                    substitutionInstance.logValue({ "Applicable substitution found " }, level = currentLogLevel)
                    result.add(FactSubstitutionPlace(actualFacts, i, substitutionInstance, checkOutMainLineNodePart, actualFacts[i]))
                }
            }
        }
    }

    fun findAllPossibleSubstitutionPlaces(root: MainChainPart,
                                          checkOutMainLineNodePart: Boolean,
                                          additionalFacts: List<MainChainPart>): MutableList<FactSubstitutionPlace> {
        val result = mutableListOf<FactSubstitutionPlace>()
        log.addMessage({ "search places for transformation" })
        findAllPossibleSubstitutionPlaces(root, checkOutMainLineNodePart, additionalFacts, result)
        return result
    }

    fun applySubstitution(substitutionPlaces: List<FactSubstitutionPlace>, additionalFacts: List<MainChainPart>, additionalFactUsed: MutableList<Boolean>) {
        for (substitutionPlace in substitutionPlaces) {
            val newValue = checkAndApply(substitutionPlace.parentChain[substitutionPlace.replaceableNodeIndex],
                    substitutionPlace.checkOutMainLineNodePart, additionalFacts)
            if (newValue != null) {
                substitutionPlace.parentChain[substitutionPlace.replaceableNodeIndex] = newValue
                if (substitutionPlace.substitutionInstance.varNamesTimeStorage.varsList.filter { it.type == SubstitutionInstanceVarType.INFO && it.name == additionalFactUsedVarName }.isNotEmpty() && additionalFactUsed.isEmpty()) {
                    additionalFactUsed.add(true)
                }
            }
        }
    }

    fun applySubstitutionByBitMask(substitutionPlaces: List<FactSubstitutionPlace>, bitMask: Int, additionalFactUsed: MutableList<Boolean>) {
        for (i in 0 until substitutionPlaces.size) {
            val substitutionPlace = substitutionPlaces[i]
            substitutionPlace.parentChain[substitutionPlace.replaceableNodeIndex] = substitutionPlace.originalValue //roll back previous substitutions
        }
        for (i in 0 until substitutionPlaces.size) {
            if (bitMask and (1 shl i) == 0) continue
            val substitutionPlace = substitutionPlaces[i]
            val newValue = applyRight(substitutionPlace.substitutionInstance, !substitutionPlace.checkOutMainLineNodePart, right,
                    substitutionPlace.parentChain[substitutionPlace.replaceableNodeIndex])
            if (newValue != null) {
                substitutionPlace.parentChain[substitutionPlace.replaceableNodeIndex] = newValue //to save. to test such case for expressions - todo
                if (substitutionPlace.substitutionInstance.varNamesTimeStorage.varsList.filter { it.type == SubstitutionInstanceVarType.INFO && it.name == additionalFactUsedVarName }.isNotEmpty() && additionalFactUsed.isEmpty()) {
                    additionalFactUsed.add(true)
                }
            }
        }
    }

    fun applyRight(substitutionInstance: SubstitutionInstance, checkOutMainLineNodePart: Boolean, right: MainChainPart,
                   factNode: MainChainPart? = null): MainChainPart? {
        if (right.type() == ComparableTransformationPartType.MAIN_LINE_AND_NODE || right.type() == ComparableTransformationPartType.MAIN_LINE_OR_NODE) {
            val result = right.copyNode()
            val resultFacts = if (!checkOutMainLineNodePart) getOutFactsFromMainLineNode(result)
            else getInFactsFromMainLineNode(result)
            val rightFacts = getInFactsFromMainLineNode(right)
            for (fact in rightFacts) {
                val child = when (fact.type()) {
                    ComparableTransformationPartType.MAIN_LINE_AND_NODE, ComparableTransformationPartType.MAIN_LINE_OR_NODE -> {
                        applyRight(substitutionInstance, checkOutMainLineNodePart, fact)
                    }
                    ComparableTransformationPartType.EXPRESSION_COMPARISON -> {
                        val res = fact.copyNode() as ExpressionComparison
                        val leftDesignationName = (fact as ExpressionComparison).leftExpression.getDesignation()
                        val rightDesignationName = (fact).rightExpression.getDesignation()
                        res.leftExpression = if (leftDesignationName.isBlank() || substitutionInstance.getComparableVar(leftDesignationName) == null) {
                            Expression(data = ExpressionSubstitution.applyRightCompanion(substitutionInstance, fact.leftExpression.data)
                                    ?: return null)
                        } else {
                            (substitutionInstance.getComparableVar(leftDesignationName)
                                    ?: return null).cloneWithNormalization(mutableMapOf(), sorted = false) as Expression
                        }
                        res.rightExpression = if (rightDesignationName.isBlank() || substitutionInstance.getComparableVar(rightDesignationName) == null) {
                            Expression(data = ExpressionSubstitution.applyRightCompanion(substitutionInstance, fact.rightExpression.data)
                                    ?: return null)
                        } else {
                            (substitutionInstance.getComparableVar(rightDesignationName)
                                    ?: return null).cloneWithNormalization(mutableMapOf(), sorted = false) as Expression
                        }
                        res.identifier = res.computeIdentifier(false)
                        res
                    }
                    ComparableTransformationPartType.EXPRESSION -> {
                        val designationName = (fact as Expression).getDesignation()
                        if (designationName.isBlank()) return null
                        (substitutionInstance.getComparableVar(designationName)
                                ?: return null).cloneWithNormalization(mutableMapOf(), sorted = false) as MainChainPart
                    }
                    else -> null
                }
                resultFacts.add(child ?: return null)
            }
            if (factNode != null && right.type() == substitutionInstance.correspondingIndexes.nodeType) {
                val actualFacts = if (checkOutMainLineNodePart) getOutFactsFromMainLineNode(factNode)
                else getInFactsFromMainLineNode(factNode)
                val correspondingIndexesSet = substitutionInstance.correspondingIndexes.children.map { it.matchedFactIndex }.toSet()
                for (i in 0 until actualFacts.size) {
                    if (!correspondingIndexesSet.contains(i)) {
                        resultFacts.add(actualFacts[i])
                    }
                }
            }
            return result as MainChainPart
        } else if (right.type() == ComparableTransformationPartType.EXPRESSION_COMPARISON) {
            val res = right.copyNode() as ExpressionComparison
            val leftDesignationName = (right as ExpressionComparison).leftExpression.getDesignation()
            val rightDesignationName = (right as ExpressionComparison).rightExpression.getDesignation()
            res.leftExpression = if (leftDesignationName.isBlank() || substitutionInstance.getComparableVar(leftDesignationName) == null) {
                Expression(data = ExpressionSubstitution.applyRightCompanion(substitutionInstance, right.leftExpression.data)
                        ?: return null)
            } else {
                (substitutionInstance.getComparableVar(leftDesignationName)
                        ?: return null).cloneWithNormalization(mutableMapOf(), sorted = false) as Expression
            }
            res.rightExpression = if (rightDesignationName.isBlank() || substitutionInstance.getComparableVar(rightDesignationName) == null) {
                Expression(data = ExpressionSubstitution.applyRightCompanion(substitutionInstance, right.rightExpression.data)
                        ?: return null)
            } else {
                (substitutionInstance.getComparableVar(rightDesignationName)
                        ?: return null).cloneWithNormalization(mutableMapOf(), sorted = false) as Expression
            }
            res.identifier = res.computeIdentifier(false)
            return res
        }
        return null
    }

    fun checkAndApply(factNode: MainChainPart, checkOutMainLineNodePart: Boolean = true, additionalFacts: List<MainChainPart> = listOf()): MainChainPart? {
        val substitutionInstance = checkLeftCondition(factNode, checkOutMainLineNodePart, additionalFacts)
        if (substitutionInstance.isApplicable)
            return applyRight(substitutionInstance, checkOutMainLineNodePart, right, factNode)
        else
            return null
    }
}

fun emptyFactSubstitution() = FactSubstitution(emptyExpression(), emptyExpression(), factComporator = FactComporator(), name = "")