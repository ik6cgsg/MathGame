package expressiontree

import baseoperations.*
import config.CheckingKeyWords.Companion.comparisonTypesConflict
import config.ComparisonType
import config.CompiledConfiguration
import config.strictComparison
import factstransformations.SubstitutionDirection
import logs.MessageType
import logs.log
import numbers.Complex
import optimizerutils.DomainPointGenerator
import kotlin.math.abs
import kotlin.math.max

class ExpressionComporator(
        val baseOperationsDefinitions: BaseOperationsDefinitions = BaseOperationsDefinitions()
) {
    lateinit var compiledConfiguration: CompiledConfiguration
    lateinit var definedFunctionNameNumberOfArgsSet: MutableSet<String>

    fun init(compiledConfiguration: CompiledConfiguration) {
        this.compiledConfiguration = compiledConfiguration
        definedFunctionNameNumberOfArgsSet = compiledConfiguration.definedFunctionNameNumberOfArgsSet
    }

    fun compareAsIs(left: ExpressionNode, right: ExpressionNode, nameArgsMap: MutableMap<String, String> = mutableMapOf(),
                    withBracketUnification: Boolean = false): Boolean {
        val normilized = normalizeExpressionsForComparison(left, right)
        if (normilized.first.isNodeSubtreeEquals(normilized.second, nameArgsMap)){
            return true
        } else if (!withBracketUnification){
            return false
        }
        val lUnified = normilized.first
        lUnified.dropBracketNodesIfOperationsSame()
        val rUnified = normilized.second
        rUnified.dropBracketNodesIfOperationsSame()
        if (lUnified.isNodeSubtreeEquals(rUnified, nameArgsMap)){
            return true
        }
        lUnified.normalizeSubTree(sorted = true)
        rUnified.normalizeSubTree(sorted = true)
        return lUnified.isNodeSubtreeEquals(rUnified, nameArgsMap)
    }

    fun probabilityTestComparison(leftOrigin: ExpressionNode, rightOrigin: ExpressionNode,
                                  comparisonType: ComparisonType = compiledConfiguration.comparisonSettings.defaultComparisonType,
                                  justInDomainsIntersection: Boolean = compiledConfiguration.comparisonSettings.justInDomainsIntersection,

                                  maxMinNumberOfPointsForEquality: Int = compiledConfiguration.comparisonSettings.minNumberOfPointsForEquality,
                                  allowedPartOfErrorTests: Double = compiledConfiguration.comparisonSettings.allowedPartOfErrorTests,
                                  testWithUndefinedResultIncreasingCoef: Double = compiledConfiguration.comparisonSettings.testWithUndefinedResultIncreasingCoef,
                                  useCleverDomain: Boolean = false): Boolean {
        val normalized = normalizeExpressionsForComparison(leftOrigin, rightOrigin)
        val left = normalized.first
        val right = normalized.second
        if (compareAsIs(left, right)) {
            return !strictComparison(comparisonType)
        }
        if (right.children.isEmpty() || left.children.isEmpty()) {
            return false
        }
        baseOperationsDefinitions.computeExpressionTree(left.children[0])
        baseOperationsDefinitions.computeExpressionTree(right.children[0])
        var numberOfRemainingTests = (left.getCountOfNodes() + right.getCountOfNodes()).toDouble()
        if (comparisonType == ComparisonType.EQUAL) {
            val domain = PointGenerator(baseOperationsDefinitions, arrayListOf(left, right))
            val baseOperationsComputationComplex = BaseOperationsComputation(ComputationType.COMPLEX)
            val baseOperationsComputationDouble = BaseOperationsComputation(ComputationType.DOUBLE)
            val totalTests = numberOfRemainingTests
            var passedTests = 0.0
            val minNumberOfPointsForEquality = max(max(left.getMaxMinNumberOfPointsForEquality(), right.getMaxMinNumberOfPointsForEquality()), maxMinNumberOfPointsForEquality)
            while (numberOfRemainingTests-- > 0) {
                val pointI = domain.generateNewPoint()
                val lDouble = baseOperationsComputationDouble.compute(
                        left.cloneWithNormalization(pointI, sorted = false)) as Double
                val rDouble = baseOperationsComputationDouble.compute(
                        right.cloneWithNormalization(pointI, sorted = false)) as Double
                if (lDouble.isNaN() || rDouble.isNaN()) {
                    if ((lDouble.isNaN() != rDouble.isNaN()) && justInDomainsIntersection) {
                        return false
                    }
                    val lComplex = baseOperationsComputationComplex.compute(
                            left.cloneWithNormalization(pointI, sorted = false)) as Complex
                    val rComplex = baseOperationsComputationComplex.compute(
                            right.cloneWithNormalization(pointI, sorted = false)) as Complex
                    if (lComplex.equals(rComplex)) {
                        passedTests++
                    }
                } else {
                    if (!lDouble.isFinite() || !rDouble.isFinite()) {
                        numberOfRemainingTests += testWithUndefinedResultIncreasingCoef
                    } else if (baseOperationsDefinitions.additivelyEqual(lDouble, rDouble)) {
                        passedTests++
                    } else return false
                }
                if (passedTests >= minNumberOfPointsForEquality) {
                    return true
                }
            }
            return (passedTests >= totalTests * (1 - allowedPartOfErrorTests) && passedTests >= minNumberOfPointsForEquality) || (passedTests >= totalTests)
        } else {
            if (useCleverDomain) {
                val domain = DomainPointGenerator(arrayListOf(left, right), baseOperationsDefinitions)
                while (numberOfRemainingTests-- > 0) {
                    val pointI = domain.generateNewPoint()
                    val l = baseOperationsDefinitions.computeExpressionTree(left.cloneWithNormalization(pointI, sorted = false))
                            .value.toDoubleOrNull() ?: continue
                    val r = baseOperationsDefinitions.computeExpressionTree(right.cloneWithNormalization(pointI, sorted = false))
                            .value.toDoubleOrNull() ?: continue
                    if (justInDomainsIntersection && (l.isNaN() != r.isNaN())) {
                        return false
                    } else if (!l.isFinite() || !r.isFinite()) { //todo (optimization: identify this cases with help of computing just domain: if point not in domain - fall here, if in domain - continue data value computation using domain parts values)
                        numberOfRemainingTests += testWithUndefinedResultIncreasingCoef
                    } else when (comparisonType) {
                        ComparisonType.LEFT_MORE_OR_EQUAL -> if (l < r) return false
                        ComparisonType.LEFT_MORE -> if (l <= r) return false
                        ComparisonType.LEFT_LESS_OR_EQUAL -> if (l > r) return false
                        ComparisonType.LEFT_LESS -> if (l >= r) return false
                    }
                }
                return true
            } else {
                val domain = PointGenerator(baseOperationsDefinitions, arrayListOf(left, right))
                while (numberOfRemainingTests-- > 0) {
                    val pointI = domain.generateNewPoint()
                    val l = baseOperationsDefinitions.computeExpressionTree(left.cloneWithNormalization(pointI, sorted = false))
                            .value.toDoubleOrNull() ?: continue
                    val r = baseOperationsDefinitions.computeExpressionTree(right.cloneWithNormalization(pointI, sorted = false))
                            .value.toDoubleOrNull() ?: continue
                    if (justInDomainsIntersection && (l.isNaN() != r.isNaN())) {
                        return false
                    } else if (!l.isFinite() || !r.isFinite()) { //todo (optimization: identify this cases with help of computing just domain: if point not in domain - fall here, if in domain - continue data value computation using domain parts values)
                        numberOfRemainingTests += testWithUndefinedResultIncreasingCoef
                    } else when (comparisonType) {
                        ComparisonType.LEFT_MORE_OR_EQUAL -> if (l < r) return false
                        ComparisonType.LEFT_MORE -> if (l <= r) return false
                        ComparisonType.LEFT_LESS_OR_EQUAL -> if (l > r) return false
                        ComparisonType.LEFT_LESS -> if (l >= r) return false
                    }
                }
                return true
            }
        }
    }

    fun compareWithoutSubstitutions(l: ExpressionNode, r: ExpressionNode,
                                    comparisonType: ComparisonType = compiledConfiguration.comparisonSettings.defaultComparisonType,
                                    definedFunctionNameNumberOfArgs: Set<String> = definedFunctionNameNumberOfArgsSet,
                                    justInDomainsIntersection: Boolean = compiledConfiguration.comparisonSettings.justInDomainsIntersection): Boolean {
        if (compareAsIs(l, r, withBracketUnification = false))
            return true
        val left = l.clone()
        val right = r.clone()
        left.applyAllImmediateSubstitutions(compiledConfiguration)
        right.applyAllImmediateSubstitutions(compiledConfiguration)
        left.applyAllSubstitutions(compiledConfiguration.compiledFunctionDefinitions)
        right.applyAllSubstitutions(compiledConfiguration.compiledFunctionDefinitions)
        left.normalizeSubTree(sorted = true)
        right.normalizeSubTree(sorted = true)
        if (compiledConfiguration.comparisonSettings.compareExpressionsWithProbabilityRulesWhenComparingExpressions &&
                !left.isBoolExpression(compiledConfiguration.functionConfiguration.boolFunctions) && !left.isBoolExpression(compiledConfiguration.functionConfiguration.boolFunctions)) {
            val functionIdentifierToVariableMap = mutableMapOf<ExpressionNode, String>()
            left.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgs, this)
            right.replaceNotDefinedFunctionsOnVariables(functionIdentifierToVariableMap, definedFunctionNameNumberOfArgs, this)
            return probabilityTestComparison(left, right, comparisonType, justInDomainsIntersection = justInDomainsIntersection)
        }
        return compareAsIs (left, right, withBracketUnification = true)
    }

    fun compareWithTreeTransformationRules(leftOriginal: ExpressionNode, rightOriginal: ExpressionNode, transformations: Collection<ExpressionSubstitution>,
                                           maxTransformationWeight: Double = compiledConfiguration.comparisonSettings.maxExpressionTransformationWeight,
                                           maxBustCount: Int = compiledConfiguration.comparisonSettings.maxExpressionBustCount,
                                           minTransformationWeight: Double = transformations.minBy { it.weight }?.weight
                                                   ?: 1.0,
                                           expressionChainComparisonType: ComparisonType = ComparisonType.EQUAL,
                                           maxDistBetweenDiffSteps: Double = 1.0): Boolean {
        val resultForOperandsInOriginalOrder = compareWithTreeTransformationRulesInternal(leftOriginal, rightOriginal, transformations, maxTransformationWeight,
                maxBustCount, minTransformationWeight, expressionChainComparisonType, false, maxDistBetweenDiffSteps = maxDistBetweenDiffSteps)
        if (resultForOperandsInOriginalOrder) {
            return true
        } else {
            val resultForOperandsInSortedOrder = compareWithTreeTransformationRulesInternal(leftOriginal, rightOriginal, transformations, maxTransformationWeight,
                    maxBustCount, minTransformationWeight, expressionChainComparisonType, true, maxDistBetweenDiffSteps = maxDistBetweenDiffSteps)
            return resultForOperandsInSortedOrder
        }

    }

    fun compareWithTreeTransformationRulesInternal(leftOriginal: ExpressionNode, rightOriginal: ExpressionNode, transformations: Collection<ExpressionSubstitution>,
                                                   maxTransformationWeight: Double = compiledConfiguration.comparisonSettings.maxExpressionTransformationWeight,
                                                   maxBustCount: Int = compiledConfiguration.comparisonSettings.maxExpressionBustCount,
                                                   minTransformationWeight: Double = transformations.minBy { it.weight }?.weight
                                                           ?: 1.0,
                                                   expressionChainComparisonType: ComparisonType = ComparisonType.EQUAL,
                                                   sortOperands: Boolean = false,
                                                   maxDistBetweenDiffSteps: Double = 1.0): Boolean {
        val left = if (sortOperands) leftOriginal.cloneWithSortingChildrenForExpressionSubstitutionComparison() else leftOriginal.clone()
        val right = if (sortOperands) rightOriginal.cloneWithSortingChildrenForExpressionSubstitutionComparison() else rightOriginal.clone()
        left.applyAllImmediateSubstitutions(compiledConfiguration)
        right.applyAllImmediateSubstitutions(compiledConfiguration)
        definedFunctionNameNumberOfArgsSet = if (maxTransformationWeight < 0.5) {
            compiledConfiguration.noTransformationDefinedFunctionNameNumberOfArgsSet
        } else {
            compiledConfiguration.definedFunctionNameNumberOfArgsSet
        } //set as global class instance parameter because it also mast be used for function arguments comparison
        if (compareWithoutSubstitutions(left, right, expressionChainComparisonType)) return true
        if (maxTransformationWeight < minTransformationWeight) return false
        if (left.containsDifferentiation() || right.containsDifferentiation()){
            var leftDiffWeight = (listOf(0.0) + if (maxDistBetweenDiffSteps > unlimitedWeight) listOf(maxDistBetweenDiffSteps) else emptyList()).toMutableList()
            var rightDiffWeight = (listOf(0.0) + if (maxDistBetweenDiffSteps > unlimitedWeight) listOf(maxDistBetweenDiffSteps) else emptyList()).toMutableList()
            val leftDiff = left.diff(leftDiffWeight, compiledConfiguration)
            val rightDiff = right.diff(rightDiffWeight, compiledConfiguration)
            if (abs(leftDiffWeight[0] - rightDiffWeight[0]) < maxDistBetweenDiffSteps &&
                    compareWithoutSubstitutions(leftDiff, rightDiff, expressionChainComparisonType)) {
                return true
            }
        }
        val functionsInExpression = left.getContainedFunctions() + right.getContainedFunctions()
        for (originalTransformation in transformations.filter { it.weight <= maxTransformationWeight }) {
            if (!originalTransformation.basedOnTaskContext && //taskContextRules contains quite small count of rules and its applicability depends not only on functions
                    originalTransformation.leftFunctions.isNotEmpty() &&
                    functionsInExpression.intersect(originalTransformation.leftFunctions).isEmpty()){ //fast check to indicate that rule is not applicable
                continue
            }
            val transformation = if (sortOperands) {
                ExpressionSubstitution(originalTransformation.left.cloneWithSortingChildrenForExpressionSubstitutionComparison(),
                        originalTransformation.right.cloneWithSortingChildrenForExpressionSubstitutionComparison(),
                        originalTransformation.weight,
                        originalTransformation.basedOnTaskContext,
                        originalTransformation.name,
                        originalTransformation.comparisonType)
            } else {
                originalTransformation
            }
            val l = if (sortOperands) left.cloneWithSortingChildrenForExpressionSubstitutionComparison() else left.clone()
            val r = if (sortOperands) right.cloneWithSortingChildrenForExpressionSubstitutionComparison() else right.clone()
            val direction = getComparingDirection(expressionChainComparisonType, transformation.comparisonType)
            if (direction == null) {
                log.add(expressionChainComparisonType.string, transformation.comparisonType.string, {
                    "$comparisonTypesConflict '"
                }, { "' in expression vs '" }, { "' in rule. MSG_CODE_" }, messageType = MessageType.USER)
            }
            val substitutionPlaces = if (direction != SubstitutionDirection.RIGHT_TO_LEFT) {
                transformation.findAllPossibleSubstitutionPlaces(l)
            } else {
                listOf<SubstitutionPlace>()
            } +
                    if (direction != SubstitutionDirection.LEFT_TO_RIGHT) {
                        transformation.findAllPossibleSubstitutionPlaces(r)
                    } else {
                        listOf<SubstitutionPlace>()
                    }
            val bitMaskCount = 1 shl substitutionPlaces.size
            if (bitMaskCount * transformations.size > maxBustCount) {
                transformation.applySubstitution(substitutionPlaces)
                if (compareWithTreeTransformationRulesInternal(l, r, transformations, maxTransformationWeight - transformation.weight,
                                maxBustCount, minTransformationWeight, expressionChainComparisonType, sortOperands))
                    return true
            } else {
                for (bitMask in 1 until bitMaskCount) {
                    transformation.applySubstitutionByBitMask(substitutionPlaces, bitMask)
                    if (compareWithTreeTransformationRulesInternal(l.clone(), r.clone(), transformations, maxTransformationWeight - transformation.weight,
                                    maxBustCount, minTransformationWeight, expressionChainComparisonType, sortOperands))
                        return true
                }
            }
        }
        return false
    }

    fun fullExpressionsCompare(left: ExpressionNode, right: ExpressionNode,
                               expressionChainComparisonType: ComparisonType = ComparisonType.EQUAL): Boolean {
        left.applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        right.applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        if (compiledConfiguration.comparisonSettings.isComparisonWithRules) {
            if (compareWithTreeTransformationRulesInternal(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules,
                            compiledConfiguration.comparisonSettings.maxTransformationWeight, compiledConfiguration.comparisonSettings.maxBustCount, expressionChainComparisonType = expressionChainComparisonType)) return true
            baseOperationsDefinitions.computeExpressionTree(left.children[0])
            baseOperationsDefinitions.computeExpressionTree(right.children[0])
            if (compareWithTreeTransformationRulesInternal(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules,
                            compiledConfiguration.comparisonSettings.maxTransformationWeight, compiledConfiguration.comparisonSettings.maxBustCount, expressionChainComparisonType = expressionChainComparisonType)) return true
        } else {
            if (compareWithoutSubstitutions(left, right)) return true
        }
        return false
    }
}

fun getComparingDirection(expressionChainComparisonType: ComparisonType, transformationComparisonType: ComparisonType): SubstitutionDirection? {
    return when (expressionChainComparisonType) {
        ComparisonType.EQUAL -> if (transformationComparisonType == ComparisonType.EQUAL) SubstitutionDirection.ALL_TO_ALL else null
        ComparisonType.LEFT_MORE_OR_EQUAL -> when (transformationComparisonType) {
            ComparisonType.EQUAL -> SubstitutionDirection.ALL_TO_ALL
            ComparisonType.LEFT_MORE_OR_EQUAL, ComparisonType.LEFT_MORE -> SubstitutionDirection.LEFT_TO_RIGHT
            ComparisonType.LEFT_LESS_OR_EQUAL, ComparisonType.LEFT_LESS -> SubstitutionDirection.RIGHT_TO_LEFT
        }
        ComparisonType.LEFT_LESS_OR_EQUAL -> when (transformationComparisonType) {
            ComparisonType.EQUAL -> SubstitutionDirection.ALL_TO_ALL
            ComparisonType.LEFT_MORE_OR_EQUAL, ComparisonType.LEFT_MORE -> SubstitutionDirection.RIGHT_TO_LEFT
            ComparisonType.LEFT_LESS_OR_EQUAL, ComparisonType.LEFT_LESS -> SubstitutionDirection.LEFT_TO_RIGHT
        }
        ComparisonType.LEFT_MORE -> when (transformationComparisonType) {
            ComparisonType.LEFT_MORE -> SubstitutionDirection.LEFT_TO_RIGHT
            ComparisonType.LEFT_LESS -> SubstitutionDirection.RIGHT_TO_LEFT
            else -> null
        }
        ComparisonType.LEFT_LESS -> when (transformationComparisonType) {
            ComparisonType.LEFT_MORE -> SubstitutionDirection.RIGHT_TO_LEFT
            ComparisonType.LEFT_LESS -> SubstitutionDirection.LEFT_TO_RIGHT
            else -> null
        }
    }
}