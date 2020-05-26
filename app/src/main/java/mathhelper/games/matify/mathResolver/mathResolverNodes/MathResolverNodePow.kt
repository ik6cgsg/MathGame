package mathhelper.games.matify.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverNodePow(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node), style, taskType)
            elem.setNodesFromExpression()
            children.add(elem)
            height += elem.height
            length += elem.length
        }
        baseLineOffset = height - (children[0].height - children[0].baseLineOffset)
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
        val curStr = leftTop.y + baseLineOffset
        if (needBrackets) {
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(leftTop.x, "(")
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(rightBottom.x, ")")
        }
        children.forEach { it.getPlainNode(stringMatrix, spannableArray) }
    }
}