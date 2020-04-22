package com.twf.baseoperations

import com.twf.expressiontree.ExpressionNode
import com.twf.numbers.Complex
import com.twf.platformdependent.random
import com.twf.standartlibextensions.abs
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.system.exitProcess

data class VariableInfo (
        val name: String,
        var value: Double = 0.0
) {
    fun clone() : VariableInfo {
        return VariableInfo(name, value)
    }
}


class DomainPointGenerator (
        val expressions: ArrayList<ExpressionNode> = arrayListOf(),
        val baseOperationsDefinitions: BaseOperationsDefinitions = BaseOperationsDefinitions(),

        randomPointCount: Int = 2,
        goodEnought: Int = 1,

        annealingRuns: Int = 2,
        annealingAttemptsCount: Int = 2,
        annealingIterationCount: Int = (1000).toInt(),
        annealingTemperatureMultiplier: Double = 0.99,
        private val annealingPenaltyRestrictionMultiplier: Double = 1000.0,

        gradientDescentRuns: Int = 2,
        gradientDescentIterationCount: Int = 50,
        gradientDescentTernarySearchIterationCount: Int = 13,
        gradientDescentTernarySearchOrderStep: Double = 1000.0,
        gradientDescentTernarySearchOrderIterCount: Int = 3,
        gradientDescentDelta: Double = 0.001
) {
    private var variablesList = mutableListOf<VariableInfo>()
    private val pointsInDomain = mutableListOf<List<VariableInfo>>()
    private var maxConstant = 1.0

    private var pointsFoundUsingAnnealing = 0
    private var pointsFoundUsingGradientDescent = 0

    val epsilon = 1e-9
    val baseOperationsComputationComplex = BaseOperationsComputation(ComputationType.COMPLEX)

    init {
        val variablesNamesList = mutableListOf<String>()
        for (expression in expressions) {
            variablesNamesList.addAll(expression.getVariableNames())
            maxConstant = maxOf(expression.getMaxConstant(), maxConstant)
        }
        variablesNamesList.forEach { variablesList.add(VariableInfo(it)) }
        for (attempt in 0 until randomPointCount) {
            variablesList.forEach { it.value = random(-maxConstant, maxConstant) }
            if (calculatePenalty(variablesList) < epsilon)
                pointsInDomain.add(clone(variablesList))
        }
        if (pointsInDomain.size < goodEnought) {
            for (attempt in 0 until annealingRuns) {
                variablesList.forEach({ it.value = random(-maxConstant, maxConstant) })
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
    }

    fun hasPointsInDomain() = pointsInDomain.isNotEmpty()

    fun getResult() : String {
        println("points found using:\nAnnealing ${pointsFoundUsingAnnealing}\nGradient descent ${pointsFoundUsingGradientDescent}")
        var result = "points found: ${pointsInDomain.size}\n";
        if (pointsInDomain.isNotEmpty()){
            for (vari in pointsInDomain.get(0)){
                result += "${vari.name} = ${vari.value}\n"
            }
        }
        return result
    }

    fun generateNewPoint(domainArea: Double = 1e-5, randomPointTries: Int = 10): MutableMap<String, String> {
        for (iterNumber in 0 until randomPointTries) {
            for (variable in variablesList)
                variable.value = random(-2 * maxConstant, 2 * maxConstant)
            if (calculatePenalty(variablesList) < epsilon)
                return variablesListToMap(variablesList)
        }
        if (pointsInDomain.isNotEmpty()) {
            val pointIndex = random.nextInt(0, pointsInDomain.size)
            return variablesListToMap(pointsInDomain[pointIndex])
        } else {
            val result = mutableMapOf<String, String>()
            for (variable in variablesList)
                result[variable.name] = random(-maxConstant, maxConstant).toString()
            return result
        }
    }

    fun variablesListToMap(variableList: List<VariableInfo>) : MutableMap<String, String> {
        var result = mutableMapOf<String, String>()
        for (variable in variableList)
            result[variable.name] = variable.value.toString()
        return result
    }

    private fun clone(variableList: List<VariableInfo>) : List<VariableInfo> {
        val values = mutableListOf<VariableInfo>()
        for (variable in variablesList)
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
    ) : Boolean {
        var currentPenalty = calculatePenalty(variableList)
        if (currentPenalty < epsilon)
            return true
        for (iterNumber in 0 until iterationCount) {
            val direction = calculateDirection(variableList, delta, currentPenalty)
//            for (i in variableList.indices)
//                println("${variableList[i].name}: ${variableList[i].value}, direction: ${direction[i]}")
//            println()
            if (ternarySearchIterationCount > 0) {
                currentPenalty = runTernarySearch(
                        variableList,
                        direction,
                        ternarySearchIterationCount,
                        ternarySearchOrderStep,
                        ternarySearchOrderIterationCount
                )
            }
            else{
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
        for (iterNumber in 0 until iterationCount){
            if (minPenalty < epsilon)
                break
            val (med1, med2) = generateMedians(leftBorder, rightBorder)
            if (penaltyLeft == null) {
                penaltyLeft = substituteDirectionMultiplier(med1, variableList, direction)
                if(penaltyLeft < minPenalty) {
                    minPenalty = penaltyLeft
                    optimalMultiplier = med1
                }
            }
            if (penaltyRight == null) {
                penaltyRight = substituteDirectionMultiplier(med2, variableList, direction)
                if(penaltyRight < minPenalty) {
                    minPenalty = penaltyRight
                    optimalMultiplier = med2
                }
            }
//            println("left: x = ${variableList[0].value + leftBorder * direction[0]}, val = ${penaltyLeft}")
//            println("right: x = ${variableList[0].value + rightBorder * direction[0]}, val = ${penaltyRight}")
//            println()
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
//        println(optimalMultiplier)
//        println()
        for (i in variableList.indices)
            variableList[i].value += optimalMultiplier * direction[i]
        return minPenalty
    }

    private fun substituteDirectionMultiplier(coef: Double, variableList: List<VariableInfo>, direction: ArrayList<Double>) : Double {
        for (i in variableList.indices)
            variableList[i].value += coef * direction[i]
        val penalty = calculatePenalty(variableList)
        for (i in variableList.indices)
            variableList[i].value -= coef * direction[i]
        return penalty
    }

    private fun generateMedians(left: Double, right: Double) : Pair<Double, Double> {
        val phi = (3 - sqrt(5.0)) / 2
        return Pair(left + (right - left) * phi, right - (right - left) * phi)
    }

    private fun calculateDirection(
            variableList: List<VariableInfo>,
            delta: Double,
            currentPenalty: Double
    ) : ArrayList<Double> {
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
    ) : Boolean {
        var currentTemperature = 1.0
        var currentPenalty = calculatePenalty(variablesList)
        if (variableList.isEmpty())
            return currentPenalty < epsilon
        if (currentPenalty < epsilon)
            return true;
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

    private fun newPointGoodEnough(currentPenalty: Double, newPenalty: Double, temperature: Double) : Boolean {
        return currentPenalty > newPenalty || random(0.0, 1.0) < exp((currentPenalty - newPenalty) / temperature) * 0.5
    }

    private fun calculatePenalty(
            variableList: List<VariableInfo>
    ) : Double {
        var penalty = 0.0
        val point = mutableMapOf<String, String>()
        for (variable in variableList)
            point.put(variable.name, variable.value.toString())
        for (expression in expressions) {
            val (_, currentPenalty) = baseOperationsComputationComplex.computeWithPenalty(
                    expression.cloneWithVariableReplacement(point))
            penalty += currentPenalty
        }
        return penalty
    }

    private fun generateNextValue (variable: VariableInfo, currentPenalty: Double) : VariableInfo {
        var step = maxOf(1.0, variable.value.abs() / 10, currentPenalty / annealingPenaltyRestrictionMultiplier)
        step = minOf(step, currentPenalty * annealingPenaltyRestrictionMultiplier)
        variable.value = random(variable.value - step, variable.value + step)
        return variable
    }
}