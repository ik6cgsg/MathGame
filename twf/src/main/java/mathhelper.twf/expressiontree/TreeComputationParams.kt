package mathhelper.twf.expressiontree

import mathhelper.twf.numbers.toReal
import kotlin.math.abs
import kotlin.math.pow

const val TREE_COMPUTATION_RULES_PRIORITY = 5
const val TREE_COMPUTATION_DEFAULT = "SimpleComputation"


data class SimpleComputationRuleParams(
        var ruleCodes: Set<String> = setOf(),
        var isIncluded: Boolean = true,
        var maxCalcComplexity: Int = 5,
        var maxTenPowIterations: Int = 10,
        var maxPlusArgRounded: Int = 200,
        var maxMulArgRounded: Int = 50,
        var maxDivBaseRounded: Int = 400,
        var maxPowBaseRounded: Int = 50,
        var maxPowDegRounded: Int = 10,
        var maxLogBaseRounded: Int = 400,
        var maxResRounded: Int = 400,
        var operationsMap: Map<String, (List<Double>) -> Double?> = mapOf()
) {
    init {
        operationsMap = mapOf(
                "+" to { args -> plus(args, this) },
                "-" to { args -> minus(args, this) },
                "*" to { args -> mul(args, this) },
                "/" to { args -> div(args, this) },
                "^" to { args -> pow(args, this) },
                "log" to { args -> log(args, this) }
        )

        if (ruleCodes.isNotEmpty()) {
            if (ruleCodes.contains("SimpleComputationBeforeSchool")) {
                maxCalcComplexity = 2
                maxTenPowIterations = 3
                maxPlusArgRounded = 1
                maxMulArgRounded = 1
                maxDivBaseRounded = 1
                maxPowBaseRounded = 1
                maxPowDegRounded = 1
                maxLogBaseRounded = 1

                maxResRounded = 10

            } else if (ruleCodes.contains("SimpleComputationYoungSchool")) {
                maxCalcComplexity = 3
                maxTenPowIterations = 5
                maxPlusArgRounded = 100
                maxMulArgRounded = 10
                maxDivBaseRounded = 10
                maxPowBaseRounded = 1
                maxPowDegRounded = 1
                maxLogBaseRounded = 1

                maxResRounded = 100

            } else if (ruleCodes.contains("SimpleComputationMiddleSchool")) {
                maxCalcComplexity = 5
                maxTenPowIterations = 10
                maxPlusArgRounded = 200
                maxMulArgRounded = 50
                maxDivBaseRounded = 400
                maxPowBaseRounded = 50
                maxPowDegRounded = 10
                maxLogBaseRounded = 400

                maxResRounded = 400

            } else if (ruleCodes.contains("SimpleComputationUniversity")) {
                maxCalcComplexity = Int.MAX_VALUE
                maxTenPowIterations = 11
                maxPlusArgRounded = Int.MAX_VALUE
                maxMulArgRounded = Int.MAX_VALUE
                maxDivBaseRounded = Int.MAX_VALUE
                maxPowBaseRounded = Int.MAX_VALUE
                maxPowDegRounded = Int.MAX_VALUE
                maxLogBaseRounded = Int.MAX_VALUE

                maxResRounded = Int.MAX_VALUE
            }
        }
    }
}

val simpleComputationRuleParamsDefault = SimpleComputationRuleParams()

val simpleComputationRuleParamsNoLimits = SimpleComputationRuleParams(setOf("SimpleComputationUniversity"), true)

fun ExpressionNode.calcComplexity(): Int {
    if (nodeType == NodeType.VARIABLE && (value == "1" || value == "0" || (value.toDoubleOrNull() != null && (roundNumber(value.toDouble()) - 1.0).toReal().additivelyEqualToZero()))) {
        return 0
    } else if (children.isEmpty()) {
        return 1
    }

    val nodeComplexity = when (value) {
        "", "-", "+" -> 0
        "*", "/" -> 1
        else -> 2
    } + children.sumBy { it.calcComplexity() }
    return nodeComplexity
}

fun ExpressionNode.computeNodeIfPossible(): Double? {
    val result = computeNodeIfSimpleRecursive(simpleComputationRuleParamsNoLimits) ?: return null
    return result
}

fun ExpressionNode.computeNodeIfSimple(simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    val result = computeNodeIfSimpleRecursive(simpleComputationRuleParams) ?: return null
    return result
}

private fun ExpressionNode.computeNodeIfSimpleRecursive(simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (nodeType == NodeType.VARIABLE) {
        return value.toDoubleOrNull()
    } else if (children.isEmpty()) {
        return null
    }

    val listOfArgs = mutableListOf<Double>()
    for (childNode in children) {
        childNode.computeNodeIfSimpleRecursive(simpleComputationRuleParams)?.let { listOfArgs.add(it) } ?: return null
    }

    return simpleComputationRuleParams.operationsMap[value]?.invoke(listOfArgs)
}

private fun inZ(value: Double) = (value.toInt() - value).toReal().additivelyEqualToZero()

private fun roundNumber(number: Double): Double {
    var current = abs(number)
    var leftIterations = 10
    while (!current.toReal().additivelyEqualToZero() && inZ(current) && leftIterations > 0) {
        current /= 10
        leftIterations--
    }
    leftIterations = 10
    while (!inZ(current) && leftIterations > 0) {
        current *= 10
        leftIterations--
    }
    return current
}

fun tenPowToMakeZ(number: Double, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Int {
    var current = abs(number)
    var iterations = 0
    while (!inZ(current) && iterations < simpleComputationRuleParams.maxTenPowIterations) {
        current *= 10
        iterations++
    }
    return iterations
}

private fun plus(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size == 2 && args.any { (roundNumber(it) - 1.0).toReal().additivelyEqualToZero() }) {
        return args.sum()
    }
    val result = args.sum()
    if (args.count { roundNumber(it) > simpleComputationRuleParams.maxPlusArgRounded } > 1 && roundNumber(result) > simpleComputationRuleParams.maxResRounded) {
        return null
    }
    return result
}

private fun minus(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 1) {
        return null
    }
    return -args.first()
}

private fun mul(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size == 2 && args.any { (roundNumber(it) - 2.0).toReal().additivelyEqualToZero() }) {
        return args.first() * args.last()
    }

    var result = 1.0
    for (arg in args) {
        result *= arg
    }
    if (args.count { roundNumber(it) > simpleComputationRuleParams.maxMulArgRounded } > 1 && roundNumber(result) > simpleComputationRuleParams.maxResRounded) {
        return null
    }
    return result
}

private fun div(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > simpleComputationRuleParams.maxDivBaseRounded || roundNumber(args.last()) > simpleComputationRuleParams.maxMulArgRounded || args.last() == 0.0) {
        return null
    }
    val result = args.first() / args.last()
    return result
}

private fun pow(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 2) {
        return null
    }
    val result = args.first().pow(args.last())
    if (!result.isFinite()) {
        return null
    }
    if (roundNumber(result) > simpleComputationRuleParams.maxResRounded ||
            (roundNumber(args.first()) > simpleComputationRuleParams.maxPowBaseRounded && roundNumber(args.last()) > simpleComputationRuleParams.maxPowDegRounded)) {
        return null
    }
    return result
}

private fun log(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 2) {
        return null
    }
    val result = kotlin.math.log(args.first(), args.last())
    if (!result.isFinite() || (roundNumber(result) >= 1 && !inZ(result))) {
        return null
    }
    if (roundNumber(result) > simpleComputationRuleParams.maxResRounded ||
            (roundNumber(args.first()) > simpleComputationRuleParams.maxPowBaseRounded && roundNumber(args.last()) > simpleComputationRuleParams.maxLogBaseRounded)) {
        return null
    }
    return result
}

private fun sin(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 1) {
        return null
    }
    val result = kotlin.math.sin(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 0.5).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero()) {
        return null
    }
    return result
}

private fun cos(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 1) {
        return null
    }
    val result = kotlin.math.cos(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 0.5).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero()) {
        return null
    }
    return result
}

private fun tg(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 1) {
        return null
    }
    val result = kotlin.math.tan(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero()) {
        return null
    }
    return result
}

private fun ctg(args: List<Double>, simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (args.size != 1) {
        return null
    }
    val result = 1 / kotlin.math.tan(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero()) {
        return null
    }
    return result
}

