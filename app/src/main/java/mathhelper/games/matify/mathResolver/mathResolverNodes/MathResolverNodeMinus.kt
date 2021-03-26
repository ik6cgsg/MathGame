package mathhelper.games.matify.mathResolver.mathResolverNodes

import expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverNodeMinus(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        val elem = createNode(origin.children[0], getNeedBrackets(origin.children[0]), style, taskType)
        elem.setNodesFromExpression()
        children.add(elem)
        height = elem.height
        length += elem.length + 1
        baseLineOffset = elem.baseLineOffset
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        val currLen = if (!needBrackets) leftTop.x + 1 else leftTop.x + 2
        val child = children[0]
        child.setCoordinates(Point(currLen, leftTop.y + baseLineOffset - child.baseLineOffset))
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        val curStr = leftTop.y + baseLineOffset
        var curInd = leftTop.x
        if (needBrackets) {
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(curInd, "(")
            curInd++
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(rightBottom.x, ")")
        }
        val child = children[0]
        stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, op!!.name)
        child.getPlainNode(stringMatrix, spannableArray)
    }
}