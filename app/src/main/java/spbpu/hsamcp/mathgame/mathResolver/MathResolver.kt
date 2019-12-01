package spbpu.hsamcp.mathgame.mathResolver

import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType

class MathResolver {
    companion object {
        private lateinit var stringMatrix: ArrayList<String>
        private var currentString: Int = 0
        private var currentIndex: Int = 0
        private var baseString = 0
        private lateinit var currentViewTree: MathResolverNode
        private var divSymbol = "\u2212"

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
            currentString = stringMatrix.size - 1
            currentIndex = 0
            // TODO: get normal base string
            baseString = currentViewTree.height / 2
            getPlainNode(currentViewTree)
            for (str in stringMatrix) {
                result += str + "\n"
            }
            return result
        }

        private fun getPlainNode(node: MathResolverNode) {
            if (node.origin.nodeType == NodeType.VARIABLE) {
                currentString = node.leftTop.y
                currentIndex = node.leftTop.x
                stringMatrix[currentString] =
                    stringMatrix[currentString].replaceRange(currentIndex, currentIndex + node.length, node.origin.value)
            } else {
                node.children.forEachIndexed { ind: Int, child: MathResolverNode ->
                    when (val s = node.origin.value) {
                        "+" -> {
                            currentString = node.height / 2
                            if (ind != 0) {
                                stringMatrix[currentString] =
                                    stringMatrix[currentString].replaceRange(currentIndex, currentIndex + 1, s)
                                currentIndex++
                            }
                            getPlainNode(child)
                            currentIndex++
                        }
                        "/" -> {
                            getPlainNode(child)
                            currentIndex = node.leftTop.x
                            if (ind != node.children.size - 1) {
                                val replacement = divSymbol.repeat(node.length)
                                stringMatrix[child.height] =
                                    stringMatrix[child.height].replaceRange(currentIndex, currentIndex + node.length,
                                        replacement)
                            } else {
                                currentIndex = node.leftTop.x + node.length - 1
                            }
                        }
                    }
                }
            }
        }
    }
}