package spbpu.hsamcp.mathgame.mathResolver

import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType
import kotlin.math.ceil

data class MathResolverNode(
    var origin: ExpressionNode,
    var length: Int = 0, var height: Int = 0
) {
    var children: ArrayList<MathResolverNode> = ArrayList()
    lateinit var leftTop: Point
    lateinit var rightBottom: Point

    companion object {
        fun getTree(expression: ExpressionNode): MathResolverNode {
            val root = setNodesFrom(expression.children[0])
            setCoordinates(root, Point(0, 0))
            return root
        }

        private fun setNodesFrom(expression: ExpressionNode): MathResolverNode {
            val currentNode = MathResolverNode(expression)
            if (expression.nodeType == NodeType.VARIABLE) {
                currentNode.length = expression.value.length
                currentNode.height = 1
            } else {
                when (expression.value) {
                    "+" -> {
                        val heights = ArrayList<Int>()
                        currentNode.length = expression.children.size - 1
                        for (node in expression.children) {
                            val elem = setNodesFrom(node)
                            currentNode.children.add(elem)
                            heights.add(elem.height)
                            currentNode.length += elem.length
                        }
                        currentNode.height = heights.max()!!
                    }
                    "/" -> {
                        val lengths = ArrayList<Int>()
                        currentNode.height = expression.children.size - 1
                        for (node in expression.children) {
                            val elem = setNodesFrom(node)
                            currentNode.children.add(elem)
                            currentNode.height += elem.height
                            lengths.add(elem.length)
                        }
                        currentNode.length = lengths.max()!!
                    }
                }
            }
            return currentNode
        }

        private fun setCoordinates(node: MathResolverNode, leftTop: Point) {
            node.leftTop = leftTop
            node.rightBottom = Point(leftTop.x + node.length - 1, leftTop.y + node.height - 1)
            if (node.origin.nodeType != NodeType.VARIABLE) {
                when (node.origin.value) {
                    "+" -> {
                        var currLen = leftTop.x
                        for (child in node.children) {
                            setCoordinates(child, Point(currLen, (node.height - child.height) / 2))
                            currLen += child.length + 1
                        }
                    }
                    "/" -> {
                        var currH = leftTop.y
                        for (child in node.children) {
                            setCoordinates(child, Point(leftTop.x +
                                ceil((node.length - child.length) / 2f).toInt(), currH))
                            currH += child.height + 1
                        }
                    }
                }
            }
        }
    }
}