package com.twf.expressiontree

import com.twf.numbers.toReal
import com.twf.platformdependent.abs
import kotlin.math.pow

data class SimpleComputationRuleParams(
    val isIncluded: Boolean,
    val operationsMap: Map<String, (List<Double>) -> Double?> = mapOf(
        "+" to {args -> plus(args)},
        "-" to {args -> minus(args)},
        "*" to {args -> mul(args)},
        "/" to {args -> div(args)},
        "^" to {args -> pow(args)},
        "log" to {args -> log(args)}
    )
)

val simpleComputationRuleParamsDefault = SimpleComputationRuleParams(true)

fun ExpressionNode.calcComplexity (): Int {
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

fun ExpressionNode.computeNodeIfSimple (simpleComputationRuleParams: SimpleComputationRuleParams = simpleComputationRuleParamsDefault): Double? {
    if (nodeType == NodeType.VARIABLE) {
        return value.toDoubleOrNull()
    } else if (children.isEmpty()) {
        return null
    }

    val listOfArgs = mutableListOf<Double>()
    for (childNode in children) {
        childNode.computeNodeIfSimple(simpleComputationRuleParams)?.let { listOfArgs.add(it) } ?: return null
    }

    return simpleComputationRuleParams.operationsMap[value]?.invoke(listOfArgs)
}

private fun inZ (value: Double) = (value.toInt() - value).toReal().additivelyEqualToZero()

private fun roundNumber (number: Double): Double {
    var current = abs(number)
    var leftIterations = 10
    while (!current.toReal().additivelyEqualToZero() && inZ(current) && leftIterations > 0){
        current /= 10
        leftIterations--
    }
    leftIterations = 10
    while (!inZ(current) && leftIterations > 0){
        current *= 10
        leftIterations--
    }
    return current
}

private fun plus (args: List<Double>): Double? {
    if (args.size == 2 && args.any { (roundNumber(it) - 1.0).toReal().additivelyEqualToZero() }){
        return args.sum()
    }

    if (args.any { roundNumber(it) > 200 } && args.size > 2) {
        return null
    }
    val result = args.sum()
    if (roundNumber(result) > 300) {
        return null
    }
    return result
}

private fun minus (args: List<Double>): Double? {
    if (args.size != 1) {
        return null
    }
    return -args.first()
}

private fun mul (args: List<Double>): Double? {
    if (args.size == 2 && args.any { (roundNumber(it) - 2.0).toReal().additivelyEqualToZero() }){
        return args.first() * args.last()
    }

    if (args.any { roundNumber(it) > 50 }) {
        return null
    }
    var result = 1.0
    for (arg in args) {
        result *= arg
    }
    if (roundNumber(result) > 100) {
        return null
    }
    return result
}

private fun div (args: List<Double>): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > 100 || roundNumber(args.last()) > 50 || args.last() == 0.0) {
        return null
    }
    val result = args.first() / args.last()
    if (roundNumber(result) > 100) {
        return null
    }
    return result
}

private fun pow (args: List<Double>): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > 400 || roundNumber(args.last()) > 10) {
        return null
    }
    val result = args.first().pow(args.last())
    if (!result.isFinite() || roundNumber(result) > 400) {
        return null
    }
    return result
}

private fun log (args: List<Double>): Double? {
    if (args.size != 2) {
        return null
    }
    if (roundNumber(args.first()) > 400 || roundNumber(args.last()) > 400) {
        return null
    }
    val result = kotlin.math.log(args.first(), args.last())
    if (!result.isFinite() || roundNumber(result) > 400 || (roundNumber(result) >= 1 && !inZ(result))) {
        return null
    }
    return result
}

private fun sin (args: List<Double>): Double? {
    if (args.size != 1) {
        return null
    }
    val result = kotlin.math.sin(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 0.5).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero() ) {
        return null
    }
    return result
}

private fun cos (args: List<Double>): Double? {
    if (args.size != 1) {
        return null
    }
    val result = kotlin.math.cos(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 0.5).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero() ) {
        return null
    }
    return result
}

private fun tg (args: List<Double>): Double? {
    if (args.size != 1) {
        return null
    }
    val result = kotlin.math.tan(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero() ) {
        return null
    }
    return result
}

private fun ctg (args: List<Double>): Double? {
    if (args.size != 1) {
        return null
    }
    val result = 1 / kotlin.math.tan(args.first())
    if (!result.isFinite() || roundNumber(result).toReal().additivelyEqualToZero() || (roundNumber(result) - 1.0).toReal().additivelyEqualToZero() ) {
        return null
    }
    return result
}

