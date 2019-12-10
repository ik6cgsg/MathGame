package com.twf.baseoperations

import com.twf.config.FunctionIdentifier
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType
import kotlin.math.pow

data class BaseOperationsDefinition(
        val name: String,
        val numberOfArguments: Int,
        val calculatingFunction: (ExpressionNode) -> ExpressionNode
)

class BaseOperationsDefinitions(val epsilon: Double = 11.9e-6) {
    val baseOperationsDefinitions = listOf<BaseOperationsDefinition>(
            BaseOperationsDefinition("", 0, { argsParentNode: ExpressionNode -> emptyBrackets(argsParentNode) }),
            BaseOperationsDefinition("", 1, { argsParentNode: ExpressionNode -> brackets(argsParentNode) }),
            BaseOperationsDefinition("+", -1, { argsParentNode: ExpressionNode -> plus(argsParentNode) }),
            BaseOperationsDefinition("*", -1, { argsParentNode: ExpressionNode -> mul(argsParentNode) }),
            BaseOperationsDefinition("-", -1, { argsParentNode: ExpressionNode -> minus(argsParentNode) }),
            BaseOperationsDefinition("/", -1, { argsParentNode: ExpressionNode -> div(argsParentNode) }),
            BaseOperationsDefinition("^", -1, { argsParentNode: ExpressionNode -> pow(argsParentNode) }),
            BaseOperationsDefinition("mod", 2, { argsParentNode: ExpressionNode -> mod(argsParentNode) }),
            BaseOperationsDefinition("S", 4, { argsParentNode: ExpressionNode -> sumN(argsParentNode) }),
            BaseOperationsDefinition("P", 4, { argsParentNode: ExpressionNode -> prodN(argsParentNode) }),

            BaseOperationsDefinition("and", -1, { argsParentNode: ExpressionNode -> and(argsParentNode) }),
            BaseOperationsDefinition("or", -1, { argsParentNode: ExpressionNode -> or(argsParentNode) }),
            BaseOperationsDefinition("xor", -1, { argsParentNode: ExpressionNode -> xor(argsParentNode) }),
            BaseOperationsDefinition("alleq", -1, { argsParentNode: ExpressionNode -> alleq(argsParentNode) }),
            BaseOperationsDefinition("not", 1, { argsParentNode: ExpressionNode -> not(argsParentNode) }),

            BaseOperationsDefinition("sin", 1, { argsParentNode: ExpressionNode -> sin(argsParentNode) }),
            BaseOperationsDefinition("cos", 1, { argsParentNode: ExpressionNode -> cos(argsParentNode) }),
            BaseOperationsDefinition("sh", 1, { argsParentNode: ExpressionNode -> sinh(argsParentNode) }),
            BaseOperationsDefinition("ch", 1, { argsParentNode: ExpressionNode -> cosh(argsParentNode) }),
            BaseOperationsDefinition("tg", 1, { argsParentNode: ExpressionNode -> tan(argsParentNode) }),
            BaseOperationsDefinition("th", 1, { argsParentNode: ExpressionNode -> tanh(argsParentNode) }),
            BaseOperationsDefinition("asin", 1, { argsParentNode: ExpressionNode -> asin(argsParentNode) }),
            BaseOperationsDefinition("acos", 1, { argsParentNode: ExpressionNode -> acos(argsParentNode) }),
            BaseOperationsDefinition("atg", 1, { argsParentNode: ExpressionNode -> atan(argsParentNode) }),
            BaseOperationsDefinition("actg", 1, { argsParentNode: ExpressionNode -> actan(argsParentNode) }),
            BaseOperationsDefinition("exp", 1, { argsParentNode: ExpressionNode -> exp(argsParentNode) }),
            BaseOperationsDefinition("ln", 1, { argsParentNode: ExpressionNode -> ln(argsParentNode) }),
            BaseOperationsDefinition("abs", 1, { argsParentNode: ExpressionNode -> abs(argsParentNode) })
    )

    val mapNameAndArgsNumberToOperation = baseOperationsDefinitions.associateBy { FunctionIdentifier.getIdentifier(it.name, it.numberOfArguments) }
    val definedFunctionNameNumberOfArgsSet = mapNameAndArgsNumberToOperation.keys.toSet()

    fun getOperation(name: String, numberOfArguments: Int): BaseOperationsDefinition? {
        val baseOperationsDefinition = mapNameAndArgsNumberToOperation.get(FunctionIdentifier.getIdentifier(name, numberOfArguments))
        if (baseOperationsDefinition == null)
            return mapNameAndArgsNumberToOperation.get(FunctionIdentifier.getIdentifier(name, -1))
        else return baseOperationsDefinition
    }

    fun applyOperationToExpressionNode(node: ExpressionNode): ExpressionNode {
        if (node.value.isEmpty()) {
            val operation = getOperation(node.value, node.children.size)
            if (operation != null) {
                return operation.calculatingFunction(node)
            }
        }
        if (node.children.size > 0) {
            val operation = getOperation(node.value, node.children.size)
            if (operation != null) {
                return operation.calculatingFunction(node)
            }
        }
        return node
    }

    fun computeExpressionTree(node: ExpressionNode): ExpressionNode {
        for (child in node.children) {
            computeExpressionTree(child)
        }
        return applyOperationToExpressionNode(node)
    }

    fun sumN(argsParentNode: ExpressionNode): ExpressionNode {
        var low = argsParentNode.children[1].value.toDoubleOrNull() ?: return argsParentNode
        val up = argsParentNode.children[2].value.toDoubleOrNull() ?: return argsParentNode
        if (up < low) {
            argsParentNode.setVariable("0.0")
        } else {
            val counterName = argsParentNode.children[0].value
            var result = 0.0
            while (low <= up) {
                val expr = argsParentNode.children[3].cloneWithVariableReplacement(mutableMapOf(Pair(counterName, low.toString())))
                result += computeExpressionTree(expr).value.toDoubleOrNull() ?: return argsParentNode
                low += 1.0
            }
            argsParentNode.setVariable(result.toString())
        }
        return argsParentNode
    }

    fun prodN(argsParentNode: ExpressionNode): ExpressionNode {
        var low = argsParentNode.children[1].value.toDoubleOrNull() ?: return argsParentNode
        val up = argsParentNode.children[2].value.toDoubleOrNull() ?: return argsParentNode
        if (up < low) {
            argsParentNode.setVariable("1.0")
        } else {
            val counterName = argsParentNode.children[0].value
            var result = 1.0
            var flagHasVariable = false
            while (low <= up) {
                val expr = argsParentNode.children[3].cloneWithVariableReplacement(mutableMapOf(Pair(counterName, low.toString())))
                val arg = computeExpressionTree(expr).value.toDoubleOrNull()
                if (arg == null) {
                    flagHasVariable = true;
                } else {
                    result *= arg
                    if (result.additivelyEqualToZero()) {
                        argsParentNode.setVariable("0.0")
                        break;
                    }
                }
                low += 1.0
            }
            if (!flagHasVariable)
                argsParentNode.setVariable(result.toString())
        }
        return argsParentNode
    }

    fun plus(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 0.0
        for (argNode in argsParentNode.children) {
            if (argNode.children.isNotEmpty()) continue
            val argValue = argNode.value.toDoubleOrNull()
            if (argValue != null) {
                result += argValue
                argNode.nodeType = NodeType.EMPTY
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualToZero()) {
            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        } else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        return argsParentNode
    }

    fun minus(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 0.0
        val first = argsParentNode.children[0].value.toDoubleOrNull()
        val firstIsNumber = ((first != null) && (argsParentNode.children[0].children.size == 0))
        if (firstIsNumber) result = first!!
        if (argsParentNode.children.size == 1) {
            if (firstIsNumber)
                argsParentNode.setVariable((-result).toString())
            return argsParentNode
        } else {
            for (i in 1 until argsParentNode.children.size) {
                if (argsParentNode.children[i].children.isNotEmpty()) continue
                val argValue = argsParentNode.children[i].value.toDoubleOrNull()
                if (argValue != null) {
                    if (firstIsNumber) result -= argValue
                    else result += argValue
                    argsParentNode.children[i].nodeType = NodeType.EMPTY
                }
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 1) {
            if (firstIsNumber) argsParentNode.setVariable(result.toString())
            else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        } else {
            if (firstIsNumber) argsParentNode.children[0].setVariable(result.toString())
            else if (!result.additivelyEqualToZero() && (startSize > argsParentNode.children.size)) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        }
        return argsParentNode
    }

    fun and(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 1.0
        for (argNode in argsParentNode.children) {
            if (argNode.children.isNotEmpty()) continue
            val argValue = argNode.value.toDoubleOrNull()
            if (argValue != null) {
                result *= argValue
                argNode.nodeType = NodeType.EMPTY
                if (result.additivelyEqualToZero()) {
                    argsParentNode.setVariable("0.0")
                    return argsParentNode
                }
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualTo(1.0)) {
            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        }
        return argsParentNode
    }

    fun or(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 0.0
        for (argNode in argsParentNode.children) {
            if (argNode.children.isNotEmpty()) continue
            val argValue = argNode.value.toDoubleOrNull()
            if (argValue != null) {
                result += argValue
                argNode.nodeType = NodeType.EMPTY
                if (!result.additivelyEqualToZero()) {
                    argsParentNode.setVariable("1.0")
                    return argsParentNode
                }
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualTo(1.0)) {
            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        }
        return argsParentNode
    }

    fun xor(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 0.0
        for (argNode in argsParentNode.children) {
            if (argNode.children.isNotEmpty()) continue
            val argValue = argNode.value.toDoubleOrNull()
            if (argValue != null) {
                result.addMod2(argValue)
                argNode.nodeType = NodeType.EMPTY
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualToZero()) {
            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        } else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        return argsParentNode
    }

    fun alleq(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 0.5
        for (argNode in argsParentNode.children) {
            if (argNode.children.isNotEmpty()) continue
            val argValue = argNode.value.toDoubleOrNull()
            if (argValue != null) {
                val addArg = if (argValue.additivelyEqualToZero()) 0.0 else 1.0
                if (result == 0.5) result = addArg
                else if (!result.additivelyEqualTo(addArg)) {
                    argsParentNode.setVariable("0.0")
                    return argsParentNode
                }
                argNode.nodeType = NodeType.EMPTY
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualToZero()) {
            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        } else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        return argsParentNode
    }

    fun Double.addMod2(arg: Double): Double {
        if (this.additivelyEqualToZero()) return arg
        else return arg.not()
    }

    fun mul(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 1.0
        for (argNode in argsParentNode.children) {
            if (argNode.children.isNotEmpty()) continue
            val argValue = argNode.value.toDoubleOrNull()
            if (argValue != null) {
                result *= argValue
                argNode.nodeType = NodeType.EMPTY
                if (result.additivelyEqualToZero()) {
                    argsParentNode.setVariable("0.0")
                    return argsParentNode
                }
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualTo(1.0)) {
            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        } else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        return argsParentNode
    }

    fun div(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 1.0
        val first = argsParentNode.children[0].value.toDoubleOrNull()
        val firstIsNumber = ((first != null) && (argsParentNode.children[0].children.size == 0))
        if (firstIsNumber) result = first!!
        if (argsParentNode.children.size == 1) {
            if (firstIsNumber)
                argsParentNode.setVariable((1 / result).toString())
            return argsParentNode
        } else {
            for (i in 1 until argsParentNode.children.size) {
                if (argsParentNode.children[i].children.isNotEmpty()) continue
                val argValue = argsParentNode.children[i].value.toDoubleOrNull()
                if (argValue != null) {
                    if (firstIsNumber) result /= argValue
                    else result *= argValue
                    argsParentNode.children[i].nodeType = NodeType.EMPTY
                }
            }
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 1) {
            if (firstIsNumber) argsParentNode.setVariable(result.toString())
            else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        } else {
            if (firstIsNumber) argsParentNode.children[0].setVariable(result.toString())
            else if (!result.additivelyEqualTo(1.0) && (startSize > argsParentNode.children.size)) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        }
        return argsParentNode
    }

    fun pow(argsParentNode: ExpressionNode): ExpressionNode {
        val startSize = argsParentNode.children.size
        var result = 1.0
        for (i in argsParentNode.children.lastIndex downTo 0) {
            if (argsParentNode.children[i].children.isNotEmpty()) {
                break
            }
            val argValue = argsParentNode.children[i].value.toDoubleOrNull()
            if (argValue != null) {
                result = argValue.pow(result)
                argsParentNode.children[i].nodeType = NodeType.EMPTY
            } else if (result.additivelyEqualToZero()) {
                result = 1.0
                argsParentNode.children[i].nodeType = NodeType.EMPTY
            } else break
        }
        argsParentNode.children.removeAll({ it.nodeType == NodeType.EMPTY })
        if (argsParentNode.children.size == 0) argsParentNode.setVariable(result.toString())
        else if (result.additivelyEqualToZero()) {
            if (argsParentNode.children.size == 1) argsParentNode.setVariable("1.0")
        } else if (result.additivelyEqualTo(1.0)) {
//            if (argsParentNode.children.size == 1) argsParentNode.setNode(argsParentNode.children[0])
        } else if (startSize > argsParentNode.children.size) argsParentNode.addChild(ExpressionNode(NodeType.VARIABLE, result.toString()))
        return argsParentNode
    }

    fun mod(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        val secondArg: Double?
        if (firstArg.additivelyEqualToZero()) {
            argsParentNode.setVariable("0.0")
        } else {
            secondArg = argsParentNode.children[1].value.toDoubleOrNull() ?: return argsParentNode
            argsParentNode.setVariable(firstArg.rem(secondArg).toString())
        }
        return argsParentNode
    }

    fun sin(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.sin(firstArg).toString())
        return argsParentNode
    }

    fun cos(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.cos(firstArg).toString())
        return argsParentNode
    }

    fun tan(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.tan(firstArg).toString())
        return argsParentNode
    }

    fun asin(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.asin(firstArg).toString())
        return argsParentNode
    }

    fun acos(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.acos(firstArg).toString())
        return argsParentNode
    }

    fun atan(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.atan(firstArg).toString())
        return argsParentNode
    }

    fun actan(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.atan(1.0 / firstArg).toString())
        return argsParentNode
    }

    fun sinh(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.sinh(firstArg).toString())
        return argsParentNode
    }

    fun cosh(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.cosh(firstArg).toString())
        return argsParentNode
    }

    fun tanh(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.tanh(firstArg).toString())
        return argsParentNode
    }

    fun exp(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.exp(firstArg).toString())
        return argsParentNode
    }

    fun ln(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.log(firstArg, kotlin.math.E).toString())
        return argsParentNode
    }

    fun abs(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(kotlin.math.abs(firstArg).toString())
        return argsParentNode
    }

    fun not(argsParentNode: ExpressionNode): ExpressionNode {
        val firstArg = argsParentNode.children[0].value.toDoubleOrNull() ?: return argsParentNode
        argsParentNode.setVariable(firstArg.not().toString())
        return argsParentNode
    }

    fun Double.not() = if (this.additivelyEqualToZero()) 1.0 else 0.0

    fun brackets(argsParentNode: ExpressionNode): ExpressionNode {
        if (argsParentNode.children[0].children.size == 0) {
            argsParentNode.setVariable(argsParentNode.children[0].value)
            return argsParentNode
        } else {
            return argsParentNode.children[0]
        }
    }

    fun emptyBrackets(argsParentNode: ExpressionNode): ExpressionNode {
        argsParentNode.setVariable("0")
        return argsParentNode
    }

    private fun Double.additivelyEqualTo(number: Double) = (this - number).additivelyEqualToZero()
    private fun Double.additivelyEqualToZero() = (this in (-epsilon)..epsilon)

    fun additivelyEqual(l: Double, r: Double) = (l == r || l.additivelyEqualTo(r))

}