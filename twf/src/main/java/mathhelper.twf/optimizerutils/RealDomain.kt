package mathhelper.twf.optimizerutils

import mathhelper.twf.baseoperations.BaseOperationsComputation
import mathhelper.twf.baseoperations.BaseOperationsDefinitions
import mathhelper.twf.baseoperations.ComputationType
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.NodeType
import mathhelper.twf.numbers.Complex
import mathhelper.twf.numbers.toComplex
import mathhelper.twf.platformdependent.random
import mathhelper.twf.standartlibextensions.abs
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

data class VariableInfo(
        val name: String,
        var value: Double = 0.0
) {
    fun clone(): VariableInfo {
        return VariableInfo(name, value)
    }
}

data class CompressedNode(val func: String = "", var value: Complex = "0".toComplex()) {
    var subtreeValue = value
    var children = mutableListOf<CompressedNode>()

    var functor: ((MutableList<Complex>) -> Complex)? = null

    init {
        if (func.isNotEmpty()) {
            val baseFunctions = BaseOperationsComputation(ComputationType.COMPLEX).baseComputationComplex
            if (baseFunctions.contains(func))
                functor = baseFunctions[func]
            else
                throw Throwable("invalid function $func")
        }
    }
}


class DomainPointGenerator(
        _expressions: ArrayList<ExpressionNode> = arrayListOf(),
        val baseOperationsDefinitions: BaseOperationsDefinitions = BaseOperationsDefinitions(),

        randomPointCount: Int = 10,
        goodEnought: Int = 5,

        annealingRuns: Int = 2,
        private val annealingAttemptsCount: Int = 2,
        private val annealingIterationCount: Int = (1000).toInt(),
        private val annealingTemperatureMultiplier: Double = 0.99,
        private val annealingPenaltyRestrictionMultiplier: Double = 1000.0,

        private val gradientDescentRuns: Int = 1,
        private val gradientDescentIterationCount: Int = 50,
        private val gradientDescentTernarySearchIterationCount: Int = 13,
        private val gradientDescentTernarySearchOrderStep: Double = 1000.0,
        private val gradientDescentTernarySearchOrderIterCount: Int = 3,
        private val gradientDescentDelta: Double = 0.001
) {
    private var variablesList = mutableListOf<VariableInfo>()
    private val pointsInDomain = mutableListOf<List<VariableInfo>>()
    private var maxConstant = 1.0

    private var pointsFoundUsingAnnealing = 0
    private var pointsFoundUsingGradientDescent = 0

    val epsilon = 1e-6
    val baseOperationsComputationComplex = BaseOperationsComputation(ComputationType.COMPLEX)

    private val treeNodes = mutableListOf<CompressedNode>()
    private var nodesWithVariables = mutableListOf<Pair<Int, Int>>()

    init {
        val variablesNamesList = mutableListOf<String>()
        for (expression in _expressions) {
            variablesNamesList.addAll(expression.getVariableNames())
            maxConstant = maxOf(expression.getMaxConstant(), maxConstant)
        }
        variablesNamesList.forEach { variablesList.add(VariableInfo(it)) }
        for (expression in _expressions)
            findAllVariables(expression)
        treeNodes.reverse()
        for (attempt in 0 until randomPointCount) {
            variablesList.forEach { it.value = random(-maxConstant, maxConstant) }
            if (calculatePenalty(variablesList) < epsilon)
                pointsInDomain.add(clone(variablesList))
        }
        if (pointsInDomain.size < goodEnought) {
            findPoints(annealingRuns)
        }
    }

    fun findPoints(annealingRuns: Int) {
        for (attempt in 0 until annealingRuns) {
            variablesList.forEach { it.value = random(-maxConstant, maxConstant) }
            var foundSomething = false
            for (i in 0 until annealingAttemptsCount) {
                if (runAnnealing(variablesList, annealingTemperatureMultiplier, annealingIterationCount)) {
                    pointsFoundUsingAnnealing++
                    pointsInDomain.add(clone(variablesList))
                    foundSomething = true
                    break
                }
            }
            if (!foundSomething) {
                for (run in 0 until gradientDescentRuns) {
                    if (runGradientDescent(
                                    variablesList,
                                    gradientDescentIterationCount,
                                    gradientDescentTernarySearchIterationCount,
                                    gradientDescentTernarySearchOrderStep,
                                    gradientDescentTernarySearchOrderIterCount,
                                    gradientDescentDelta
                            )) {
                        pointsFoundUsingGradientDescent++
                        pointsInDomain.add(clone(variablesList))
                        break
                    }
                }
            }
        }
    }

    fun hasPointsInDomain() = pointsInDomain.isNotEmpty()
    fun foundPointsCount() = pointsInDomain.size

    fun getResult(): String {
        println("points found using:\nAnnealing ${pointsFoundUsingAnnealing}\nGradient descent ${pointsFoundUsingGradientDescent}")
        var result = "points found: ${pointsInDomain.size}\n"
        if (pointsInDomain.isNotEmpty()) {
            for (vari in pointsInDomain.get(0)) {
                result += "${vari.name} = ${vari.value}\n"
            }
        }
        return result
    }

    fun hasVariables() = variablesList.size > 0

    fun generateSimplePoints() : List<MutableMap<String, String>> {
        val points = mutableListOf<MutableMap<String, String>>()
        for (zeroIndex in 0..variablesList.size) {
            val point = mutableMapOf<String, String>()
            for (i in 0..variablesList.lastIndex) {
                point[variablesList[i].name] = if (i == zeroIndex) "0" else "1"
            }
            points.add(point)
        }
        for (oneIndex in 0..variablesList.size) {
            val point = mutableMapOf<String, String>()
            for (i in 0..variablesList.lastIndex) {
                point[variablesList[i].name] = if (i == oneIndex) "1" else "-1"
            }
            points.add(point)
        }
        for (minusOneIndex in 0..variablesList.size) {
            val point = mutableMapOf<String, String>()
            for (i in 0..variablesList.lastIndex) {
                point[variablesList[i].name] = if (i == minusOneIndex) "-1" else "0"
            }
            points.add(point)
        }
        val encreasingPoint = mutableMapOf<String, String>()
        var currentVal = 1.0
        for (i in 0..variablesList.lastIndex) {
            encreasingPoint[variablesList[i].name] = currentVal.toString()
            currentVal *= 10000
        }
        points.add(encreasingPoint)

        val decreasingPoint = mutableMapOf<String, String>()
        for (i in 0..variablesList.lastIndex) {
            encreasingPoint[variablesList[i].name] = currentVal.toString()
            currentVal /= 10000
        }
        points.add(encreasingPoint)

        val encreasingSmallPoint = mutableMapOf<String, String>()
        currentVal = 0.00001
        for (i in 0..variablesList.lastIndex) {
            encreasingSmallPoint[variablesList[i].name] = currentVal.toString()
            currentVal *= 10000
        }
        points.add(encreasingSmallPoint)

        val decreasingSmallPoint = mutableMapOf<String, String>()
        for (i in 0..variablesList.lastIndex) {
            decreasingSmallPoint[variablesList[i].name] = currentVal.toString()
            currentVal /= 10000
        }
        points.add(decreasingSmallPoint)
        return points
    }

    fun generateNewPoint(domainArea: Double = 1e-5, randomPointTries: Int = 10, centreID: Int = -1): MutableMap<String, String> {
        for (iterNumber in 0 until randomPointTries) {
            for (variable in variablesList) {
                variable.value = random(-2 * maxConstant, 2 * maxConstant)
            }
            if (calculatePenalty(variablesList) < epsilon)
                return variablesListToMap(variablesList)
        }
        if (pointsInDomain.isNotEmpty()) {
            val pointIndex = if (centreID == -1) random.nextInt(0, pointsInDomain.size) else centreID
            return variablesListToMap(pointsInDomain[pointIndex])
        } else {
            val result = mutableMapOf<String, String>()
            for (variable in variablesList)
                result[variable.name] = random(-maxConstant, maxConstant).toString()
            return result
        }
    }

    fun variablesListToMap(variableList: List<VariableInfo>): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()
        for (variable in variableList) {
            result[variable.name] = variable.value.toString()
        }
        return result
    }

    private fun clone(variableList: List<VariableInfo>): List<VariableInfo> {
        val values = mutableListOf<VariableInfo>()
        for (variable in variableList)
            values.add(variable.clone())
        return values
    }

    private fun runGradientDescent(
            variableList: List<VariableInfo>,
            iterationCount: Int,
            ternarySearchIterationCount: Int,
            ternarySearchOrderStep: Double,
            ternarySearchOrderIterationCount: Int,
            delta: Double
    ): Boolean {
        var currentPenalty = calculatePenalty(variableList)
        if (currentPenalty < epsilon)
            return true
        for (iterNumber in 0 until iterationCount) {
            val direction = calculateDirection(variableList, delta, currentPenalty)
            if (ternarySearchIterationCount > 0) {
                currentPenalty = runTernarySearch(
                        variableList,
                        direction,
                        ternarySearchIterationCount,
                        ternarySearchOrderStep,
                        ternarySearchOrderIterationCount
                )
            } else {
                for (i in 0 until variableList.size)
                    variableList[i].value += direction[i]
                currentPenalty = calculatePenalty(variableList)
            }
            if (currentPenalty < epsilon)
                return true
        }
        return false
    }

    private fun runTernarySearch(
            variableList: List<VariableInfo>,
            direction: ArrayList<Double>,
            iterationCount: Int,
            orderStep: Double,
            orderIterationCount: Int
    ): Double {
        var curOrder = 1.0
        var optimalMultiplier = 1.0
        var minPenalty = substituteDirectionMultiplier(1.0, variableList, direction)
        for (iter in 0 until orderIterationCount) {
            curOrder *= orderStep
            val penalty = substituteDirectionMultiplier(curOrder, variableList, direction)
            if (penalty < minPenalty) {
                minPenalty = penalty
                optimalMultiplier = curOrder
            }
        }
        curOrder = 1.0
        for (iter in 0 until orderIterationCount) {
            curOrder /= orderStep
            val penalty = substituteDirectionMultiplier(curOrder, variableList, direction)
            if (penalty < minPenalty) {
                minPenalty = penalty
                optimalMultiplier = curOrder
            }
        }
        var leftBorder = optimalMultiplier / sqrt(orderStep)
        var rightBorder = sqrt(orderStep) * optimalMultiplier
        var penaltyLeft: Double? = null
        var penaltyRight: Double? = null
        for (iterNumber in 0 until iterationCount) {
            if (minPenalty < epsilon)
                break
            val (med1, med2) = generateMedians(leftBorder, rightBorder)
            if (penaltyLeft == null) {
                penaltyLeft = substituteDirectionMultiplier(med1, variableList, direction)
                if (penaltyLeft < minPenalty) {
                    minPenalty = penaltyLeft
                    optimalMultiplier = med1
                }
            }
            if (penaltyRight == null) {
                penaltyRight = substituteDirectionMultiplier(med2, variableList, direction)
                if (penaltyRight < minPenalty) {
                    minPenalty = penaltyRight
                    optimalMultiplier = med2
                }
            }
            if (penaltyLeft < penaltyRight) {
                rightBorder = med2
                penaltyRight = penaltyLeft
                penaltyLeft = null
            } else {
                leftBorder = med1
                penaltyLeft = penaltyRight
                penaltyRight = null
            }
        }
        for (i in variableList.indices)
            variableList[i].value += optimalMultiplier * direction[i]
        return minPenalty
    }

    private fun substituteDirectionMultiplier(coef: Double, variableList: List<VariableInfo>, direction: ArrayList<Double>): Double {
        for (i in variableList.indices)
            variableList[i].value += coef * direction[i]
        val penalty = calculatePenalty(variableList)
        for (i in variableList.indices)
            variableList[i].value -= coef * direction[i]
        return penalty
    }

    private fun generateMedians(left: Double, right: Double): Pair<Double, Double> {
        val phi = (3 - sqrt(5.0)) / 2
        return Pair(left + (right - left) * phi, right - (right - left) * phi)
    }

    private fun calculateDirection(
            variableList: List<VariableInfo>,
            delta: Double,
            currentPenalty: Double
    ): ArrayList<Double> {
        val direction = ArrayList<Double>()
        for (variable in variableList) {
            val dx = max(abs(variable.value), 1.0) * delta
            variable.value += dx
            direction.add(-(calculatePenalty(variableList) - currentPenalty) / dx)
            variable.value -= dx
        }
        return direction
    }

    private fun runAnnealing(
            variableList: List<VariableInfo>,
            temperatureMultiplier: Double,
            iterationCount: Int
    ): Boolean {
        var currentTemperature = 1.0
        var currentPenalty = calculatePenalty(variablesList)
        if (variableList.isEmpty())
            return currentPenalty < epsilon
        if (currentPenalty < epsilon)
            return true
        for (iterNumber in 0 until iterationCount) {
            val newPoint = mutableListOf<VariableInfo>()
            for (variable in variableList)
                newPoint.add(variable.clone())
            val varIndex = random.nextInt(0, newPoint.size)
            newPoint[varIndex] = generateNextValue(newPoint[varIndex], currentPenalty)
            val newPenalty = calculatePenalty(newPoint)
            if (newPointGoodEnough(currentPenalty, newPenalty, currentTemperature)) {
                currentPenalty = newPenalty
                for (i in newPoint.indices)
                    variableList[i].value = newPoint[i].value
                if (currentPenalty < epsilon)
                    return true
            }
            currentTemperature *= temperatureMultiplier
        }
        return false
    }

    private fun newPointGoodEnough(currentPenalty: Double, newPenalty: Double, temperature: Double): Boolean {
        return currentPenalty > newPenalty || random(0.0, 1.0) < exp((currentPenalty - newPenalty) / temperature) * 0.5
    }


    private fun calculatePenalty(variableList: List<VariableInfo>): Double {
        for ((nodeInd, valInd) in nodesWithVariables)
            treeNodes[treeNodes.size - 1 - nodeInd].value = variableList[valInd].value.toComplex()
        return baseOperationsComputationComplex.computePenalty(treeNodes as ArrayList<CompressedNode>)
    }

    private fun generateNextValue(variable: VariableInfo, currentPenalty: Double): VariableInfo {
        var step = maxOf(1.0, variable.value.abs() / 10, currentPenalty / annealingPenaltyRestrictionMultiplier)
        step = minOf(step, currentPenalty * annealingPenaltyRestrictionMultiplier)
        variable.value = random(variable.value - step, variable.value + step)
        return variable
    }

    private fun findAllVariables(node: ExpressionNode): Int {
        val ind = treeNodes.size
        if (node.nodeType == NodeType.FUNCTION) {
            treeNodes.add(CompressedNode(func = node.value))
            for (child in node.children) {
                val childIndex = findAllVariables(child)
                treeNodes[ind].children.add(treeNodes[childIndex])
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
                treeNodes.add(CompressedNode())
            else
                treeNodes.add(CompressedNode(value = node.value.toComplex()))
        }
        return ind
    }
}