package com.twf.expressiontree

fun ExpressionNode.containsDifferentiation() = containsFunction("d", 2)

fun ExpressionNode.diff(transformationWeight: MutableList<Double> = mutableListOf(0.0)): ExpressionNode {
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
            return diffPlusMinus(variable, transformationWeight)
        } else if (children[0].value == "*" && children[0].children.size == 2 &&
                (children[0].children[0].getContainedVariables(setOf(variable)).isEmpty() ||
                        children[0].children[1].getContainedVariables(setOf(variable)).isEmpty())) {
            return diffMul(variable, transformationWeight)
        }
        transformationWeight[0] += 0.4
        when (children[0].value) {
            "*" -> return diffMul(variable, transformationWeight)
        }
        transformationWeight[0] += 0.15
        when (children[0].value) {
            "/" -> return diffDiv(variable, transformationWeight)
            "^" -> return diffPow(variable, transformationWeight)
            "ln" -> return diffLn(variable, transformationWeight)
            "exp" -> return diffExp(variable, transformationWeight)
            "sin" -> return diffSin(variable, transformationWeight)
            "cos" -> return diffCos(variable, transformationWeight)
            "asin" -> return diffAsin(variable, transformationWeight)
            "acos" -> return diffAcos(variable, transformationWeight)
            "sh" -> return diffSh(variable, transformationWeight)
            "ch" -> return diffCh(variable, transformationWeight)
            "tg" -> return diffTg(variable, transformationWeight)
            "ctg" -> return diffCtg(variable, transformationWeight)
            "atg" -> return diffAtg(variable, transformationWeight)
            "actg" -> return diffActg(variable, transformationWeight)
            "th" -> return diffTh(variable, transformationWeight)
            "cth" -> return diffCth(variable, transformationWeight)
        }
    }
    val result = copy()
    var maxWeight = 0.0
    for (child in children) {
        val currWeight = mutableListOf(0.0)
        result.addChild(child.diff(currWeight))
        if (maxWeight < currWeight[0]) {
            maxWeight = currWeight[0]
        }
    }
    transformationWeight[0] += maxWeight
    return result
}

val unlimitedWeight = 10000.0

private fun buildDiffNode(expressionNode: ExpressionNode, variable: String): ExpressionNode {
    val newChild = ExpressionNode(NodeType.FUNCTION, "d")
    newChild.addChild(expressionNode.clone())
    newChild.addChild(ExpressionNode(NodeType.VARIABLE, variable))
    return newChild
}

private fun ExpressionNode.diffLn(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))
    result.addChild(children[0].children[0].clone())

    return result
}

private fun ExpressionNode.diffExp(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "*")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))
    result.addChild(children[0].clone())

    return result
}

private fun ExpressionNode.diffSin(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "*")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "cos"))
    result.children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    return result
}

private fun ExpressionNode.diffCos(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "*")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "sin"))
    result.children.last().children.last().children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    return result
}

private fun ExpressionNode.diffSh(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "*")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "ch"))
    result.children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    return result
}

private fun ExpressionNode.diffCh(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "*")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "sh"))
    result.children.last().addChild(children[0].children[0].clone())
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    return result
}

private fun ExpressionNode.diffAsin(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "0.5"))

    return result
}

private fun ExpressionNode.diffAcos(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.children.last().children.last().addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "0.5"))

    return result
}

private fun ExpressionNode.diffTg(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "cos"))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffCtg(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "sin"))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffAtg(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffActg(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.children.last().children.last().addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffTh(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "ch"))
    result.children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffCth(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size != 1) return this.clone()

    val result = ExpressionNode(NodeType.FUNCTION, "/")
    val newChild = buildDiffNode(children[0].children[0], variable)
    result.addChild(newChild.diff(transformationWeight))

    result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
    result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
    result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
    result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "sh"))
    result.children.last().children.last().children.last().children.last().addChild(children[0].children[0])
    result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2"))

    return result
}

private fun ExpressionNode.diffPlusMinus(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    val result = children[0].copy()
    var maxWeight = 0.0
    for (child in children[0].children) {
        val newChild = buildDiffNode(child, variable)
        val currWeight =(mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
        result.addChild(newChild.diff(currWeight))
        if (maxWeight < currWeight[0]) {
            maxWeight = currWeight[0]
        }
    }
    transformationWeight[0] += maxWeight
    return result
}

private fun ExpressionNode.diffMul(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    val result = ExpressionNode(NodeType.FUNCTION, "+")
    var maxWeight = 0.0
    for (diffChildIndex in 0..children[0].children.lastIndex) {
        result.addChild(ExpressionNode(NodeType.FUNCTION, "*"))
        for (i in 0..children[0].children.lastIndex) {
            if (diffChildIndex == i) {
                val newChild = buildDiffNode(children[0].children[i], variable)
                val currWeight = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
                result.children.last().addChild(newChild.diff(currWeight))
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

private fun ExpressionNode.diffDiv(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size == 1) { // (1/f(x))' = - f' / (f^2)
        val result = ExpressionNode(NodeType.FUNCTION, "/")

        result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
        result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
        val newChild = buildDiffNode(children[0].children[0], variable)
        result.children.last().children.last().addChild(newChild.diff(transformationWeight))

        result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
        result.children.last().addChild(children[0].children[0].clone())
        result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2", identifier = "2"))
        return result
    } else {
        var denum = children[0].children[1]
        if (children[0].children.size > 2) {
            denum = ExpressionNode(NodeType.FUNCTION, "*")
            for (i in 1..children[0].children.lastIndex) {
                denum.addChild(children[0].children[i])
            }
        }

        if (children[0].children[0].getContainedVariables(setOf(variable)).isEmpty()) { // (c/f(x))' = - c*f' / (f^2)
            val result = ExpressionNode(NodeType.FUNCTION, "/")

            result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
            result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
            result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "*"))
            result.children.last().children.last().children.last().addChild(children[0].children[0].clone())
            val newChild = buildDiffNode(denum, variable)
            result.children.last().children.last().children.last().addChild(newChild.diff(transformationWeight))

            result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
            result.children.last().addChild(denum.clone())
            result.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "2", identifier = "2"))
            return result
        } else { // (f(x)/g(x))' = (f'*g - g'*f / (g^2)
            val result = ExpressionNode(NodeType.FUNCTION, "/")

            result.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
            result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "*"))
            result.children.last().children.last().addChild(denum.clone())
            val newNumChild = buildDiffNode(children[0].children[0], variable)
            var weight1 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
            result.children.last().children.last().addChild(newNumChild.diff(weight1))

            result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
            result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "*"))
            result.children.last().children.last().children.last().addChild(children[0].children[0].clone())
            val newDenumChild = buildDiffNode(denum, variable)
            var weight2 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
            result.children.last().children.last().children.last().addChild(newDenumChild.diff(weight2))

            result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
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

private fun ExpressionNode.diffPow(variable: String, transformationWeight: MutableList<Double>): ExpressionNode {
    if (children[0].children.size < 2) return this.clone()

    var degree = children[0].children[1]
    if (children[0].children.size > 2) {
        degree = ExpressionNode(NodeType.FUNCTION, "^")
        for (i in 1..children[0].children.lastIndex) {
            degree.addChild(children[0].children[i])
        }
    }

    if (degree.getContainedVariables(setOf(variable)).isEmpty()) { // (f(x)^c)' = c*f^(c-1)*f'
        val result = ExpressionNode(NodeType.FUNCTION, "*")
        result.addChild(degree.clone())

        result.addChild(ExpressionNode(NodeType.FUNCTION, "^"))
        result.children.last().addChild(children[0].children[0].clone())
        result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
        result.children.last().children.last().addChild(degree.clone())
        result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
        result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))

        val newChild = buildDiffNode(children[0].children[0], variable)
        result.children.last().addChild(newChild.diff(transformationWeight))
        return result
    } else {
        if (children[0].children[0].getContainedVariables(setOf(variable)).isNotEmpty()) {
            if (transformationWeight.size > 1) { //unlimited differentiation actions count -> (f(x)^g(x))' = (e^(g*ln(f)))' = e^(g*ln(f)) * (g*ln(f))' = f^g * (g' * ln(f) + g * f' / f)
                val result = ExpressionNode(NodeType.FUNCTION, "+")

                result.addChild(ExpressionNode(NodeType.FUNCTION, "*"))
                result.children.last().addChild(children[0].clone())
                result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "ln"))
                result.children.last().children.last().addChild(children[0].children[0].clone())
                val newDegreeChild = buildDiffNode(degree, variable)
                var weight1 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
                result.children.last().addChild(newDegreeChild.diff(weight1))

                result.addChild(ExpressionNode(NodeType.FUNCTION, "*"))
                result.children.last().addChild(degree.clone())
                val newChild = buildDiffNode(children[0].children[0], variable)
                var weight2 = (mutableListOf(0.0) + transformationWeight.subList(1,transformationWeight.size)).toMutableList()
                result.children.last().addChild(newChild.diff(weight2))

                result.children.last().addChild(ExpressionNode(NodeType.FUNCTION, "^"))
                result.children.last().children.last().addChild(children[0].children[0].clone())
                result.children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                result.children.last().children.last().children.last().addChild(degree.clone())
                result.children.last().children.last().children.last().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
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
            val result = ExpressionNode(NodeType.FUNCTION, "*")
            result.addChild(children[0].clone())

            result.addChild(ExpressionNode(NodeType.FUNCTION, "ln"))
            result.children.last().addChild(children[0].children[0].clone())

            val newChild = buildDiffNode(degree, variable)
            result.addChild(newChild.diff(transformationWeight))
            return result
        }
    }
}