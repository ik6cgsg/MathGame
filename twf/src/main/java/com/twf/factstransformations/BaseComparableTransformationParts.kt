package com.twf.factstransformations

import com.twf.baseoperations.BaseOperationsDefinitions
import com.twf.config.*
import com.twf.expressiontree.*
import com.twf.logs.MessageType
import com.twf.logs.log
import com.twf.standartlibextensions.TransformationsPart
import com.twf.visualization.ColoringTask

enum class ComparableTransformationPartType { EXPRESSION, EXPRESSION_COMPARISON, MAIN_LINE_AND_NODE, MAIN_LINE_OR_NODE, EXPRESSION_CHAIN, RULE, RULE_POINTER, EMPTY }
interface ComparableTransformationsPart : TransformationsPart {
    var identifier: String
    fun computeIdentifier(recomputeIfComputed: Boolean): String
    fun computeInIdentifier(recomputeIfComputed: Boolean): String
    fun computeOutIdentifier(recomputeIfComputed: Boolean): String
    fun computeSortedOutIdentifier(recomputeIfComputed: Boolean): String
    fun type(): ComparableTransformationPartType

    fun copyNode(): ComparableTransformationsPart
    fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean): ComparableTransformationsPart
    fun variableReplacement(replacements: Map<String, String>)

    fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
              factsTransformations: List<FactSubstitution>, //not map with key <name> because it can be substitutions without names
              expressionTransformations: List<ExpressionSubstitution>, //not map with key <name> because it can be substitutions without names
              additionalFacts: List<MainChainPart>): ComparisonResult
}

data class ComparisonResult(
        val isCorrect: Boolean,
        val coloringTasks: MutableList<ColoringTask>,
        val left: ComparableTransformationsPart,
        val right: ComparableTransformationsPart,
        val description: String = "",
        val additionalFactUsed: Boolean = false
)

interface MainChainPart : ComparableTransformationsPart {
    var parent: MainLineNode?

    fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean)
    fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>)
    fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions)
    fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>)

    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean): MainChainPart
    fun clone(): MainChainPart

    fun isSolutionForVariables (targetVariables: MutableMap<String, Boolean>, left: Boolean = false, allowedVariables: Set<String>): GeneralError?
    fun isFactorizationForVariables(minNumberOfMultipliers: Int, targetVariables: Set<String>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError?
    fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError?
    fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError?
}

interface MainLineNode : MainChainPart {
    //check sequence:
    //1. check that inFacts (factTransformationChains starts) follows from out facts of previous system (if facts were transformed, its must be written in the same sequence; steps, in which facts structure was transformed, cannot also include facts transformations)
    //2. check that factTransformationChains are correct
    val factTransformationChains: MutableList<MainChain> //facts transformations inside node, connects in and out facts
    val inFacts: MutableList<MainChainPart>
    val outFacts: MutableList<MainChainPart>
    val expressionTransformationChains: MutableList<ExpressionChain> //facts transformations inside node, connects in and out facts, comparison chain simplification
    val rules: MutableList<Rule>

    fun addStartNewFactChain()
    fun addExpressionComparisonFact(fact: MainChainPart)
    fun getActualChain(): MutableList<MainChainPart>

    /**
     * Returns: is MainChainPart correct, if it is numeric; NULL otherwise
     */
    fun computeIfNumeric(substitutionInstance: SubstitutionInstance, baseOperationsDefinitions: BaseOperationsDefinitions, checkOutMainLineNodePart: Boolean): Boolean?

    fun isEmpty(): Boolean = (factTransformationChains.isEmpty() && inFacts.isEmpty() && outFacts.isEmpty() && expressionTransformationChains.isEmpty() && rules.isEmpty())
}

class Expression(
        override val startPosition: Int = 0,
        override val endPosition: Int = 0,
        var data: ExpressionNode,
        override var identifier: String = "",
        override var parent: MainLineNode? = null
) : MainChainPart {
    override fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError?{
        for (function in forbidden) {
            if (data.containsFunction(function.first, function.second)){
                return GeneralError("Answer contains forbidden function or operation")
            }
        }
        return null
    }

    override fun isFactorizationForVariables(minNumberOfMultipliers: Int,
                                             targetVariables: Set<String>,
                                             targetExpression: ExpressionNode,
                                             factComporator: FactComporator): GeneralError?{
        val comparisonResult = factComporator.expressionComporator.compareWithoutSubstitutions(data, targetExpression,
                definedFunctionNameNumberOfArgs = FunctionConfiguration().notChangesOnVariablesInComparisonFunction
                        .map { it.getIdentifier() }.toSet())
        if (!comparisonResult){
            return GeneralError("Answer not equal to original expression")
        }
        if (minNumberOfMultipliers <= 1){
            return null
        }
        var topNode = data
        while (topNode.value.isBlank() && topNode.children.size == 1){
            topNode = topNode.children.first()
        }
        if (topNode.value != "*"){
            return GeneralError("Answer is not factorized")
        }
        var numberOfMultipliers = 0
        for (child in topNode.children){
            val containedVariables = data.getContainedVariables(targetVariables)
            if (containedVariables.isNotEmpty()){
                numberOfMultipliers++
            }
        }
        if (numberOfMultipliers < minNumberOfMultipliers){
            return GeneralError("Not enough multipliers in the answer")
        }

        return null
    }

    override fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        val comparisonResult = factComporator.expressionComporator.compareWithoutSubstitutions(data, targetExpression,
                definedFunctionNameNumberOfArgs = FunctionConfiguration().notChangesOnVariablesInComparisonFunction
                        .map { it.getIdentifier() }.toSet())
        if (!comparisonResult){
            return GeneralError("Answer not equal to original expression")
        }
        if (data.canContainDivisions()){
            return GeneralError("Answer contains fraction")
        }
        return null
    }

    override fun isSolutionForVariables(targetVariables: MutableMap<String, Boolean>, left: Boolean, allowedVariables: Set<String>): GeneralError? {
        if (left){
            if (data.containsFunctions()){
                return GeneralError("left part of the result contains functions or operations")
            }
            val containedVariables = data.getVariableNames()
            if (containedVariables.size > 1){
                return GeneralError("left part of the result contains more than one variable: ${containedVariables.joinToString {"'$it'"} }")
            } else if (containedVariables.size == 1) {
                targetVariables.put(containedVariables.first(), true)
            }
            return null
        } else {
            val containedVariables = data.getContainedVariables(targetVariables.keys)
            if (containedVariables.isNotEmpty()){
                return GeneralError("right part of the result contains unknown variables: ${containedVariables.joinToString {"'$it'"} }")
            } else if (allowedVariables.isNotEmpty()){
                val notContainedVariables = data.getNotContainedVariables(allowedVariables)
                if (notContainedVariables.isNotEmpty()){
                    return GeneralError("right part of the result contains unknown variables: ${notContainedVariables.joinToString {"'$it'"} }")
                }
            }
            return null
        }
    }

    override fun variableReplacement(replacements: Map<String, String>) {
        data.variableReplacement(replacements)
    }

    fun computeIfNumeric(substitutionInstance: SubstitutionInstance, baseOperationsDefinitions: BaseOperationsDefinitions): Double? {
        val variableNames = data.getVariableNames()
        val pointI = mutableMapOf<String, String>()
        for (variableName in variableNames) {
            val node = substitutionInstance.getExprVar(variableName) ?: continue
            val value = baseOperationsDefinitions.computeExpressionTree(node.clone()).value
            pointI.put(variableName, value)
        }
        val result = baseOperationsDefinitions.computeExpressionTree(data.cloneWithNormalization(pointI, false)).value.toDoubleOrNull()
        return result
    }

    override fun clone() = Expression(startPosition, endPosition, data.clone(), parent = parent)
    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) =
            Expression(startPosition, endPosition, data.cloneWithNormalization(nameArgsMap, sorted), parent = parent)

    override fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean) {
        data.normalizeSubTree(currentDeep, nameArgsMap, sorted)
    }

    override fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {
        data.applyAllSubstitutions(expressionSubstitutions)
    }

    override fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions) {
        data = baseOperationsDefinitions.computeExpressionTree(data)
    }

    override fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>) {
        data.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet)
    }

    override fun copyNode() = Expression(startPosition, endPosition, ExpressionNode(NodeType.EMPTY, ""), parent = parent)
    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessage({ "This method must not been called. Undefined behaviour. MSG_CODE_1537133804710" })
        return ComparisonResult(true, mutableListOf(), this, this)
    }

    override fun type() = ComparableTransformationPartType.EXPRESSION
    override fun toString() = data.toString()
    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = data.computeIdentifier()
        }
        return identifier
    }

    override fun computeInIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)

    fun getDesignation() = if (data.nodeType == NodeType.VARIABLE) data.value
    else if (data.value == "" && data.children.size == 1 && data.children[0].nodeType == NodeType.VARIABLE) data.children[0].value
    else ""

    fun isDesignation() = if (data.nodeType == NodeType.VARIABLE) true
    else if (data.value == "" && data.children.size == 1 && data.children[0].nodeType == NodeType.VARIABLE) true
    else false

    companion object {
        fun parseFromFactIdentifier(string: String, parent: MainLineNode? = null): Expression? {
            val expressionTreeParser = ExpressionTreeParser(string, true,
                    compiledImmediateVariableReplacements = mapOf<String, String>(*(VariableConfiguration().variableImmediateReplacementRules.map { Pair(it.left, it.right) }.toTypedArray())))
            val error = expressionTreeParser.parse()
            return Expression(0, 0, expressionTreeParser.root, parent = parent)
        }
    }
}

fun emptyExpression() = Expression(0, 0, ExpressionNode(NodeType.EMPTY, ""))


class ExpressionChain(
        override val startPosition: Int = 0,
        override val endPosition: Int = 0,
        val comparisonType: ComparisonType,
        val chain: MutableList<ComparableTransformationsPart> = mutableListOf(),
        override var identifier: String = ""
) : ComparableTransformationsPart {
    override fun type() = ComparableTransformationPartType.EXPRESSION_CHAIN
    override fun toString() = chain.joinToString(separator = comparisonType.string) { it.toString() }
    override fun copyNode() = ExpressionChain(startPosition, endPosition, comparisonType, mutableListOf())
    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) = ExpressionChain(startPosition, endPosition, comparisonType,
            chain.map { it.cloneWithNormalization(nameArgsMap, sorted) }.toMutableList())

    override fun variableReplacement(replacements: Map<String, String>) {
        chain.forEach { it.variableReplacement(replacements) }
    }

    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = chain.joinToString(separator = comparisonType.string) { it.computeIdentifier(recomputeIfComputed) }
        }
        return identifier
    }

    override fun computeInIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)

    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessage({ "Start checking expression chain" }, MessageType.USER, levelChange = 1)
        log.add(chain.toString(), { "expression chain: " }, { "" })
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        val coloringTasks = mutableListOf<ColoringTask>()
        var currentLeftIndex = log.assignAndLog(0, currentLogLevel, { "currentLeftIndex" })
        var currentRightIndex = log.assignAndLog(1, currentLogLevel, { "currentRightIndex" })
        var additionalFactUsed = false
        while (currentRightIndex < chain.size) {
            if (chain[currentRightIndex].type() != ComparableTransformationPartType.EXPRESSION) {
                val transformation = if (chain[currentRightIndex].type() == ComparableTransformationPartType.RULE) {
                    val rule = chain[currentRightIndex] as Rule
                    log.addMessageWithFactDetail({ "Check ${CheckingKeyWords.rule}" }, rule.root, MessageType.USER, level = currentLogLevel)
                    val checkingResult = rule.check(factComporator, false, factsTransformations, expressionTransformations, additionalFacts)
                    if (checkingResult.isCorrect && rule.expressionSubstitution != null) {
                        log.addMessageWithExpressionSubstitutionShort({ "Expression transformation recognized:" }, rule.expressionSubstitution!!, MessageType.USER,
                                level = currentLogLevel)
                        rule.expressionSubstitution!!
                    } else {
                        if (!checkingResult.isCorrect) {
                            log.addMessage({ "${CheckingKeyWords.rule} ${CheckingKeyWords.verificationFailed}" }, MessageType.USER, level = currentLogLevel)
                            coloringTasks.addAll(checkingResult.coloringTasks)
                            return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex + 1],
                                    "${CheckingKeyWords.rule} ${CheckingKeyWords.verificationFailed}")
                        } else {
                            log.addMessage({ "${CheckingKeyWords.rule} ${CheckingKeyWords.isNotExpressionRule} -> ${CheckingKeyWords.verificationFailed}" }, MessageType.USER, level = currentLogLevel)
                            log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex + 1].startPosition,
                                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factNotHelpFactColor,
                                    { "Coloring task on positions: '" }, { "' - '" }, { "', factNotHelpFactColor = '" }, { "" }, level = currentLogLevel)
                            coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex + 1].startPosition,
                                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factNotHelpFactColor))
                            return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex + 1],
                                    "${CheckingKeyWords.rule} ${CheckingKeyWords.isNotExpressionRule}")
                        }
                    }
                } else {
                    val ruleName = (chain[currentRightIndex] as RulePointer).nameLink
                    log.add(ruleName, { "Handling ${CheckingKeyWords.ruleReference} '" }, { "'" }, messageType = MessageType.USER, level = currentLogLevel)
                    val rules = expressionTransformations.filter { it.name == ruleName }
                    if (rules.isEmpty()) {
                        log.add(ruleName, { "ERROR: ${CheckingKeyWords.ruleReference} '" }, { "' not found" }, messageType = MessageType.USER, level = currentLogLevel)
                        log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex + 1].startPosition,
                                factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongFactColor,
                                { "Coloring task on positions: '" }, { "' - '" }, { "', wrongFactColor = '" }, { "" }, level = currentLogLevel)
                        coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex + 1].startPosition,
                                factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongFactColor))
                        return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex + 1],
                                "Rule with name '$ruleName' not found. Exists only rules with names: ${expressionTransformations.map { it.name }.filter { it.isNotBlank() }.joinToString { "'$it'" }}")
                    }
                    rules.first()
                }
                currentRightIndex = log.assignAndLog(currentRightIndex + 1, currentLogLevel, { "currentRightIndex" })
                log.addMessage({ "Check transformation from left fact to right fact:" }, MessageType.USER, level = currentLogLevel)
                currentLogLevel++
                log.addMessageWithExpression({ "Left expression: " }, (chain[currentLeftIndex] as Expression).data, MessageType.USER, level = currentLogLevel)
                log.addMessageWithExpression({ "Right expression: " }, (chain[currentRightIndex] as Expression).data, MessageType.USER, level = currentLogLevel)

                val result = factComporator.expressionComporator.compareWithTreeTransformationRules(
                        (chain[currentLeftIndex] as Expression).data, (chain[currentRightIndex] as Expression).data,
                        listOf(transformation), expressionChainComparisonType = comparisonType,
                        maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                if (result) {
                    log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', color = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor))
                    log.add(transformation.basedOnTaskContext, { "Transformation verified, isInTaskContext: '" }, { "'" }, messageType = MessageType.USER, level = currentLogLevel)
                    if (transformation.basedOnTaskContext) {
                        additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })//todo: check, may be should be something about task context
                    }
                } else {
                    log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factNotHelpFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', factNotHelpFactColor = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factNotHelpFactColor))
                    log.addMessage({ "${CheckingKeyWords.verificationFailed}" }, MessageType.USER, level = currentLogLevel)
                    return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex],
                            "Unclear transformation between '${(chain[currentLeftIndex] as Expression).data}' and '${(chain[currentRightIndex] as Expression).data}' even with rule")
                }
                currentLogLevel--
            } else {
                log.addMessageWithExpression({ "Left expression: " }, (chain[currentLeftIndex] as Expression).data, MessageType.USER, level = currentLogLevel)
                log.addMessageWithExpression({ "Right expression: " }, (chain[currentRightIndex] as Expression).data, MessageType.USER, level = currentLogLevel)

                val result = factComporator.expressionComporator.compareWithTreeTransformationRules(
                        (chain[currentLeftIndex] as Expression).data, (chain[currentRightIndex] as Expression).data,
                        expressionTransformations.filter { !it.basedOnTaskContext } +
                                factComporator.compiledConfiguration.compiledExpressionTreeTransformationRules.filter { !it.basedOnTaskContext },
                        expressionChainComparisonType = comparisonType,
                        maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                if (result) {
                    log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', correctFactColor = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor))
                    log.addMessage({ "${CheckingKeyWords.transformationVerified} and correct not only in task context" }, MessageType.USER, level = currentLogLevel)
                } else {
                    log.addMessage({ "${CheckingKeyWords.verificationFailed} not only in task context. Try to check transformation only in task context" }, MessageType.USER, level = currentLogLevel)
                    val resultInContext = factComporator.expressionComporator.compareWithTreeTransformationRules(
                            (chain[currentLeftIndex] as Expression).data, (chain[currentRightIndex] as Expression).data,
                            expressionTransformations/*.filter { it.basedOnTaskContext }*/ + factComporator.compiledConfiguration.compiledExpressionTreeTransformationRules,
                            expressionChainComparisonType = comparisonType,
                            maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
                    if (resultInContext) {
                        log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                                factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor,
                                { "Coloring task on positions: '" }, { "' - '" }, { "', correctFactColor = '" }, { "" }, level = currentLogLevel)
                        coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                                factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor))
                        log.addMessage({ "${CheckingKeyWords.transformationVerified} and correct only in task context" }, MessageType.USER, level = currentLogLevel)
                        additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" }) //todo: check, may be should be something about task context
                    } else {
                        log.addMessage({ "Transformation not verified, try to check transformation with rules and additional facts" })
                        val fullFact = ExpressionComparison(leftExpression = chain[currentLeftIndex] as Expression, rightExpression = chain[currentRightIndex] as Expression, comparisonType = comparisonType)
                        val result = factComporator.compareAsIs(
                                MainLineAndNode(), MainLineAndNode(inFacts = mutableListOf(fullFact)), additionalFacts.map { it.computeOutIdentifier(true) }.sorted())
                        if (result) {
                            log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factHelpFactColor,
                                    { "Coloring task on positions: '" }, { "' - '" }, { "', color = '" }, { "" }, level = currentLogLevel)
                            coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factHelpFactColor))
                            additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })
                        } else {
                            log.add(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongTransformationFactColor,
                                    { "Coloring task on positions: '" }, { "' - '" }, { "', wrongFactColor = '" }, { "" }, level = currentLogLevel)
                            coloringTasks.add(ColoringTask(chain[currentLeftIndex].endPosition, chain[currentRightIndex].startPosition,
                                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongTransformationFactColor))
                            log.addMessage({ "${CheckingKeyWords.verificationFailed}" }, MessageType.USER, level = currentLogLevel)
                            return ComparisonResult(false, coloringTasks, chain[currentLeftIndex], chain[currentRightIndex],
                                    "Unclear transformation between '${(chain[currentLeftIndex] as Expression).data}' and '${(chain[currentRightIndex] as Expression).data}' ")
                        }
                    }
                }
            }
            currentLeftIndex = log.assignAndLog(currentRightIndex, currentLogLevel, { "currentLeftIndex" })
            currentRightIndex = log.assignAndLog(currentRightIndex + 1, currentLogLevel, { "currentRightIndex" })
        }
        log.addMessage({ "${CheckingKeyWords.expressionChainVerified}. '${chain.first()}' ${comparisonType.string} '${chain.last()}'" },
                messageType = MessageType.USER, level = currentLogLevel)
        return ComparisonResult(true, coloringTasks, chain.first(), chain.last(), additionalFactUsed = additionalFactUsed)
    }
}

class ExpressionComparison(
        override val startPosition: Int = 0,
        override val endPosition: Int = 0,
        var leftExpression: Expression,
        var rightExpression: Expression,
        val comparisonType: ComparisonType,
        override var parent: MainLineNode? = null,
        override var identifier: String = ""
) : MainChainPart {
    override fun isSolutionWithoutFunctions(forbidden: List<Pair<String,Int>>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError?{
        if (targetExpression.children.isNotEmpty() && !factComporator.expressionComporator.compareAsIs(leftExpression.data, targetExpression)){
            return GeneralError("Wrong start expression")
        }
        return rightExpression.isSolutionWithoutFunctions(forbidden, targetExpression, factComporator)
    }

    override fun isFactorizationForVariables(minNumberOfMultipliers: Int, targetVariables: Set<String>, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (!factComporator.expressionComporator.compareAsIs(leftExpression.data, targetExpression)){
            return GeneralError("Wrong start expression")
        }
        return rightExpression.isFactorizationForVariables(minNumberOfMultipliers, targetVariables, targetExpression, factComporator)
    }

    override fun hasNoFractions(maxNumberOfDivisions: Int, targetExpression: ExpressionNode, factComporator: FactComporator): GeneralError? {
        if (!factComporator.expressionComporator.compareAsIs(leftExpression.data, targetExpression)){
            return GeneralError("Wrong start expression")
        }
        return rightExpression.hasNoFractions(maxNumberOfDivisions, targetExpression, factComporator)
    }

    override fun isSolutionForVariables(targetVariables: MutableMap<String, Boolean>, left: Boolean, allowedVariables: Set<String>): GeneralError? {
        val error = leftExpression.isSolutionForVariables(targetVariables, true, allowedVariables)
        if (error != null){
            return error
        }
        return rightExpression.isSolutionForVariables(targetVariables, false, allowedVariables)
    }

    override fun variableReplacement(replacements: Map<String, String>) {
        leftExpression.variableReplacement(replacements)
        rightExpression.variableReplacement(replacements)
    }

    override fun check(factComporator: FactComporator, onExpressionLevel: Boolean,
                       factsTransformations: List<FactSubstitution>,
                       expressionTransformations: List<ExpressionSubstitution>,
                       additionalFacts: List<MainChainPart>): ComparisonResult {
        log.addMessage({ "Start checking expression comparison" }, MessageType.USER, levelChange = 1)
        var currentLogLevel = log.currentLevel
        log.addMessage({ "Current log level: ${currentLogLevel}" }, level = currentLogLevel)
        log.addMessageWithExpression({ "Left expression: " }, leftExpression.data, MessageType.USER, level = currentLogLevel)
        log.addMessageWithExpression({ "Right expression: " }, rightExpression.data, MessageType.USER, level = currentLogLevel)
        val coloringTasks = mutableListOf<ColoringTask>()
        var additionalFactUsed = false
        val result = factComporator.expressionComporator.compareWithTreeTransformationRules(
                leftExpression.data, rightExpression.data,
                expressionTransformations.filter { !it.basedOnTaskContext } + factComporator.compiledConfiguration.compiledExpressionTreeTransformationRules,
                expressionChainComparisonType = comparisonType,
                maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
        if (result) {
            log.add(leftExpression.endPosition, rightExpression.startPosition,
                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor,
                    { "Coloring task on positions: '" }, { "' - '" }, { "', correctFactColor = '" }, { "" }, level = currentLogLevel)
            coloringTasks.add(ColoringTask(leftExpression.endPosition, rightExpression.startPosition,
                    factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor))
            log.addMessage({ "${CheckingKeyWords.transformationVerified} and correct not only in task context" }, MessageType.USER, level = currentLogLevel)
        } else {
            log.addMessage({ "${CheckingKeyWords.verificationFailed} not only in task context. Try to check transformation only in task context" }, MessageType.USER, level = currentLogLevel)
            val resultInContext = factComporator.expressionComporator.compareWithTreeTransformationRules(
                    leftExpression.data, rightExpression.data,
                    expressionTransformations.filter { it.basedOnTaskContext },
                    expressionChainComparisonType = comparisonType,
                    maxDistBetweenDiffSteps = factComporator.compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps)
            if (resultInContext) {
                log.add(leftExpression.endPosition, rightExpression.startPosition,
                        factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor,
                        { "Coloring task on positions: '" }, { "' - '" }, { "', correctFactColor = '" }, { "" }, level = currentLogLevel)
                coloringTasks.add(ColoringTask(leftExpression.endPosition, rightExpression.startPosition,
                        factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.correctFactColor))
                log.addMessage({ "${CheckingKeyWords.transformationVerified} and correct only in task context" }, MessageType.USER, level = currentLogLevel)
                additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" }) //todo: check, may be should be something about task context
            } else {
                log.addMessage({ "Transformation not verified, try to check transformation with rules and additional facts" })
                val result = factComporator.compareAsIs(
                        MainLineAndNode(), MainLineAndNode(inFacts = mutableListOf(this)), additionalFacts.map { it.computeOutIdentifier(true) }.sorted())
                if (result) {
                    log.add(leftExpression.endPosition, rightExpression.startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factHelpFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', color = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(leftExpression.endPosition, rightExpression.startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.factHelpFactColor))
                    additionalFactUsed = log.assignAndLog(true, currentLogLevel, { "additionalFactUsed" })
                } else {
                    log.add(leftExpression.endPosition, rightExpression.startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongTransformationFactColor,
                            { "Coloring task on positions: '" }, { "' - '" }, { "', wrongFactColor = '" }, { "" }, level = currentLogLevel)
                    coloringTasks.add(ColoringTask(leftExpression.endPosition, rightExpression.startPosition,
                            factComporator.compiledConfiguration.checkedFactAccentuation.checkedFactColor.wrongTransformationFactColor))
                    log.addMessage({ "${CheckingKeyWords.verificationFailed}" }, MessageType.USER, level = currentLogLevel)
                    return ComparisonResult(false, coloringTasks, leftExpression, rightExpression,
                            "Unclear transformation between '${leftExpression.data}' and '${rightExpression.data}' ")
                }
            }
        }

        return ComparisonResult(true, coloringTasks, leftExpression, rightExpression, additionalFactUsed = additionalFactUsed)
    }

    /**
     * Returns: is MainChainPart correct, if it is numeric; NULL otherwise
     */
    fun computeIfNumeric(substitutionInstance: SubstitutionInstance, baseOperationsDefinitions: BaseOperationsDefinitions): Boolean? {
        val l = leftExpression.computeIfNumeric(substitutionInstance, baseOperationsDefinitions) ?: return null
        val r = rightExpression.computeIfNumeric(substitutionInstance, baseOperationsDefinitions) ?: return null
        return when (comparisonType) {
            ComparisonType.EQUAL -> (baseOperationsDefinitions.additivelyEqual(l, r))
            ComparisonType.LEFT_MORE_OR_EQUAL -> (l >= r)
            ComparisonType.LEFT_MORE -> (l > r)
            ComparisonType.LEFT_LESS_OR_EQUAL -> (l <= r)
            ComparisonType.LEFT_LESS -> (l < r)
        }
    }

    override fun copyNode() = ExpressionComparison(startPosition, endPosition, emptyExpression(), emptyExpression(), comparisonType, parent, "")

    override fun clone() = ExpressionComparison(startPosition, endPosition,
            leftExpression.clone(), rightExpression.clone(), comparisonType, parent)

    override fun cloneWithNormalization(nameArgsMap: MutableMap<String, String>, sorted: Boolean) = ExpressionComparison(startPosition, endPosition,
            leftExpression.cloneWithNormalization(nameArgsMap, sorted), rightExpression.cloneWithNormalization(nameArgsMap, sorted), comparisonType, parent)

    override fun normalizeSubTree(currentDeep: Int, nameArgsMap: MutableMap<String, String>, sorted: Boolean) {
        leftExpression.normalizeSubTree(currentDeep, nameArgsMap, sorted)
        rightExpression.normalizeSubTree(currentDeep, nameArgsMap, sorted)
    }

    override fun applyAllExpressionSubstitutions(expressionSubstitutions: Collection<ExpressionSubstitution>) {
        leftExpression.applyAllExpressionSubstitutions(expressionSubstitutions)
        rightExpression.applyAllExpressionSubstitutions(expressionSubstitutions)
    }

    override fun computeExpressionTrees(baseOperationsDefinitions: BaseOperationsDefinitions) {
        leftExpression.computeExpressionTrees(baseOperationsDefinitions)
        rightExpression.computeExpressionTrees(baseOperationsDefinitions)
    }

    override fun replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap: MutableMap<ExpressionNode, String>, definedFunctionNameNumberOfArgsSet: MutableSet<String>) {
        leftExpression.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet)
        rightExpression.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgsSet)
    }

    override fun type() = ComparableTransformationPartType.EXPRESSION_COMPARISON
    override fun toString() = "$leftExpression;${comparisonType.string};$rightExpression"

    override fun computeIdentifier(recomputeIfComputed: Boolean): String {
        if (identifier.isBlank() || recomputeIfComputed) {
            identifier = "${leftExpression.computeIdentifier(recomputeIfComputed)};ec;${comparisonType.string};ec;${rightExpression.computeIdentifier(recomputeIfComputed)}"
        }
        return identifier
    }

    override fun computeInIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)
    override fun computeSortedOutIdentifier(recomputeIfComputed: Boolean) = computeIdentifier(recomputeIfComputed)

    companion object {
        fun parseFromFactIdentifier(string: String, parent: MainLineNode? = null): ExpressionComparison? {
            val parts = string.split(";ec;")
            val result = ExpressionComparison(0, 0, emptyExpression(), emptyExpression(), valueOfComparisonType(parts[1]), parent)
            result.leftExpression = Expression.parseFromFactIdentifier(parts[0])!!
            result.rightExpression = Expression.parseFromFactIdentifier(parts[2])!!
            return result
        }
    }
}