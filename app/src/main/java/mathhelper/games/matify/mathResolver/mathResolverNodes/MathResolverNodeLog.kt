package mathhelper.games.matify.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverNodeLog(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        length += op!!.name.length
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node), style, taskType)
            elem.setNodesFromExpression()
            children.add(elem)
            height += elem.height
            length += elem.length
        }
        baseLineOffset = children[0].height - (children[1].height - children[0].baseLineOffset)
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = (if (!needBrackets) leftTop.x else leftTop.x + 1) + op!!.name.length
        var currH = rightBottom.y + 1 - children[1].height
        children[1].setCoordinates(Point(currLen, currH - children[0].height + children[1].height))
        children[0].setCoordinates(Point(currLen + children[1].length, currH - children[0].height))
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        val curStr = leftTop.y + baseLineOffset
        var curInd = leftTop.x
        stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, op!!.name)
        curInd += op!!.name.length
        if (needBrackets) {
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(leftTop.x, "(")
            stringMatrix[curStr] =
                stringMatrix[curStr].replaceByIndex(rightBottom.x, ")")
        }
        children.forEach { it.getPlainNode(stringMatrix, spannableArray) }
    }
}