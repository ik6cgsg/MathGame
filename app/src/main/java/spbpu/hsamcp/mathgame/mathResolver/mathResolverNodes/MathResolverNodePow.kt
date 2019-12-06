package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverNodePow(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation? = null,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node))
            elem.setNodesFromExpression()
            children.add(elem)
            height += elem.height
            length += elem.length
        }
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        var currH = rightBottom.y + 1
        for (child in children) {
            currH -= child.height
            child.setCoordinates(Point(currLen, currH))
            currLen += child.length
        }
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        children.forEach { it.getPlainNode(stringMatrix, spannableArray) }
    }
}