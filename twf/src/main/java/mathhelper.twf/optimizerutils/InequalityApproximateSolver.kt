package mathhelper.twf.optimizerutils

import mathhelper.twf.baseoperations.BaseOperationsDefinitions
import mathhelper.twf.baseoperations.Segment
import mathhelper.twf.baseoperations.SegmentsUnion
import mathhelper.twf.baseoperations.VariableProperties
import mathhelper.twf.expressiontree.*

class InequalityApproximateSolver(
        private val baseOperationsDefinitions: BaseOperationsDefinitions = BaseOperationsDefinitions()
) {
    fun isLinear(expressionNode: ExpressionNode): Boolean {
        return true
    }

    fun isQuadratic(expressionNode: ExpressionNode): Boolean {
        return true
    }

    fun ExpressionNode.isConstant(variables: Set<String>): Boolean {
        if (variables.contains(value))
            return false
        for (child in children) {
            if (!child.isConstant(variables))
                return false
        }
        return true
    }

    fun ExpressionNode.isPolynom(variables: Set<String>): Boolean {
        if (children.isEmpty()) return true
        when (value) {
            in "", "+", "-", "*" -> for (child in children) {
                if (!child.isPolynom(variables))
                    return false
            }
            in "/", "^" -> {
                if (!children.first().isPolynom(variables))
                    return false
                for (i in 1 until children.size) {
                    if (!children[i].isConstant(variables))
                        return false
                }
            }
            else -> for (child in children) {
                if (!child.isConstant(variables))
                    return false
            }
        }
        return true
    }

    fun ExpressionNode.isPolynomThatCanHaveDivsOnlyOnTopLevel(variables: Set<String>): Boolean { //if node starts like this (...) * (...) / (...), where brackets contains polynoms, we also can solve inequality
        when (value) {
            in "*", "" -> for (child in children) {
                if (!child.isPolynomThatCanHaveDivsOnlyOnTopLevel(variables))
                    return false
            }
            in "/" -> for (child in children) {
                if (!child.isPolynom(variables))
                    return false
            }
            else -> return isPolynom(variables)
        }
        return true
    }

    fun ExpressionNode.findTopMultipliers(variable: String) {}


    fun ExpressionNode.findVariableNode(variable: String): Int {
        var variableChildNumber = -1
        for (i in 0..children.lastIndex) {
            if (!children[i].isConstant(setOf(variable))) {
                if (variableChildNumber < 0)
                    variableChildNumber = i
                else return children.size
            }
        }
        return variableChildNumber
    }

    fun ExpressionNode.fastTopResolver(variable: String): VariableProperties? {
        val result = VariableProperties(variable, mutableListOf(SegmentsUnion()))
        val variableChildNumber = findVariableNode(variable)
        if (variableChildNumber !in 0..children.lastIndex)
            return null
        val varChild = children[variableChildNumber]
        val root = ExpressionNode(NodeType.FUNCTION, "")
        if (varChild.value == variable) {
            when (value) {
                "+" -> {
                    root.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                    for (i in 0..children.lastIndex) {
                        if (i != variableChildNumber) {
                            if (children[i].value == "-") {
                                root.children.first().addChild(children[i].children.first().clone())
                            } else {
                                val additive = ExpressionNode(NodeType.FUNCTION, "-")
                                additive.addChild(children[i].clone())
                                root.children.first().addChild(additive)
                            }
                        }
                    }
                    result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                }
                "-" -> {
                    if (children.size == 1) {
                        root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                        result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                    } else {
                        if (variableChildNumber == 0) {
                            root.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                            for (i in 1..children.lastIndex) {
                                root.children.first().addChild(children[i].clone())
                            }
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                        } else {
                            if (children.size == 2)
                                root.addChild(children[0].clone())
                            else {
                                root.addChild(ExpressionNode(NodeType.FUNCTION, "-"))
                                for (i in 0..children.lastIndex) {
                                    if (i != variableChildNumber) {
                                        root.children.first().addChild(children[i].clone())
                                    }
                                }
                            }
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                        }
                    }
                }
                "*" -> {
                    if (children.size == 2) {
                        val mult = children[1 - variableChildNumber].value.toDoubleOrNull() ?: return null
                        if (mult > 0) {
                            root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                        } else if (mult < 0) {
                            root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                        } else result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment())
                    } else return null
                }
                "/" -> {
                    if (children.size == 2) {
                        val mult = children[1 - variableChildNumber].value.toDoubleOrNull() ?: return null
                        if (mult > 0) {
                            root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                        } else if (mult < 0) {
                            root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                        }
                    } else return null
                }
                "^" -> {
                    if (variableChildNumber == 0) {
                        root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                        result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                    } else if (children.size == 2) {
                        result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment())
                    } else return null
                }
                "exp" -> {
                    result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment())
                }
                "ln" -> {
                    root.addChild(ExpressionNode(NodeType.VARIABLE, "1"))
                    result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root, isLeftBorderIncluded = false))
                }
                "S" -> {
                    if (children.size != 4) return null
                    when (variableChildNumber) {
                        1, 2 -> {
                            if (children[3].value == children[0].value) {
                                root.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                                root.children.first().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
                                root.children.first().children.first().addChild(children[variableChildNumber - 1].clone())
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                            } else return null
                        }
                        3 -> {
                            val multNode = ExpressionNode(NodeType.FUNCTION, "-")
                            multNode.addChild(children[2].clone())
                            multNode.addChild(children[1].clone())
                            baseOperationsDefinitions.computeExpressionTree(multNode)
                            val mult = multNode.value.toDoubleOrNull() ?: return null
                            if (mult > 0) {
                                root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                            } else if (mult < 0) {
                                root.addChild(ExpressionNode(NodeType.VARIABLE, "0"))
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                            } else result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment())
                        }
                        else -> result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment())
                    }
                }
                else -> return null
            }
            return result
        } else if (children.size > 2)
            return null
        else if (value == "+") {
            val variableGrandSonNumber = varChild.findVariableNode(variable)
            val varGrandSon = varChild.children[variableGrandSonNumber]
            if (varGrandSon.value == variable) {
                when (varChild.value) {
                    "-" -> {
                        if (varChild.children.size == 1) {
                            root.addChild(children[1 - variableChildNumber].clone())
                            result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                        } else {
                            root.addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                            if (variableGrandSonNumber == 0) {
                                for (i in 1..varChild.children.lastIndex) {
                                    root.children.first().addChild(varChild.children[i].clone())
                                }
                                if (children[1 - variableChildNumber].value == "-") {
                                    root.children.first().addChild(children[1 - variableChildNumber].clone())
                                } else {
                                    root.children.first().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
                                    root.children.first().children.first().addChild(children[1 - variableChildNumber].clone())
                                }
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                            } else {
                                root.children.first().addChild(children[1 - variableChildNumber].clone())
                                if (varChild.children.size == 2)
                                    root.children.first().addChild(varChild.children[0].clone())
                                else {
                                    root.children.first().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
                                    for (i in 0..varChild.children.lastIndex) {
                                        if (i != variableGrandSonNumber) {
                                            root.children.first().children.first().addChild(varChild.children[i].clone())
                                        }
                                    }
                                }
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                            }
                        }
                    }
                    "*" -> {
                        if (varChild.children.size == 2) {
                            val mult = varChild.children[1 - variableGrandSonNumber].value.toDoubleOrNull() ?: return null
                            root.addChild(ExpressionNode(NodeType.FUNCTION, "/"))
                            if (children[1 - variableChildNumber].value == "-") {
                                root.children.first().addChild(children[1 - variableChildNumber].clone())
                            } else {
                                root.children.first().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                                root.children.first().children.first().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
                                root.children.first().children.first().children.first().addChild(children[1 - variableChildNumber].clone())
                            }
                            if (mult > 0) {
                                root.children.first().addChild(ExpressionNode(NodeType.VARIABLE, mult.toString()))
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                            } else if (mult < 0) {
                                root.children.first().addChild(ExpressionNode(NodeType.VARIABLE, (-mult).toString()))
                                result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                            } else {
                                val const = children[1 - variableChildNumber].value.toDoubleOrNull() ?: return null
                                if (const > 0)
                                    result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment())
                            }
                        } else return null
                    }
                    "/" -> {
                        if (children.size == 2) {
                            val mult = varChild.children[1 - variableGrandSonNumber].value.toDoubleOrNull() ?: return null
                            if (variableGrandSonNumber == 0) {
                                root.addChild(ExpressionNode(NodeType.FUNCTION, "*"))
                                if (children[1 - variableChildNumber].value == "-") {
                                    root.children.first().addChild(children[1 - variableChildNumber].clone())
                                } else {
                                    root.children.first().addChild(ExpressionNode(NodeType.FUNCTION, "+"))
                                    root.children.first().children.first().addChild(ExpressionNode(NodeType.FUNCTION, "-"))
                                    root.children.first().children.first().children.first().addChild(children[1 - variableChildNumber].clone())
                                }
                                if (mult > 0) {
                                    root.children.first().addChild(ExpressionNode(NodeType.VARIABLE, mult.toString()))
                                    result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(leftBorder = root))
                                } else if (mult < 0) {
                                    root.children.first().addChild(ExpressionNode(NodeType.VARIABLE, (-mult).toString()))
                                    result.segmentsUnionsIntersection.first().segmentsUnion.add(Segment(rightBorder = root))
                                }
                            } else return null
                        } else return null
                    }
                   //todo()
                    else -> return null
                }
                return result
            } else if (varChild.value == "-") {
                val variableGreatGrandSonNumber = varGrandSon.findVariableNode(variable)
                val varGreatGrandSon = varChild.children[variableGreatGrandSonNumber]
                if (varGreatGrandSon.value == variable) {
                    //todo()
                } else return null
            } else return null
        } else if (value == "-") {
            //todo()
        } else return null
        return result
    }

    fun solveExpressionMoreThanNull(expressionNode: ExpressionNode): Map<String, VariableProperties>? {
        val expression = expressionNode.cloneWithNormalization(sorted = false)
        baseOperationsDefinitions.computeExpressionTree(expression.children[0])
        val variables = expression.getVariableNames()
        val result = mutableMapOf<String, VariableProperties>()
        for (variable in variables) {
            val fastResolverResult = expression.fastTopResolver(variable)
            if (fastResolverResult == null) {
                if (!expression.isPolynomThatCanHaveDivsOnlyOnTopLevel(setOf(variable)))
                    result.put(variable, VariableProperties(variable, unableToCompute = true)) //todo (resolve this case)
                else {

                }
            } else {
                result.put(variable, fastResolverResult)
            }
        }
        return result
    }


}