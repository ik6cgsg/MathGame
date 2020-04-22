package com.twf.baseoperations

import com.twf.expressiontree.ExpressionNode
import com.twf.numbers.Complex
import com.twf.numbers.toComplex
import com.twf.platformdependent.defaultRandom
import com.twf.standartlibextensions.abs
import kotlin.system.exitProcess

enum class ComputationType {COMPLEX, DOUBLE}

class BaseOperationsComputation(private val computationType: ComputationType) {
    private class FoldedExpression(val nameOfZVariable : String, val from : Double,
                                   val to : Double, val expression: ExpressionNode)

    companion object {
        val epsilon: Double = 11.9e-7
        private fun Double.additivelyEqualTo(number: Double) = ((this - number) in (-epsilon)..epsilon)
        private fun Double.additivelyEqualToZero() = (this in (-epsilon)..epsilon)

        fun additivelyEqual(l: Double, r: Double) = (l == r || l.additivelyEqualTo(r))
    }

    private val baseComputationOperations = mapOf(
            ComputationType.COMPLEX to
                    mapOf(
                            "" to {listOfArgs : List<Any> -> brackets(listOfArgs) },
                            "+" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.plus(args)}
                            },
                            "*" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.mul(args)}
                            },
                            "-" to  {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.minus(args)}
                            },
                            "/" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.div(args)}
                            },
                            "^" to  {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.pow(args)}
                            },
                            "mod" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.mod(args)}
                            },

                            "and" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.and(args)}
                            },
                            "or" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.or(args)}
                            },
                            "xor" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.xor(args)}
                            },
                            "alleq" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.alleq(args)}
                            },
                            "not" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.not(args)}
                            },

                            "sin" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.sin(args)}
                            },
                            "cos" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.cos(args)}
                            },
                            "tg" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.tan(args)}
                            },
                            "sh" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.sinh(args)}
                            },
                            "ch" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.cosh(args)}
                            },
                            "th" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.tanh(args)}
                            },
                            "asin" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.asin(args)}
                            },
                            "acos" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.acos(args)}
                            },
                            "atg" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.atan(args)}
                            },
                            "actg" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.actan(args)}
                            },
                            "pow" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.pow(args)}
                            },
                            "ln" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.ln(args)}
                            },
                            "exp" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.exp(args)}
                            },
                            "abs" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.abs(args)}
                            },
                            "sqrt" to {listOfArgs : List<Any> ->
                                complexFunctionToAny(listOfArgs) { args -> ComplexBaseOperation.sqrt(args)}
                            }
                    ),
            ComputationType.DOUBLE to
                    mapOf(
                            "" to {listOfArgs : List<Any> -> brackets(listOfArgs) },
                            "+" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.plus(args)}
                            },
                            "*" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.mul(args)}
                            },
                            "-" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.minus(args)}
                            },
                            "/" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.div(args)}
                            },
                            "^" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.pow(args)}
                            },
                            "mod" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.mod(args)}
                            },

                            "and" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.and(args)}
                            },
                            "or" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.or(args)}
                            },
                            "xor" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.xor(args)}
                            },
                            "alleq" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.alleq(args)}
                            },
                            "not" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.not(args)}
                            },

                            "sin" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.sin(args)}
                            },
                            "cos" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.cos(args)}
                            },
                            "tg" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.tan(args)}
                            },
                            "sh" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.sinh(args)}
                            },
                            "ch" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.cosh(args)}
                            },
                            "th" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.tanh(args)}
                            },
                            "asin" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.asin(args)}
                            },
                            "acos" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.acos(args)}
                            },
                            "atg" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.atan(args)}
                            },
                            "actg" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.actan(args)}
                            },
                            "pow" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.pow(args)}
                            },
                            "ln" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.ln(args)}
                            },
                            "exp" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.exp(args)}
                            },
                            "abs" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.abs(args)}
                            },
                            "sqrt" to {listOfArgs : List<Any> ->
                                doubleFunctionToAny(listOfArgs) { args -> DoubleBaseOperation.sqrt(args)}
                            }
                    )
    )

    private fun complexFunctionToAny(listOfArgs : List<Any>, baseOperation : (args : List<Complex>) -> Complex) =
            baseOperation(parseComplexNumbers(listOfArgs))
    private fun doubleFunctionToAny(listOfArgs : List<Any>, baseOperation : (args : List<Double>) -> Double) =
            baseOperation(parseDoubleNumbers(listOfArgs))

    fun compute(expressionNode : ExpressionNode) : Any {
        return if (expressionNode.children.isEmpty()) {
            stringToNumber(expressionNode.value)
        } else {
            if (isFoldedExpression(expressionNode)) {
                return calculateFoldedExpression(expressionNode)
            }
            val listOfArgs = mutableListOf<Any>()
            for (childNode in expressionNode.children) {
                listOfArgs.add(compute(childNode))
            }

            if (baseComputationOperations[computationType]!!.containsKey(expressionNode.value)) {
                baseComputationOperations[computationType]!![expressionNode.value]!!.invoke(listOfArgs)
            } else {
                stringToNumber(defaultRandom().toString())
            }
        }
    }

    fun calculatePenalty(value: Any, operatorType: String = "") : Double {
        if (operatorType == "ln" || operatorType == "asin" || operatorType == "acos") {
            val re = value.toString().toComplex().getReal().value.abs()
            val im = value.toString().toComplex().getImaginary().value.abs()
            return re * im
        }
        return value.toString().toComplex().getImaginary().value.abs()
    }

    fun computeWithPenalty(expressionNode : ExpressionNode) : Pair<Any, Double> {
        return if (expressionNode.children.isEmpty()) {
            val value = stringToNumber(expressionNode.value)
            return Pair(value, calculatePenalty(value))
        } else {
            if (isFoldedExpression(expressionNode)) {
                val value = calculateFoldedExpression(expressionNode)
                return Pair(value, calculatePenalty(value))
            }
            var penalty = 0.0
            val listOfArgs = mutableListOf<Any>()
            for (childNode in expressionNode.children) {
                val (value, pen) = computeWithPenalty(childNode)
                penalty += pen
                listOfArgs.add(value)
            }

            if (baseComputationOperations[computationType]!!.containsKey(expressionNode.value)) {
                val value = baseComputationOperations[computationType]!![expressionNode.value]!!.invoke(listOfArgs)
                return Pair(value, penalty + calculatePenalty(value, expressionNode.value))
            } else {
                val value = stringToNumber(defaultRandom().toString())
                return Pair(value, penalty + calculatePenalty(value))
            }
        }
    }

    private fun stringToNumber(arg : String) : Any {
        return when (computationType) {
            ComputationType.DOUBLE -> arg.toDouble()
            ComputationType.COMPLEX -> arg.toComplex()
        }
    }

    private fun isFoldedExpression(expression: ExpressionNode) = expression.value == "P" || expression.value == "S"

    private fun calculateFoldedExpression(expression: ExpressionNode) : Any {
        return when (expression.value) {
            "S" -> sumN(expression)
            "P" -> prodN(expression)
            else -> throw IllegalArgumentException("Given expression's type ${expression.value} is not folded")
        }
    }

    private fun parseComplexNumbers(listOfArgs : List<Any>) : List<Complex> {
        val listOfNumbers = mutableListOf<Complex>()

        for (arg in listOfArgs) {
            if (arg is Complex) {
                listOfNumbers.add(arg)
            } else {
                throw IllegalArgumentException("List contains element, which is not a complex number")
            }
        }

        return listOfNumbers
    }

    private fun parseDoubleNumbers(listOfArgs : List<Any>) : List<Double> {
        val listOfNumbers = mutableListOf<Double>()

        for (arg in listOfArgs) {
            if (arg is Double) {
                listOfNumbers.add(arg)
            } else {
                throw IllegalArgumentException("List contains element, which is not a complex number")
            }
        }

        return listOfNumbers
    }

    private fun brackets(listOfArgs: List<Any>) : Any {
        if (listOfArgs.size != 1) {
            throw IllegalArgumentException("Not one argument in brackets.");
        } else {
            return listOfArgs[0];
        }
    }

    private fun plus(listOfArgs: List<Any>) : Any =
            baseComputationOperations[computationType]!!["+"]!!.invoke(listOfArgs)

    private fun mul(listOfArgs: List<Any>) : Any =
            baseComputationOperations[computationType]!!["*"]!!.invoke(listOfArgs)

    private fun sumN(expression: ExpressionNode) : Any {
        val deploymentOfSum = unfold(convertToFoldedExpression(expression))
        return plus(deploymentOfSum)
    }

    private fun convertToFoldedExpression(expression: ExpressionNode) : FoldedExpression {
        if (!isFoldedExpression(expression)) {
            throw IllegalArgumentException("${expression.value} is not folded")
        }

        val from = compute(expression.children[1])
        val to = compute(expression.children[2])

        return FoldedExpression(expression.children[0].value, convertToDouble(from),
                convertToDouble(to), expression.children[3])
    }

    private fun convertToDouble(value : Any) : Double {
        return when (computationType) {
            ComputationType.DOUBLE -> value as Double
            ComputationType.COMPLEX -> (value as Complex).toDouble()
        }
    }

    private fun unfold(foldedExpression: FoldedExpression) : List<Any> {
        val deployedArguments = mutableListOf<Any>()
        var iter = foldedExpression.from
        while (iter <= foldedExpression.to) {
            val expression = foldedExpression.expression.cloneWithVariableReplacement(mutableMapOf(
                    Pair(foldedExpression.nameOfZVariable, iter.toString())))
            deployedArguments.add(compute(expression))
            iter++
        }

        return deployedArguments
    }

    private fun prodN(expression: ExpressionNode) : Any {
        val deploymentOfProd = unfold(convertToFoldedExpression(expression))
        return mul(deploymentOfProd)
    }
}