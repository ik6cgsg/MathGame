package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverSetNodeNot(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {
    private val symbol = "—"

    override fun setNodesFromExpression()  {
        needBrackets = origin.children[0].nodeType == NodeType.FUNCTION
        super.setNodesFromExpression()
        val elem = createNode(origin.children[0], false)
        elem.setNodesFromExpression()
        children.add(elem)
        length += elem.length
        height = elem.height + 1
        baseLineOffset = height - 1
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        val currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        val child = children[0]
        child.setCoordinates(Point(currLen, leftTop.y + 1))
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        var curStr = leftTop.y + baseLineOffset
        if (needBrackets) {
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(leftTop.x, "(")
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(rightBottom.x, ")")
        }
        val child = children[0]
        val replacement = symbol.repeat(length)
        stringMatrix[leftTop.y] = stringMatrix[leftTop.y].replaceByIndex(leftTop.x, replacement)
        child.getPlainNode(stringMatrix, spannableArray)
    }
}