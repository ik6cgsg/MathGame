package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverNodeMinus(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation? = null,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        val elem = createNode(origin.children[0], getNeedBrackets(origin.children[0]))
        elem.setNodesFromExpression()
        children.add(elem)
        height = elem.height
        length += elem.length + 1
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        val currLen = if (!needBrackets) leftTop.x + 1 else leftTop.x + 2
        val child = children[0]
        child.setCoordinates(Point(currLen, leftTop.y + (height - child.height) / 2))
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        val curStr = (leftTop.y + rightBottom.y) / 2
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