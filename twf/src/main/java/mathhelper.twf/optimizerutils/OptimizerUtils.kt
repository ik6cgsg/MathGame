package mathhelper.twf.optimizerutils

import mathhelper.twf.baseoperations.*
import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.NodeType
import mathhelper.twf.expressiontree.buildDiffNode
import mathhelper.twf.expressiontree.diff
import kotlin.math.max
import kotlin.math.min


data class CompressedNodeDouble(val func: String = "", var value: Double = 0.0) {
    var children = mutableListOf<CompressedNodeDouble>()

    var functor: ((MutableList<Double>) -> Double)? = null

    init {
        if (func.isNotEmpty()) {
            val baseFunctions = BaseOperationsComputation(ComputationType.DOUBLE).baseComputationDouble
            if (baseFunctions.contains(func))
                functor = baseFunctions[func]
            else
                throw Throwable("invalid function $func")
        }
    }
}

class OptimizerUtils(
        val expression: ExpressionNode,
        baseOperationsDefinitions: BaseOperationsDefinitions = BaseOperationsDefinitions(),
        val compiledConfiguration: CompiledConfiguration = CompiledConfiguration(),
        domain: DomainPointGenerator? = null
) {
    private val baseOperationsComputationDouble = BaseOperationsComputation(ComputationType.DOUBLE)

    private val domainPointGenerator = if (domain != null) domain else DomainPointGenerator(arrayListOf(expression), baseOperationsDefinitions)

    private var variablesList = mutableListOf<VariableInfo>()

    private val treeNodes = mutableListOf<CompressedNodeDouble>()
    private val nodesWithVariables = mutableListOf<Pair<Int, Int>>()
    private val partialDerivativesTreeNodes = mutableListOf<MutableList<CompressedNodeDouble>>()
    private val partialDerivativesNodesWithVariables = mutableListOf<MutableList<Pair<Int, Int>>>()

    init {
        val variablesNamesList = mutableListOf<String>()
        variablesNamesList.addAll(expression.getVariableNames())
        variablesNamesList.forEach { variablesList.add(VariableInfo(it)) }

        for ((name, _) in variablesList) {
            val partialDerivative = buildDiffNode(expression, name, compiledConfiguration).diff(compiledConfiguration = compiledConfiguration)
            partialDerivativesTreeNodes.add(mutableListOf())
            partialDerivativesNodesWithVariables.add(mutableListOf())
            findAllVariables(partialDerivative, partialDerivativesTreeNodes.last(), partialDerivativesNodesWithVariables.last())
            partialDerivativesTreeNodes.last().reverse()
        }
        findAllVariables(expression, treeNodes, nodesWithVariables)
        treeNodes.reverse()
    }

    fun run(
            descentStartPointsCount: Int = compiledConfiguration.gradientDescentComparisonConfiguration.startPointsCount,
            iterationCount: Int = compiledConfiguration.gradientDescentComparisonConfiguration.iterationCount,
            threshold: Double = 0.0
    ): Boolean {
        domainPointGenerator.findPoints(max(0, descentStartPointsCount - domainPointGenerator.foundPointsCount()))
        if (!canStart()) {
            return false
        }
        for (run in 0 until min(descentStartPointsCount, domainPointGenerator.foundPointsCount())) {
            val point = domainPointGenerator.generateNewPoint(centreID = run % domainPointGenerator.foundPointsCount())
            for (variable in variablesList) {
                variable.value = point.get(variable.name)!!.toDouble()
            }
            if (runGradientDescent(iterationCount, threshold)) {
                return true
            }
        }
        return false
    }

    private fun runGradientDescent(iterationCount: Int, threshold: Double): Boolean {
        for (iter in 0 until iterationCount) {
            val direction = calculateDirection(variablesList)
            val lambda = runTernarySearch(
                    variablesList,
                    direction,
                    compiledConfiguration.gradientDescentComparisonConfiguration.ternarySearchLeftBorder,
                    compiledConfiguration.gradientDescentComparisonConfiguration.ternarySearchRightBorder,
                    compiledConfiguration.gradientDescentComparisonConfiguration.ternarySearchIterationCount,
                    threshold
            )
            if (lambda == 0.0) {
                break
            }
            for (i in variablesList.indices) {
                variablesList[i].value += lambda * direction[i]
            }
            val result = calculateValue(variablesList, treeNodes, nodesWithVariables)
            if (result < threshold)
                return true
        }
        return false
    }

    fun canStart() = domainPointGenerator.hasPointsInDomain()

    private fun calculateDirection(
            variableList: List<VariableInfo>
    ): ArrayList<Double> {
        val direction = ArrayList<Double>()
        for (i in variableList.indices) {
            val result = calculateValue(variableList, partialDerivativesTreeNodes[i], partialDerivativesNodesWithVariables[i])
            direction.add(if (!result.isNaN() && result.isFinite()) -result else 0.0)
        }
        return direction
    }

    private fun runTernarySearch(
            variableList: List<VariableInfo>,
            direction: ArrayList<Double>,
            leftBorder: Double,
            rightBorder: Double,
            iterationsCount: Int,
            threshold: Double
    ): Double {
        var left = leftBorder
        var right = rightBorder
        var leftOk = valueAt(left, variableList, direction) != null
        if (!leftOk && valueAt(right, variableList, direction) == null) {
            return 0.0
        }
        val alpha = compiledConfiguration.gradientDescentComparisonConfiguration.ternarySearchAlpha
        val beta = compiledConfiguration.gradientDescentComparisonConfiguration.ternarySearchBeta
        for (iter in 0 until iterationsCount) {
            val m1 = (alpha * left + beta * right) / (alpha + beta)
            val m2 = (beta * left + alpha * right) / (alpha + beta)

            val value1 = valueAt(m1, variableList, direction)
            val value2 = valueAt(m2, variableList, direction)
            if (value1 != null && value1 < threshold)
                return m1
            if (value2 != null && value2 < threshold)
                return m2
            if (value1 == null && value2 == null) {
                right = m1
                continue
            }
            if (value1 == null || (value2 != null && value1 > value2)) {
                left = m1
                leftOk = true
            } else {
                right = m2
            }
        }
        return if (leftOk) left else right
    }

    private fun valueAt(lambda: Double, variableList: List<VariableInfo>, direction: ArrayList<Double>): Double? {
        val variableListCloned = cloneVariableList(variableList)
        for (i in variableList.indices)
            variableListCloned[i].value += direction[i] * lambda
        val result = calculateValue(variableListCloned, treeNodes, nodesWithVariables)
        return if (result.isNaN() || result.isInfinite()) null else result
    }


    private fun cloneVariableList(variablesList: List<VariableInfo>): List<VariableInfo> {
        val values = mutableListOf<VariableInfo>()
        for (variable in variablesList)
            values.add(variable.clone())
        return values
    }

    private fun calculateValue(
            variableList: List<VariableInfo>,
            treeNodes: MutableList<CompressedNodeDouble>,
            nodesWithVariables: MutableList<Pair<Int, Int>>
    ): Double {
        for ((nodeInd, valInd) in nodesWithVariables)
            treeNodes[treeNodes.size - 1 - nodeInd].value = variableList[valInd].value
        baseOperationsComputationDouble.computeValue(treeNodes as ArrayList<CompressedNodeDouble>)
        return treeNodes.last().value
    }

    private fun findAllVariables(
            node: ExpressionNode,
            nodesList: MutableList<CompressedNodeDouble>,
            nodesWithVariables: MutableList<Pair<Int, Int>>
    ): Int {
        val ind = nodesList.size
        if (node.nodeType == NodeType.FUNCTION) {
            nodesList.add(CompressedNodeDouble(func = node.value))
            for (child in node.children) {
                val childIndex = findAllVariables(child, nodesList, nodesWithVariables)
                nodesList[ind].children.add(nodesList[childIndex])
            }
        } else {
            var isVariable = false
            for (i in variablesList.indices) {
                if (variablesList[i].name == node.value) {
                    nodesWithVariables.add(Pair(ind, i))
                    isVariable = true
                }
            }
            if (isVariable)
                nodesList.add(CompressedNodeDouble())
            else
                nodesList.add(CompressedNodeDouble(value = node.value.toDouble()))
        }
        return ind
    }
}

