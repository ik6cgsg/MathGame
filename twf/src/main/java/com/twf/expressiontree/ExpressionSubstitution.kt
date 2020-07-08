package com.twf.expressiontree

import com.twf.config.ComparisonType
import com.twf.config.CompiledConfiguration
import com.twf.factstransformations.ComparableTransformationPartType
import com.twf.factstransformations.ComparableTransformationsPart
import com.twf.factstransformations.Expression
import com.twf.logs.MessageType
import com.twf.logs.log
import com.twf.standartlibextensions.abs
import com.twf.standartlibextensions.isNumberValuesEqual

val additionalFactUsedVarName = "AFU"

enum class SubstitutionInstanceVarType { EXPR_VAR, EXPR_FUNC, COMPARABLE_VAR, INFO }

data class SubstitutionInstanceVar(val name: String, val type: SubstitutionInstanceVarType, val time: Int)

class VarNamesTimeStorage(var time: Int = 0) {
    var varsList = mutableListOf<SubstitutionInstanceVar>()

    fun addVarName(name: String, type: SubstitutionInstanceVarType) =
        addVarName(SubstitutionInstanceVar(name, type, time))

    fun addVarName(substitutionInstanceVar: SubstitutionInstanceVar): Int {
        varsList.add(substitutionInstanceVar)
        return time++
    }

    fun popVarsAfter(time: Int): List<SubstitutionInstanceVar> { // inclusive
        val parts =
            varsList.partition { it.time >= time } // can be done more effective, by O(log(n)) - just find start be binary search and split list. todo: fix it
        varsList = parts.second.toMutableList()
        return parts.first
    }
}

data class MatchedNode(
    var matchedFactIndex: Int = Int.MAX_VALUE,
    var nodeType: ComparableTransformationPartType = ComparableTransformationPartType.EMPTY,
    val children: MutableList<MatchedNode> = mutableListOf()
)

data class SubstitutionInstance(
    var isApplicable: Boolean = true,
    private var varRuleNameToExpressionValueMap: MutableMap<String, ExpressionNode> = mutableMapOf(),
    private var varRuleFunctionNameArgsToExpressionMap: MutableMap<String, ExpressionNode> = mutableMapOf(),
    private var varRuleNameToComparableTransformationsPartMap: MutableMap<String, ComparableTransformationsPart> = mutableMapOf(),
    var varNamesTimeStorage: VarNamesTimeStorage = VarNamesTimeStorage(),
    var correspondingIndexes: MatchedNode = MatchedNode()
) {
    fun logValue(message: () -> String, messageType: MessageType = MessageType.TECHNICAL, level: Int) {
        log.add(
            isApplicable,
            correspondingIndexes.matchedFactIndex,
            { "${message.invoke()}, isApplicable: '" },
            { "', matchedFactIndex: '" },
            { "'" })
        log.add(varRuleNameToExpressionValueMap.entries.joinToString(separator = ", ") { "${it.key} = '${it.value.toString()}'" },
            { "NameToExpressionValue: '''" },
            { "'''" },
            messageType = messageType,
            level = level + 1
        )
        log.add(varRuleFunctionNameArgsToExpressionMap.entries.joinToString(separator = ", ") { "${it.key} = '${it.value.toString()}'" },
            { "FunctionNameArgsToExpression: '''" },
            { "'''" },
            messageType = messageType,
            level = level + 1
        )
        log.add(varRuleNameToComparableTransformationsPartMap.entries.joinToString(separator = ", ") { "${it.key} = '${it.value.toString()}'" },
            { "NameToFact: '''" },
            { "'''" },
            messageType = messageType,
            level = level + 1
        )
    }

    fun dropExtraVarsAfter(time: Int) {
        val extraVars = varNamesTimeStorage.popVarsAfter(time)
        for (extraVar in extraVars) {
            when (extraVar.type) {
                SubstitutionInstanceVarType.EXPR_VAR -> varRuleNameToExpressionValueMap.remove(extraVar.name)
                SubstitutionInstanceVarType.EXPR_FUNC -> varRuleFunctionNameArgsToExpressionMap.remove(extraVar.name)
                SubstitutionInstanceVarType.COMPARABLE_VAR -> varRuleNameToComparableTransformationsPartMap.remove(
                    extraVar.name
                )
            }
        }
    }

    fun putExprVar(a: String, b: ExpressionNode) {
        putComparableVar(a, Expression(0, 0, b))
    }

    fun putExprFunc(a: String, b: ExpressionNode) {
        varRuleFunctionNameArgsToExpressionMap.put(a, b)
        varNamesTimeStorage.addVarName(a, SubstitutionInstanceVarType.EXPR_FUNC)
    }

    fun putComparableVar(a: String, b: ComparableTransformationsPart) {
        varRuleNameToComparableTransformationsPartMap.put(a, b)
        varNamesTimeStorage.addVarName(a, SubstitutionInstanceVarType.COMPARABLE_VAR)
        if (b.type() == ComparableTransformationPartType.EXPRESSION) {
            varRuleNameToExpressionValueMap.put(a, (b as Expression).data)
            varNamesTimeStorage.addVarName(a, SubstitutionInstanceVarType.EXPR_VAR)
        }

    }

    fun getExprVar(a: String) = varRuleNameToExpressionValueMap.get(a)
    fun removeExprVar(a: String) = varRuleNameToExpressionValueMap.remove(a)
    fun getExprFunc(a: String) = varRuleFunctionNameArgsToExpressionMap.get(a)
    fun getComparableVar(a: String) = varRuleNameToComparableTransformationsPartMap.get(a)
}

data class SubstitutionPlace(
    val nodeParent: ExpressionNode,
    val nodeChildIndex: Int,
    val substitutionInstance: SubstitutionInstance,
    val originalValue: ExpressionNode,
    val originalExpression: ExpressionNode
)

class ExpressionSubstitution(
    val left: ExpressionNode,
    val right: ExpressionNode,
    val weight: Double = 1.0,
    val basedOnTaskContext: Boolean = false,
    val name: String = "",
    val comparisonType: ComparisonType = ComparisonType.EQUAL,
    val leftFunctions: Set<String> = left.getContainedFunctions(),
    val matchJumbledAndNested: Boolean = false
) {
    var identifier = ""
    fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = "[${left.computeIdentifier()}->${right.computeIdentifier()}]"
        }
        return identifier
    }

    fun checkCondition(
        expressionNode: ExpressionNode, conditionNode: ExpressionNode,
        substitutionInstance: SubstitutionInstance,
        nameArgsMap: MutableMap<String, String> = mutableMapOf()
    ) {
        checkConditionCompanion(expressionNode, conditionNode, substitutionInstance, nameArgsMap, basedOnTaskContext, matchJumbledAndNested)
    }

    companion object {
        fun checkConditionCompanion(
            expressionNode: ExpressionNode, conditionNode: ExpressionNode,
            substitutionInstance: SubstitutionInstance,
            nameArgsMap: MutableMap<String, String> = mutableMapOf(),
            basedOnTaskContext: Boolean,
            matchJumbledAndNested: Boolean,
            onlyCheckListFlag: MutableList<Boolean>? = null
        ) {
            if (conditionNode.isNumberValue()) {
                val expressionValue = if (expressionNode.nodeType == NodeType.VARIABLE){
                    expressionNode.value
                } else if (expressionNode.calcComplexity() < 4 && !expressionNode.containsVariables()) {
                    expressionNode.computeNodeIfSimple()?.toString() ?: ""
                } else ""
                if (expressionValue.isEmpty() || !isNumberValuesEqual(expressionValue, conditionNode.value)) {
                    if (onlyCheckListFlag == null) {
                        substitutionInstance.isApplicable = false
                    } else {
                        onlyCheckListFlag.add(false)
                    }
                    return
                }
            } else if (conditionNode.children.size > 0) {
                if (isNameForRulesDesignation(basedOnTaskContext, conditionNode)) {
                    if (onlyCheckListFlag != null) return
                    val actualNameArgsMap = mutableMapOf<String, String>()
                    val conditionChildrenMap = mutableMapOf<String, String>()
                    for (j in 0..conditionNode.children.lastIndex)
                        conditionChildrenMap.put(conditionNode.children[j].value, "sys_def_func_agr_$j")
                    for ((key, value) in nameArgsMap) {
                        val orderValue = conditionChildrenMap.get(value)
                        if (orderValue != null)
                            actualNameArgsMap.put(key, orderValue)
                    }
                    val functionValue = substitutionInstance.getExprFunc(
                        conditionNode.value + "_" + conditionNode.children.size
                    )
                    if (functionValue == null) {
                        substitutionInstance.putExprFunc(
                            conditionNode.value + "_" + conditionNode.children.size,
                            expressionNode.cloneWithNormalization(actualNameArgsMap, sorted = false)
                        )
                    } else {
                        if (!expressionNode.isNodeSubtreeEquals(functionValue, actualNameArgsMap)) {
                            substitutionInstance.isApplicable = false
                            return
                        }
                    }
                } else {
                    if (conditionNode.children.size != expressionNode.children.size ||
                        !conditionNode.isNodeValueEquals(expressionNode)
                    ) {
                        if (onlyCheckListFlag == null) {
                            substitutionInstance.isApplicable = false
                        } else {
                            onlyCheckListFlag.add(false)
                        }
                        return
                    }
                    val argumentStartIndex =
                        conditionNode.functionStringDefinition?.function?.numberOfDefinitionArguments ?: 0
                    if (argumentStartIndex != 0 && onlyCheckListFlag != null) return
                    for (i in 0 until argumentStartIndex) { //for indexes in n-placement functions
                        nameArgsMap.put(expressionNode.children[i].value, conditionNode.children[i].value)
                    }
                    if (matchJumbledAndNested && conditionNode.children.size > 1 && conditionNode.functionStringDefinition?.function?.isCommutativeWithNullWeight ?: false) {
                        val usedNodes = mutableSetOf<Int>()
                        var expressionStartNodeIndex = argumentStartIndex
                        for (i in argumentStartIndex..conditionNode.children.lastIndex) {
                            var isMatched = false
                            for (j in expressionStartNodeIndex..expressionNode.children.lastIndex) {
                                if (j in usedNodes)
                                    continue

                                val checkListFlag = mutableListOf<Boolean>()
                                checkConditionCompanion(expressionNode.children[j], conditionNode.children[i], substitutionInstance, nameArgsMap, basedOnTaskContext, matchJumbledAndNested, checkListFlag)
                                //fast approximate check
                                if (checkListFlag.isNotEmpty()){
                                    continue
                                }

                                checkConditionCompanion(expressionNode.children[j], conditionNode.children[i], substitutionInstance, nameArgsMap, basedOnTaskContext, matchJumbledAndNested)
                                if (!substitutionInstance.isApplicable) return
                                if (j == expressionStartNodeIndex) {
                                    expressionStartNodeIndex++
                                } else {
                                    usedNodes.add(j)
                                }
                                isMatched = true
                                break
                            }
                            if (!isMatched) {
                                if (onlyCheckListFlag == null) {
                                    substitutionInstance.isApplicable = false
                                } else {
                                    onlyCheckListFlag.add(false)
                                }
                                return
                            }
                        }
                    } else {
                        for (i in argumentStartIndex..conditionNode.children.lastIndex) {
                            if (onlyCheckListFlag != null) {
                                checkConditionCompanion(expressionNode.children[i], conditionNode.children[i], substitutionInstance, nameArgsMap, basedOnTaskContext, matchJumbledAndNested, onlyCheckListFlag)
                                //fast approximate check
                                if (onlyCheckListFlag.isNotEmpty()) {
                                    return
                                }
                            }
                            checkConditionCompanion(expressionNode.children[i], conditionNode.children[i], substitutionInstance, nameArgsMap, basedOnTaskContext, matchJumbledAndNested)
                            if (!substitutionInstance.isApplicable) return
                        }
                    }
                    for (i in 0 until argumentStartIndex) {
                        nameArgsMap.remove(expressionNode.children[i].value)
                    }
                }
            } else {
                if (basedOnTaskContext) {
                    if (!conditionNode.isNodeSubtreeEquals(expressionNode)) {
                        if (onlyCheckListFlag == null) {
                            substitutionInstance.isApplicable = false
                        } else {
                            onlyCheckListFlag.add(false)
                        }
                    }
                    return
                } else {
                    val varValue = substitutionInstance.getExprVar(conditionNode.value)
                    if (varValue == null) {
                        if (onlyCheckListFlag != null) return
                        substitutionInstance.putExprVar(conditionNode.value, expressionNode)
                    } else {
                        if (!varValue.isNodeSubtreeEquals(expressionNode)) {
                            if (onlyCheckListFlag == null) {
                                substitutionInstance.isApplicable = false
                            } else {
                                onlyCheckListFlag.add(false)
                            }
                            return
                        }
                    }
                }
            }
        }

        private fun isNameForRulesDesignation(
            basedOnTaskContext: Boolean,
            conditionNode: ExpressionNode
        ) = !basedOnTaskContext && conditionNode.functionStringDefinition != null &&
            conditionNode.functionStringDefinition!!.function.isNameForRuleDesignations


        fun applyRightCompanion(substitutionInstance: SubstitutionInstance, right: ExpressionNode): ExpressionNode? {
            if (right.value.isEmpty() && right.children.size == 1) {
                return applyRightCompanion(substitutionInstance, right.children[0])
            } else if (right.isNumberValue()) {
                return right.copy()
            } else if (right.children.size != 0) {
                if (right.functionStringDefinition?.function?.isNameForRuleDesignations ?: false) {
                    val actualNameArgsMap = mutableMapOf<String, String>()
                    val conditionChildrenMap = mutableMapOf<String, String>()
                    val functionNameArgsToExpressionMap: MutableMap<String, ExpressionNode> = mutableMapOf()
                    for (j in 0..right.children.lastIndex)
                        functionNameArgsToExpressionMap.put(
                            "sys_def_func_agr_$j",
                            substitutionInstance.getExprVar(right.children[j].value)
                                ?.cloneWithNormalization(sorted = false) ?: return null
                        )
                    return substitutionInstance.getExprFunc(right.value + "_" + right.children.size)
                        ?.cloneWithDeepSubstitutions(functionNameArgsToExpressionMap)?.normalizeSubTree(sorted = false)
                        ?: return null
                } else {
                    val argumentStartIndex = right.functionStringDefinition?.function?.numberOfDefinitionArguments ?: 0
                    for (i in 0 until argumentStartIndex) {
                        substitutionInstance.putExprVar(right.children[i].value, right.children[i])
                    }
                    val result = right.copy()
                    for (i in 0..right.children.lastIndex) {
                        result.addChild(applyRightCompanion(substitutionInstance, right.children[i]) ?: return null)
                    }
                    for (i in 0 until argumentStartIndex) {
                        substitutionInstance.removeExprVar(right.children[i].value)
                    }
                    return result
                }
            } else {
                return (substitutionInstance.getExprVar(right.value) ?: right).cloneWithNormalization(sorted = false)
            }
        }
    }

    fun checkLeftCondition(expressionNode: ExpressionNode): SubstitutionInstance {
        val substitutionInstance = SubstitutionInstance()
        checkCondition(expressionNode, left.children[0], substitutionInstance)
        return substitutionInstance
    }

    private fun findAllPossibleSubstitutionPlaces(root: ExpressionNode, originalExpression: ExpressionNode, result: MutableList<SubstitutionPlace>) {
        for (i in 0 until root.children.size) {
            findAllPossibleSubstitutionPlaces(root.children[i], originalExpression, result)
            val substitutionInstance = checkLeftCondition(root.children[i])
            if (substitutionInstance.isApplicable) {
                result.add(SubstitutionPlace(root, i, substitutionInstance, root.children[i], originalExpression))
            }
        }
    }

    fun findAllPossibleSubstitutionPlaces(root: ExpressionNode): MutableList<SubstitutionPlace> {
        val result = mutableListOf<SubstitutionPlace>()
        findAllPossibleSubstitutionPlaces(root, root, result)
        return result
    }

    fun applySubstitution(substitutionPlaces: List<SubstitutionPlace>) {
        for (substitutionPlace in substitutionPlaces) {
            val newValue = checkAndApply(substitutionPlace.nodeParent.children[substitutionPlace.nodeChildIndex])
            if (newValue != null) {
                substitutionPlace.nodeParent.children[substitutionPlace.nodeChildIndex] = newValue
            }
        }
    }

    fun applySubstitutionByBitMask(substitutionPlaces: List<SubstitutionPlace>, bitMask: Int) {
        for (i in 0 until substitutionPlaces.size) {
            val substitutionPlace = substitutionPlaces[i]
            substitutionPlace.nodeParent.children[substitutionPlace.nodeChildIndex] =
                substitutionPlace.originalValue //roll back previous substitutions
        }
        for (i in 0 until substitutionPlaces.size) {
            val substitutionPlace = substitutionPlaces[i]
            if (bitMask and (1 shl i) == 0) continue
            val newValue = applyRight(substitutionPlace.substitutionInstance)
            if (newValue != null) {
                substitutionPlace.nodeParent.children[substitutionPlace.nodeChildIndex] =
                    newValue //to save. to test such case for expressions - todo - done
            }
        }
    }

    fun applyRight(substitutionInstance: SubstitutionInstance, right: ExpressionNode = this.right.children[0]) =
        applyRightCompanion(substitutionInstance, right)

    fun checkAndApply(expressionNode: ExpressionNode): ExpressionNode? {
        val substitutionInstance = checkLeftCondition(expressionNode)
        if (substitutionInstance.isApplicable)
            return applyRight(substitutionInstance)
        else
            return null
    }
}

fun ExpressionNode.applyAllFunctionSubstitutions(compiledSubstitutions: Map<String, ExpressionSubstitution>) {
    var i = 0
    while (i < children.size) {
        children[i].applyAllFunctionSubstitutions(compiledSubstitutions)
        val substitution = compiledSubstitutions.get(children[i].value + "_" + children[i].children.size)
        if (substitution != null) {
            children[i] = substitution.checkAndApply(children[i]) ?: children[i]
        }
        i++
    }
}

fun ExpressionNode.applyAllSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {
    for (substitution in expressionSubstitutions) {
        val substitutionPlaces = substitution.findAllPossibleSubstitutionPlaces(this)
        substitution.applySubstitution(substitutionPlaces)
    }
}

fun ExpressionNode.applyAllImmediateSubstitutions(compiledConfiguration: CompiledConfiguration) {
    variableReplacement(compiledConfiguration.compiledImmediateVariableReplacements)
    applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
}