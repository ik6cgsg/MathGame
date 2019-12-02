package spbpu.hsamcp.mathgame.mathResolver

import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType

class MathResolver {
    companion object {
        private lateinit var stringMatrix: ArrayList<String>
        private var baseString = 0
        private lateinit var currentViewTree: MathResolverNode
        private var divSymbol = "\u2014"

        fun resolveToPlain(expression: ExpressionNode): String {
            currentViewTree = MathResolverNode.getTree(expression)
            return getPlainString()
        }

        fun resolveToPlain(expression: String): String {
            val realExpression = stringToExpression(expression)
            currentViewTree = MathResolverNode.getTree(realExpression)
            return getPlainString()
        }

        private fun getPlainString(): String {
            var result: String = ""
            // matrix init
            stringMatrix = ArrayList()
            for (i in 0 until currentViewTree.height) {
                stringMatrix.add(" ".repeat(currentViewTree.length))
            }
            baseString = currentViewTree.height / 2
            getPlainNode(currentViewTree)
            for (str in stringMatrix) {
                result += str + "\n"
            }
            return result
        }

        private fun getPlainNode(node: MathResolverNode) {
            if (node.origin.nodeType == NodeType.VARIABLE) {
                stringMatrix[node.leftTop.y] = stringMatrix[node.leftTop.y].replaceByIndex(node.leftTop.x,
                    node.origin.value)
            } else {
                when (val s = node.origin.value) {
                    "/" -> {
                        var curStr = node.leftTop.y
                        val curInd = node.leftTop.x
                        node.children.forEachIndexed { ind: Int, child: MathResolverNode ->
                            getPlainNode(child)
                            curStr += child.height
                            if (ind != node.children.size - 1) {
                                val replacement = divSymbol.repeat(node.length)
                                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, replacement)
                                curStr++
                            }
                        }
                    }
                    "^" -> {
                        // TODO:
                    }
                    "*", "+" -> {
                        val curStr = (node.leftTop.y + node.rightBottom.y) / 2
                        var curInd = node.leftTop.x
                        node.children.forEachIndexed { ind: Int, child: MathResolverNode ->
                            if (ind != 0) {
                                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, s)
                                curInd += s.length
                            }
                            getPlainNode(child)
                            curInd += child.length
                        }
                    }
                    "cos", "sin", "tg", "ctg" -> {
                        val curStr = (node.leftTop.y + node.rightBottom.y) / 2
                        var curInd = node.leftTop.x
                        node.children.forEachIndexed { ind: Int, child: MathResolverNode ->
                            if (ind == 0) {
                                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, s + "(")
                                curInd += s.length + 1
                            }
                            getPlainNode(child)
                            curInd += child.length
                            if (ind == node.children.size - 1) {
                                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, ")")
                            }
                        }
                    }
                }
            }
        }

        private fun String.replaceByIndex(i: Int, replacement: String): String {
            return this.substring(0, i) + replacement + this.substring(i + replacement.length)
        }
    }
}