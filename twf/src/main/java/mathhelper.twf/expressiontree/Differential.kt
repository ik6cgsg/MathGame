package mathhelper.twf.expressiontree

import mathhelper.twf.config.CompiledConfiguration

fun ExpressionNode.containsDifferentiation() = containsFunction("d", 2)

fun ExpressionNode.diff(transformationWeight: MutableList<Double> = mutableListOf(0.0), compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (transformationWeight.isEmpty()) {
        transformationWeight.add(0.0)
    }
    if (!containsFunction("d", 2)) {
        return this
    }
    if (nodeType == NodeType.FUNCTION && value == "d" && children.size == 2 && children[1].nodeType == NodeType.VARIABLE) {
        val variable = children[1].value
        if (children[0].getContainedVariables(setOf(variable)).isEmpty()) { //constant
            return ExpressionNode(NodeType.VARIABLE, "0", identifier = "0")
        } else if (children[0].nodeType == NodeType.VARIABLE) {
            return ExpressionNode(NodeType.VARIABLE, "1", identifier = "1")
        } else if (children[0].value == "+" || children[0].value == "-") {
            return diffPlusMinus(variable, transformationWeight, compiledConfiguration)
        } else if (children[0].value == "*" && children[0].children.size == 2 &&
                (children[0].children[0].getContainedVariables(setOf(variable)).isEmpty() ||
                        children[0].children[1].getContainedVariables(setOf(variable)).isEmpty())) {
            return diffMul(variable, transformationWeight, compiledConfiguration)
        }
        transformationWeight[0] += 0.4
        when (children[0].value) {
            "*" -> return diffMul(variable, transformationWeight, compiledConfiguration)
        }
        transformationWeight[0] += 0.15
        when (children[0].value) {
            "/" -> return diffDiv(variable, transformationWeight, compiledConfiguration)
            "^" -> return diffPow(variable, transformationWeight, compiledConfiguration)
            "sqrt" -> return diffSqrt(variable, transformationWeight, compiledConfiguration)
            "ln" -> return diffLn(variable, transformationWeight, compiledConfiguration)
            "exp" -> return diffExp(variable, transformationWeight, compiledConfiguration)
            "sin" -> return diffSin(variable, transformationWeight, compiledConfiguration)
            "cos" -> return diffCos(variable, transformationWeight, compiledConfiguration)
            "asin" -> return diffAsin(variable, transformationWeight, compiledConfiguration)
            "acos" -> return diffAcos(variable, transformationWeight, compiledConfiguration)
            "sh" -> return diffSh(variable, transformationWeight, compiledConfiguration)
            "ch" -> return diffCh(variable, transformationWeight, compiledConfiguration)
            "tg" -> return diffTg(variable, transformationWeight, compiledConfiguration)
            "ctg" -> return diffCtg(variable, transformationWeight, compiledConfiguration)
            "atg" -> return diffAtg(variable, transformationWeight, compiledConfiguration)
            "actg" -> return diffActg(variable, transformationWeight, compiledConfiguration)
            "th" -> return diffTh(variable, transformationWeight, compiledConfiguration)
            "cth" -> return diffCth(variable, transformationWeight, compiledConfiguration)
        }
    }
    val result = copy()
    var maxWeight = 0.0
    for (child in children) {
        val currWeight = mutableListOf(0.0)
        result.addChild(child.diff(currWeight, compiledConfiguration))
        if (maxWeight < currWeight[0]) {
            maxWeight = currWeight[0]
        }
    }
    transformationWeight[0] += maxWeight
    return result
}

val unlimitedWeight = 10000.0

fun buildDiffNode(expressionNode: ExpressionNode, variable: String, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    val newChild = ExpressionNode(NodeType.FUNCTION, "d")
    if (expressionNode.value != "" || expressionNode.children.size != 1) // TODO: check this if
        newChild.addChild(expressionNode.clone())
    else
        newChild.addChild(expressionNode.children[0].clone())
    newChild.addChild(ExpressionNode(NodeType.VARIABLE, variable))
    return newChild
}

private fun ExpressionNode.diffLn(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))
    result.addChild(children[0].children[0].clone())

    return result
}

private fun ExpressionNode.diffSqrt(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    val doubledValue = compiledConfiguration.createExpressionFunctionNode("*", -1)
    doubledValue.addChild(ExpressionNode(NodeType.VARIABLE, "2"))
    doubledValue.addChild(children[0].clone())

    result.addChild(doubledValue)

    return result
}

private fun ExpressionNode.diffExp(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))
    result.addChild(children[0].clone())
    return result
}

private fun ExpressionNode.diffSin(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
    result.addChild(compiledConfiguration.createExpressionFunctionNode("cos", 1))
    result.children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    return result
}

private fun ExpressionNode.diffCos(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
    result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("sin", 1))
    result.children.last().children.last().children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    return result
}

private fun ExpressionNode.diffSh(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
    result.addChild(compiledConfiguration.createExpressionFunctionNode("ch", 1))
    result.children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    return result
}

private fun ExpressionNode.diffCh(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
    result.addChild(compiledConfiguration.createExpressionFunctionNode("sh", 1))
    result.children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    return result
}

private fun ExpressionNode.diffAsin(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    result.children.last().children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "0.5"))

    return result
}

private fun ExpressionNode.diffAcos(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.children.last().children.last().addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    result.children.last().children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "0.5"))

    return result
}

private fun ExpressionNode.diffTg(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("cos", 1))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffCtg(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    result.children.last().children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("sin", 1))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffAtg(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffActg(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.children.last().children.last().addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffTh(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("ch", 1))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffCth(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = compiledConfiguration.createExpressionFunctionNode("/", -1)
    val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
    result.addChild(newChild.diff(transformationWeight, compiledConfiguration))

    result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
    result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
    result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
    result.children.last().children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("sh", 1))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffPlusMinus(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    val result = children[0].copy()
    var maxWeight = 0.0
    for (child in children[0].children) {
        val newChild = buildDiffNode(child, variable, compiledConfiguration)
        val currWeight =(mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
        result.addChild(newChild.diff(currWeight, compiledConfiguration))
        if (maxWeight < currWeight[0]) {
            maxWeight = currWeight[0]
        }
    }
    transformationWeight[0] += maxWeight
    return result
}

private fun ExpressionNode.diffMul(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    val result = compiledConfiguration.createExpressionFunctionNode("+", -1)
    var maxWeight = 0.0
    for (diffChildIndex in 0..children[0].children.lastIndex) {
        result.addChild(compiledConfiguration.createExpressionFunctionNode("*", -1))
        for (i in 0..children[0].children.lastIndex) {
            if (diffChildIndex == i) {
                val newChild = buildDiffNode(children[0].children[i], variable, compiledConfiguration)
                val currWeight = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
                result.children.last().addChild(newChild.diff(currWeight, compiledConfiguration))
                if (maxWeight < currWeight[0]) {
                    maxWeight = currWeight[0]
                }
            } else {
                result.children.last().addChild(children[0].children[i].clone())
            }
        }
    }
    transformationWeight[0] += maxWeight
    return result
}

private fun ExpressionNode.diffDiv(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size == 1) { // (1/f(x))' = - f' / (f^2)
        val result = compiledConfiguration.createExpressionFunctionNode("/", -1)

        result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
        result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
        val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
        result.children.last().children.last().addChild(newChild.diff(transformationWeight, compiledConfiguration))

        result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
        result.children.last().addChild(children[0].children[0].clone())
        result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2", identifier = "2"))
        return result
    } else {
        var denum = children[0].children[1]
        if (children[0].children.size > 2) {
            denum = compiledConfiguration.createExpressionFunctionNode("*", -1)
            for (i in 1..children[0].children.lastIndex) {
                denum.addChild(children[0].children[i])
            }
        }

        if (children[0].children[0].getContainedVariables(setOf(variable)).isEmpty()) { // (c/f(x))' = - c*f' / (f^2)
            val result = compiledConfiguration.createExpressionFunctionNode("/", -1)

            result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
            result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
            result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("*", -1))
            result.children.last().children.last().children.last().addChild(children[0].children[0].clone())
            val newChild = buildDiffNode(denum, variable, compiledConfiguration)
            result.children.last().children.last().children.last().addChild(newChild.diff(transformationWeight, compiledConfiguration))

            result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
            result.children.last().addChild(denum.clone())
            result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2", identifier = "2"))
            return result
        } else { // (f(x)/g(x))' = (f'*g - g'*f / (g^2)
            val result = compiledConfiguration.createExpressionFunctionNode("/", -1)

            result.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
            result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("*", -1))
            result.children.last().children.last().addChild(denum.clone())
            val newNumChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
            val weight1 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
            result.children.last().children.last().addChild(newNumChild.diff(weight1, compiledConfiguration))

            result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
            result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("*", -1))
            result.children.last().children.last().children.last().addChild(children[0].children[0].clone())
            val newDenumChild = buildDiffNode(denum, variable, compiledConfiguration)
            val weight2 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
            result.children.last().children.last().children.last().addChild(newDenumChild.diff(weight2, compiledConfiguration))

            result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
            result.children.last().addChild(denum.clone())
            result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2", identifier = "2"))
            if (weight2[0] > weight1[0]){
                transformationWeight[0] += weight2[0]
            } else {
                transformationWeight[0] += weight1[0]
            }
            return result
        }
    }
}

private fun ExpressionNode.diffPow(variable: String, transformationWeight: MutableList<Double>, compiledConfiguration: CompiledConfiguration): ExpressionNode {
    if (children[0].children.size < 2) return this.clone()

    var degree = children[0].children[1]
    if (children[0].children.size > 2) {
        degree = compiledConfiguration.createExpressionFunctionNode("^", -1)
        for (i in 1..children[0].children.lastIndex) {
            degree.addChild(children[0].children[i])
        }
    }

    if (degree.getContainedVariables(setOf(variable)).isEmpty()) { // (f(x)^c)' = c*f^(c-1)*f'
        val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
        result.addChild(degree.clone())

        result.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
        result.children.last().addChild(children[0].children[0].clone())
        result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
        result.children.last().children.last().addChild(degree.clone())
        result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
        result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))

        val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
        result.addChild(newChild.diff(transformationWeight, compiledConfiguration))
        return result
    } else {
        if (children[0].children[0].getContainedVariables(setOf(variable)).isNotEmpty()) {
            if (transformationWeight.size > 1) { //unlimited differentiation actions count -> (f(x)^g(x))' = (e^(g*ln(f)))' = e^(g*ln(f)) * (g*ln(f))' = f^g * (g' * ln(f) + g * f' / f)
                val result = compiledConfiguration.createExpressionFunctionNode("+", -1)

                result.addChild(compiledConfiguration.createExpressionFunctionNode("*", -1))
                result.children.last().addChild(children[0].clone())
                result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("ln", 1))
                result.children.last().children.last().addChild(children[0].children[0].clone())
                val newDegreeChild = buildDiffNode(degree, variable, compiledConfiguration)
                val weight1 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
                result.children.last().addChild(newDegreeChild.diff(weight1, compiledConfiguration))

                result.addChild(compiledConfiguration.createExpressionFunctionNode("*", -1))
                result.children.last().addChild(degree.clone())
                val newChild = buildDiffNode(children[0].children[0], variable, compiledConfiguration)
                val weight2 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
                result.children.last().addChild(newChild.diff(weight2, compiledConfiguration))

                result.children.last().addChild(compiledConfiguration.createExpressionFunctionNode("^", -1))
                result.children.last().children.last().addChild(children[0].children[0].clone())
                result.children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
                result.children.last().children.last().children.last().addChild(degree.clone())
                result.children.last().children.last().children.last().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
                result.children.last().children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))

                if (weight2[0] > weight1[0]){
                    transformationWeight[0] += weight2[0]
                } else {
                    transformationWeight[0] += weight1[0]
                }
                return result
            } else { // student need to make such transformations by himself
                return this.clone()
            }
        } else { // (c^f(x))' = c^f(x) * ln(c) * f'
            val result = compiledConfiguration.createExpressionFunctionNode("*", -1)
            result.addChild(children[0].clone())

            result.addChild(compiledConfiguration.createExpressionFunctionNode("ln", 1))
            result.children.last().addChild(children[0].children[0].clone())

            val newChild = buildDiffNode(degree, variable, compiledConfiguration)
            result.addChild(newChild.diff(transformationWeight, compiledConfiguration))
            return result
        }
    }
}