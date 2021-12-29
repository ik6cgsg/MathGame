package mathhelper.twf.taskautogeneration

import mathhelper.twf.api.*
import mathhelper.twf.baseoperations.BaseOperationsComputation.Companion.epsilon
import mathhelper.twf.config.*
import mathhelper.twf.expressiontree.*
import mathhelper.twf.api.normalizeExpressionToUsualForm
import mathhelper.twf.platformdependent.random
import mathhelper.twf.platformdependent.randomBoolean
import mathhelper.twf.platformdependent.randomInt
import kotlin.math.min

enum class ExpressionGenerationDirection { ORIGINAL_TO_FINAL, FINAL_TO_ORIGINAL }

data class GeneratedExpression(
        var expressionNode: ExpressionNode,
        var code: String? = null,
        var nameEn: String? = null,
        var nameRu: String? = null,
        var descriptionShortEn: String? = null,
        var descriptionShortRu: String? = null,
        var descriptionEn: String? = null,
        var descriptionRu: String? = null,

        var subjectType: String? = null,
        var tags: MutableSet<String> = mutableSetOf()
)

data class ExpressionTaskGeneratorSettings(
        val expressionGenerationDirection: ExpressionGenerationDirection = ExpressionGenerationDirection.FINAL_TO_ORIGINAL,
        val goalStepsCount: Int = 5,
        val goalDifferentRulesCount: Int = 4,
        val taskStartGenerator: (compiledConfiguration: CompiledConfiguration) -> GeneratedExpression = {
            GeneratedExpression(
                    ExpressionNode(NodeType.FUNCTION, "").apply { addChild(ExpressionNode(NodeType.VARIABLE, "1")) },
                    "e1",
                    subjectType = "standard_math"
            )
        },
        val compiledConfiguration: CompiledConfiguration = CompiledConfiguration(),
        val expressionSubstitutions: List<ExpressionSubstitution> = compiledConfiguration.compiledExpressionTreeTransformationRules,
        val maxCountSelectedOfTasksOnIteration: Int = 10,
        val widthOfRulesApplicationsOnIteration: Int = 10,
        val minStepsCountInAutogeneration: Int = 4,
        val goalCompletionStepsCount: Int = (goalStepsCount + 1) / 2,
        val extendingExpressionSubstitutions: List<ExpressionSubstitution> = expressionSubstitutions.filter { it.priority!! >= 20 },
        val reducingExpressionSubstitutions: List<ExpressionSubstitution> = expressionSubstitutions.filter { it.priority!! <= 30 },
        val mandatoryResultTransformations: List<ExpressionSubstitution> = listOf()
) {
    init {
        compiledConfiguration.setExpressionSubstitutions(expressionSubstitutions)
    }
}


data class ExpressionTask(
        var startExpression: ExpressionNode,
        var currentExpression: ExpressionNode = startExpression,
        var requiredSubstitutions: MutableSet<ExpressionSubstitution> = mutableSetOf(),
        var usedSubstitutions: MutableList<ExpressionSubstitution> = mutableListOf(),
        var previousExpressions: MutableList<ExpressionNode> = mutableListOf(),

        var solution: String = "",
        var solutionsStepTree: MutableList<SolutionsStepITR> = mutableListOf(),
        var hints: MutableList<HintITR> = mutableListOf(),
        var time: Int = 0, //seconds
        var badStructureFine: Double = 0.0
) {
    fun clone(): ExpressionTask {
        var result = copy()
        result.currentExpression = currentExpression.clone()
        result.requiredSubstitutions = mutableSetOf()
        result.requiredSubstitutions.addAll(requiredSubstitutions)
        result.usedSubstitutions = mutableListOf()
        result.usedSubstitutions.addAll(usedSubstitutions)
        result.previousExpressions = mutableListOf()
        result.previousExpressions.addAll(previousExpressions)

        result.solutionsStepTree = mutableListOf()
        result.solutionsStepTree.addAll(solutionsStepTree)
        result.hints = mutableListOf()
        result.hints.addAll(hints)
        return result
    }

    fun badExpressionStructureFine(): Double {
        badStructureFine = currentExpression.badStructureFine()
        return badStructureFine
    }
}

fun generateExpressionTransformationTasks(
        expressionTaskGeneratorSettings: ExpressionTaskGeneratorSettings
): List<TaskITR> {
    val compiledConfiguration = expressionTaskGeneratorSettings.compiledConfiguration
    val expressionComporator = compiledConfiguration.factComporator.expressionComporator
    val actualExtendingExpressionSubstitutions = if (expressionTaskGeneratorSettings.expressionGenerationDirection == ExpressionGenerationDirection.FINAL_TO_ORIGINAL) {
        swapPartsInExpressionSubstitutions(expressionTaskGeneratorSettings.extendingExpressionSubstitutions)
    } else {
        expressionTaskGeneratorSettings.extendingExpressionSubstitutions
    }
    val actualReducingExpressionSubstitutions = if (expressionTaskGeneratorSettings.expressionGenerationDirection == ExpressionGenerationDirection.FINAL_TO_ORIGINAL) {
        swapPartsInExpressionSubstitutions(expressionTaskGeneratorSettings.reducingExpressionSubstitutions)
    } else {
        expressionTaskGeneratorSettings.reducingExpressionSubstitutions
    }
    val taskStart = expressionTaskGeneratorSettings.taskStartGenerator.invoke(compiledConfiguration)

    var allTasks = mutableListOf<ExpressionTask>()
    var currentTasks = mutableListOf<ExpressionTask>()
    currentTasks.add(ExpressionTask(taskStart.expressionNode.clone()))


    val iterationsCount = expressionTaskGeneratorSettings.goalStepsCount + 2
    var stepId = 1
    compiledConfiguration.setExpressionSubstitutions(expressionTaskGeneratorSettings.extendingExpressionSubstitutions) // сначала расширяем исходное выражение
    while (currentTasks.isNotEmpty()) {
        var newCurrentTasks = mutableListOf<ExpressionTask>()
        for (currentTask in currentTasks) {
            var currentExpression = currentTask.currentExpression
            currentExpression.computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot()

            // 1. places, then substitutions
            for (j in 1..expressionTaskGeneratorSettings.widthOfRulesApplicationsOnIteration) {
                var selectedNodeIds = currentExpression.selectRandomNodeIdsToTransform()
                if (selectedNodeIds.isEmpty() || (selectedNodeIds.size == 1 && randomInt(0, 100) == 0)) {
                    val randomNodeId = currentExpression.getAllChildrenNodeIds().random()
                    if (selectedNodeIds.isEmpty() || (selectedNodeIds.first() != randomNodeId)) {
                        selectedNodeIds = selectedNodeIds + listOf(randomNodeId)
                    }
                }
                if (selectedNodeIds.isEmpty()) {
                    continue
                }
                val applications = findApplicableSubstitutionsInSelectedPlace(currentExpression, selectedNodeIds.toTypedArray(),
                        compiledConfiguration, withReadyApplicationResult = true)
                for (application in applications) {
                    val newTask = currentTask.clone()
                    newTask.currentExpression = application.resultExpression
                    newTask.previousExpressions.add(newTask.currentExpression.clone())
                    newTask.usedSubstitutions.add(application.expressionSubstitution)

                    if (newTask.previousExpressions.any { compareExpressionNodes(it, newTask.currentExpression) }) {
                        continue
                    }
                    newTask.solutionsStepTree.add(SolutionsStepITR(
                            newTask.currentExpression.toString(),
                            application.expressionSubstitution.code,
                            selectedNodeIds,
                            stepId,
                            newTask.solutionsStepTree.lastOrNull()?.stepId ?: -1))
                    newTask.time += (application.expressionSubstitution.weight + 1).toInt() * selectedNodeIds.size * 2 // 4 seconds on trivial rule
                    newCurrentTasks.add(newTask)
                }
            }


            // 2. substitutions, then places
            val functionsInExpression = currentExpression.getContainedFunctions()
            val appropriateSubstitutions = mutableListOf<ExpressionSubstitution>()
            val actualExpressionSubstitutions = if (stepId < expressionTaskGeneratorSettings.goalCompletionStepsCount) {
                //запутываем исходное выражение
                actualExtendingExpressionSubstitutions
            } else {
                //упрощаем получившеся выражение
                actualReducingExpressionSubstitutions
            }
            for (expressionSubstitution in actualExpressionSubstitutions) {
                if (expressionSubstitution.isAppropriateToFunctions(functionsInExpression) && expressionSubstitution.left.nodeType != NodeType.EMPTY) {
                    if (expressionSubstitution.findAllPossibleSubstitutionPlaces(currentExpression, expressionComporator).isNotEmpty()) {
                        appropriateSubstitutions.add(expressionSubstitution)
                    }
                }
            }
            val appropriateSubstitutionWeight = appropriateSubstitutions.sumByDouble { it.weightInTaskAutoGeneration }
            if (appropriateSubstitutionWeight < epsilon) {
                continue
            }

            for (j in 1..expressionTaskGeneratorSettings.widthOfRulesApplicationsOnIteration) {
                val newTask = currentTask.clone()
                var selector = random(0.0, appropriateSubstitutionWeight)
                var currentSubstitutionIndex = 0
                while (selector > appropriateSubstitutions[currentSubstitutionIndex].weightInTaskAutoGeneration) {
                    selector -= appropriateSubstitutions[currentSubstitutionIndex].weightInTaskAutoGeneration
                    currentSubstitutionIndex++
                }

                val selectedSubstitution = appropriateSubstitutions[currentSubstitutionIndex]
                val places = selectedSubstitution.findAllPossibleSubstitutionPlaces(newTask.currentExpression, expressionComporator)
                if (places.size == 0) {
                    continue
                } else {
                    val changedExpression = newTask.currentExpression.clone()
                    newTask.previousExpressions.add(newTask.currentExpression.clone())
                    newTask.usedSubstitutions.add(selectedSubstitution)
                    val waysOfApplyingCount = (1 shl places.size) + 1
                    val bitMask = if (waysOfApplyingCount > 0) {
                        randomInt(1, waysOfApplyingCount)
                    } else { // overflow
                        randomInt(1, Int.MAX_VALUE)
                    }
                    val changedNodeIds = selectedSubstitution.applySubstitutionByBitMask(places, bitMask)
                    if (newTask.previousExpressions.any { compareExpressionNodes(it, newTask.currentExpression) }) {
                        continue
                    }
                    newTask.solutionsStepTree.add(SolutionsStepITR(
                            changedExpression.toString(),
                            selectedSubstitution.code,
                            changedNodeIds,
                            stepId,
                            newTask.solutionsStepTree.lastOrNull()?.stepId ?: -1))
                    newTask.time += (selectedSubstitution.weight + 1).toInt() * changedNodeIds.size * 2 // 4 seconds on trivial rule
                }
                newCurrentTasks.add(newTask)
            }
        }

        newCurrentTasks = newCurrentTasks.distinctBy { it.currentExpression.toString() }.toMutableList()
        if (stepId < expressionTaskGeneratorSettings.goalCompletionStepsCount) {
            //запутываем исходное выражение
            newCurrentTasks.sortByDescending { it.currentExpression.getContainedFunctions().size }
        } else {
            //упрощаем получившеся выражение
            allTasks.sortBy { it.badExpressionStructureFine() }
            compiledConfiguration.setExpressionSubstitutions(expressionTaskGeneratorSettings.reducingExpressionSubstitutions)
        }
        newCurrentTasks = newCurrentTasks.subList(0, min(expressionTaskGeneratorSettings.maxCountSelectedOfTasksOnIteration, newCurrentTasks.size))

        allTasks.addAll(newCurrentTasks.filter { it.previousExpressions.size >= expressionTaskGeneratorSettings.minStepsCountInAutogeneration })
        allTasks = allTasks.distinctBy { it.currentExpression.toString() }.toMutableList()
        currentTasks = newCurrentTasks.filter { it.previousExpressions.size < iterationsCount }.toMutableList()
    }

    val baseOperationsDefinitions = compiledConfiguration.factComporator.expressionComporator.baseOperationsDefinitions

    allTasks.forEach {
        it.currentExpression.applyAllSubstitutions(expressionTaskGeneratorSettings.mandatoryResultTransformations)
        it.currentExpression = simplifyAndNormalizeExpression(it.currentExpression, compiledConfiguration)
    }
    //unification
    val resultAllTasks = mutableListOf<ExpressionTask>()
    for (task in allTasks) {
        var isNew = true
        for (resultTask in resultAllTasks) {
            if (compiledConfiguration.factComporator.expressionComporator
                            .compareAsIs(task.currentExpression, resultTask.currentExpression)) {
                isNew = false
                break
            }
        }
        if (isNew) {
            resultAllTasks.add(task)
        }
    }

    resultAllTasks.sortBy { it.badExpressionStructureFine() }
    resultAllTasks.forEach {
        it.requiredSubstitutions = it.usedSubstitutions.groupBy { it.code }.values.map { it.first() }.toMutableSet()
        it.hints = it.requiredSubstitutions.map {
            HintITR(
                    textEn = "Use rule $$${expressionToTexString(it.left)}=${expressionToTexString(it.right)}$$",
                    textRu = "Используй правило $$${expressionToTexString(it.left)}=${expressionToTexString(it.right)}$$"
            )
        }.toMutableList()

        val expressionsInSolution = mutableListOf<ExpressionNode>().apply {
            addAll(it.previousExpressions)
            add(it.currentExpression)
        }
        if (expressionTaskGeneratorSettings.expressionGenerationDirection == ExpressionGenerationDirection.FINAL_TO_ORIGINAL) {
            expressionsInSolution.reverse()
        }
        it.solution = expressionsInSolution.map { i -> expressionToString(i) }.joinToString(" = ")
    }

    return resultAllTasks.map {
        TaskITR(
                code = taskStart.code,
                nameEn = taskStart.nameEn,
                nameRu = taskStart.nameRu,
                descriptionShortEn = taskStart.descriptionShortEn,
                descriptionShortRu = taskStart.descriptionShortRu,
                descriptionEn = taskStart.descriptionEn,
                descriptionRu = taskStart.descriptionRu,
                subjectType = taskStart.subjectType,
                tags = taskStart.tags,
                originalExpressionStructureString = if (expressionTaskGeneratorSettings.expressionGenerationDirection == ExpressionGenerationDirection.FINAL_TO_ORIGINAL) {
                    it.currentExpression.toString()
                } else {
                    it.startExpression.toString()
                },

                goalType = "expression",
                goalExpressionStructureString = if (expressionTaskGeneratorSettings.expressionGenerationDirection == ExpressionGenerationDirection.FINAL_TO_ORIGINAL) {
                    it.startExpression.toString()
                } else {
                    it.currentExpression.toString()
                },
                goalPattern = "",

                rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                rules = listOf(),

                stepsNumber = it.previousExpressions.size,
                time = it.time,
                difficulty = 1.0, //TODO correct difficulty

                solutionPlainText = it.solution,
                solutionsStepsTree = mapOf("data" to it.solutionsStepTree),
                hints = mapOf("data" to it.hints)
        )
    }
}

fun simplifyAndNormalizeExpression(expression: ExpressionNode, compiledConfiguration: CompiledConfiguration): ExpressionNode{
    var result = compiledConfiguration.factComporator.expressionComporator.baseOperationsDefinitions.computeExpressionTree(expression)
    normalizeExpressionToUsualForm(result, compiledConfiguration)
    result.normalizeTrivialFunctions()
    simplifyExpressionRecursive(result, compiledConfiguration)
    if (result.value != "") {
        result = ExpressionNode(NodeType.FUNCTION, "").apply {addChild(result) }
    }
    return result
}

fun simplifyExpressionRecursive(expression: ExpressionNode, compiledConfiguration: CompiledConfiguration): ExpressionNode{
    for (child in expression.children) {
        simplifyExpressionRecursive(child, compiledConfiguration)
    }
    if (expression.functionStringDefinition?.function?.isCommutativeWithNullWeight == true) {
        expression.children.sortBy {
            it.toString()
                    .replace("(", "").replace(")", "")
                    .replace('-', '~') //to move minus from start
        }
    }
    return expression
}


fun swapPartsInExpressionSubstitutions(
        expressionSubstitutions: List<ExpressionSubstitution>
): List<ExpressionSubstitution> {
    val swappedExpressionSubstitutions = mutableListOf<ExpressionSubstitution>()
    for (expressionSubstitution in expressionSubstitutions) {
        swappedExpressionSubstitutions.add(ExpressionSubstitution(
                expressionSubstitution.right,
                expressionSubstitution.left,
                expressionSubstitution.weight,
                expressionSubstitution.basedOnTaskContext,
                expressionSubstitution.code,
                expressionSubstitution.nameEn,
                expressionSubstitution.nameRu,
                expressionSubstitution.comparisonType,
                matchJumbledAndNested = expressionSubstitution.matchJumbledAndNested,
                priority = expressionSubstitution.priority,
                changeOnlyOrder = expressionSubstitution.changeOnlyOrder,
                simpleAdditional = expressionSubstitution.simpleAdditional,
                isExtending = expressionSubstitution.isExtending,
                normalizationType = expressionSubstitution.normalizationType,
                weightInTaskAutoGeneration = expressionSubstitution.weightInTaskAutoGeneration
        ))

    }
    return swappedExpressionSubstitutions
}

fun compareExpressionNodes(first: ExpressionNode, second: ExpressionNode): Boolean {
    first.fillStructureStringIdentifiers()
    second.fillStructureStringIdentifiers()
    var result = first.expressionStrictureIdentifier?.equals(second.expressionStrictureIdentifier)
    if (result == null) {
        result = false
    }
    return result as Boolean
}

fun ExpressionNode.badStructureFine(): Double {
    var badStructureFine = 0.0
    if (patternDoubleMinus()) {
        badStructureFine += 2
    }
    if (patternUnaryMinus()) {
        badStructureFine += 0.5
    }
    if (patternDoubleMinusInFraction()) {
        badStructureFine += 3
    }
    if (patternThreeLevelsInFraction()) {
        badStructureFine += 0.5
    }
    /*if (patternTooManyLevelsInFraction()) {
        patternCheckerFine += 3
    }*/
    if (patternTooManyLevelsExist()) {
        badStructureFine += 3
    }
    if (patternConstMulConst()) {
        badStructureFine += 3
    }
    badStructureFine += getDepth()
    return badStructureFine
}

fun ExpressionNode.selectRandomNodeIdsToTransform(): List<Int> {
    val depth = getDepth()
    val rnd = randomInt(0, depth)
    if (rnd == 0 && value != "") {
        return listOf(nodeId)
    } else if (rnd == 1 && functionStringDefinition?.function?.isCommutativeWithNullWeight == true) {
        val result = mutableListOf<Int>()
        val childrenShuffled = children.shuffled()
        for (child in childrenShuffled) {
            if (randomInt(0, result.size) == 0) {
                result.add(child.nodeId)
            }
        }
        return result
    } else {
        val childrenShuffled = children.shuffled()
        for (child in childrenShuffled) {
            val result = child.selectRandomNodeIdsToTransform()
            if (result.isNotEmpty() && randomBoolean()) {
                return result
            }
        }
    }
    return emptyList()
}
