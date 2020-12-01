package baseoperations

import expressiontree.ExpressionNode
import numbers.Complex
import numbers.toComplex
import optimizerutils.CompressedNode
import optimizerutils.CompressedNodeDouble
import platformdependent.defaultRandom
import standartlibextensions.abs

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
    private fun complexFunctionToComplex(listOfArgs : List<Complex>, baseOperation : (args : List<Complex>) -> Complex) =
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

    fun calculatePenaltyForNode(value: Complex, operatorType: String = "") : Double {
        if (operatorType == "ln" || operatorType == "asin" || operatorType == "acos") {
            val re = value.getReal().value.abs()
            val im = value.getImaginary().value.abs()
            return re * im
        }
        return value.getImaginary().value.abs()
    }

    // function computePenalty takes array of all nodes.
    // Children should appear earlier that parents
    fun computePenalty(treeNodes: ArrayList<CompressedNode>) : Double {
        var penalty = 0.0
        for (node in treeNodes){
            if (node.children.isEmpty() || node.func == ""){
                node.subtreeValue = node.value
            } else {
                // TODO isFoldedExpression

                val listOfArgs = mutableListOf<Complex>()
                for (child in node.children)
                    listOfArgs.add(child.subtreeValue)
                if (node.func.isNotEmpty()){
                    node.subtreeValue = node.functor!!(listOfArgs)
                    penalty += calculatePenaltyForNode(node.subtreeValue, node.func)
                }
            }
        }
        return penalty
    }


    fun computeValue(treeNodes: ArrayList<CompressedNodeDouble>) {
        for (node in treeNodes){
            if (node.children.isEmpty() && node.func == "")
                continue
            else if (node.children.isNotEmpty() && node.func != ""){
                // TODO isFoldedExpression

                val listOfArgs = mutableListOf<Double>()
                for (child in node.children)
                    listOfArgs.add(child.value)
                node.value = node.functor!!(listOfArgs)
            } else if (node.children.size == 1 && node.func == "")
                node.value = node.children[0].value
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
            throw IllegalArgumentException("Not one argument in brackets.")
        } else {
            return listOfArgs[0]
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

    public val baseComputationComplex = mapOf(
                "+" to {listOfArgs : List<Complex> -> ComplexBaseOperation.plus(listOfArgs)},
                "*" to {listOfArgs : List<Complex> -> ComplexBaseOperation.mul(listOfArgs)},
                "-" to  {listOfArgs : List<Complex> -> ComplexBaseOperation.minus(listOfArgs)},
                "/" to {listOfArgs : List<Complex> -> ComplexBaseOperation.div(listOfArgs)},
                "^" to  {listOfArgs : List<Complex> -> ComplexBaseOperation.pow(listOfArgs)},
                "mod" to {listOfArgs : List<Complex> -> ComplexBaseOperation.mod(listOfArgs)},
                "and" to {listOfArgs : List<Complex> -> ComplexBaseOperation.and(listOfArgs)},
                "or" to {listOfArgs : List<Complex> -> ComplexBaseOperation.or(listOfArgs)},
                "xor" to {listOfArgs : List<Complex> -> ComplexBaseOperation.xor(listOfArgs)},
                "alleq" to {listOfArgs : List<Complex> -> ComplexBaseOperation.alleq(listOfArgs)},
                "not" to {listOfArgs : List<Complex> -> ComplexBaseOperation.not(listOfArgs)},
                "sin" to {listOfArgs : List<Complex> -> ComplexBaseOperation.sin(listOfArgs)},
                "cos" to {listOfArgs : List<Complex> -> ComplexBaseOperation.cos(listOfArgs)},
                "tg" to {listOfArgs : List<Complex> -> ComplexBaseOperation.tan(listOfArgs)},
                "sh" to {listOfArgs : List<Complex> -> ComplexBaseOperation.sinh(listOfArgs)},
                "ch" to {listOfArgs : List<Complex> -> ComplexBaseOperation.cosh(listOfArgs)},
                "th" to {listOfArgs : List<Complex> -> ComplexBaseOperation.tanh(listOfArgs)},
                "asin" to {listOfArgs : List<Complex> -> ComplexBaseOperation.asin(listOfArgs)},
                "acos" to {listOfArgs : List<Complex> -> ComplexBaseOperation.acos(listOfArgs)},
                "atg" to {listOfArgs : List<Complex> -> ComplexBaseOperation.atan(listOfArgs)},
                "actg" to {listOfArgs : List<Complex> -> ComplexBaseOperation.actan(listOfArgs)},
                "pow" to {listOfArgs : List<Complex> -> ComplexBaseOperation.pow(listOfArgs)},
                "ln" to {listOfArgs : List<Complex> -> ComplexBaseOperation.ln(listOfArgs)},
                "exp" to {listOfArgs : List<Complex> -> ComplexBaseOperation.exp(listOfArgs)},
                "abs" to {listOfArgs : List<Complex> -> ComplexBaseOperation.abs(listOfArgs)},
                "sqrt" to {listOfArgs : List<Complex> -> ComplexBaseOperation.sqrt(listOfArgs)}
    )

    public val baseComputationDouble = mapOf(
            "+" to {listOfArgs : List<Double> -> DoubleBaseOperation.plus(listOfArgs)},
            "*" to {listOfArgs : List<Double> -> DoubleBaseOperation.mul(listOfArgs)},
            "-" to  {listOfArgs : List<Double> -> DoubleBaseOperation.minus(listOfArgs)},
            "/" to {listOfArgs : List<Double> -> DoubleBaseOperation.div(listOfArgs)},
            "^" to  {listOfArgs : List<Double> -> DoubleBaseOperation.pow(listOfArgs)},
            "mod" to {listOfArgs : List<Double> -> DoubleBaseOperation.mod(listOfArgs)},
            "and" to {listOfArgs : List<Double> -> DoubleBaseOperation.and(listOfArgs)},
            "or" to {listOfArgs : List<Double> -> DoubleBaseOperation.or(listOfArgs)},
            "xor" to {listOfArgs : List<Double> -> DoubleBaseOperation.xor(listOfArgs)},
            "alleq" to {listOfArgs : List<Double> -> DoubleBaseOperation.alleq(listOfArgs)},
            "not" to {listOfArgs : List<Double> -> DoubleBaseOperation.not(listOfArgs)},
            "sin" to {listOfArgs : List<Double> -> DoubleBaseOperation.sin(listOfArgs)},
            "cos" to {listOfArgs : List<Double> -> DoubleBaseOperation.cos(listOfArgs)},
            "tg" to {listOfArgs : List<Double> -> DoubleBaseOperation.tan(listOfArgs)},
            "sh" to {listOfArgs : List<Double> -> DoubleBaseOperation.sinh(listOfArgs)},
            "ch" to {listOfArgs : List<Double> -> DoubleBaseOperation.cosh(listOfArgs)},
            "th" to {listOfArgs : List<Double> -> DoubleBaseOperation.tanh(listOfArgs)},
            "asin" to {listOfArgs : List<Double> -> DoubleBaseOperation.asin(listOfArgs)},
            "acos" to {listOfArgs : List<Double> -> DoubleBaseOperation.acos(listOfArgs)},
            "atg" to {listOfArgs : List<Double> -> DoubleBaseOperation.atan(listOfArgs)},
            "actg" to {listOfArgs : List<Double> -> DoubleBaseOperation.actan(listOfArgs)},
            "pow" to {listOfArgs : List<Double> -> DoubleBaseOperation.pow(listOfArgs)},
            "ln" to {listOfArgs : List<Double> -> DoubleBaseOperation.ln(listOfArgs)},
            "exp" to {listOfArgs : List<Double> -> DoubleBaseOperation.exp(listOfArgs)},
            "abs" to {listOfArgs : List<Double> -> DoubleBaseOperation.abs(listOfArgs)},
            "sqrt" to {listOfArgs : List<Double> -> DoubleBaseOperation.sqrt(listOfArgs)}
    )

}