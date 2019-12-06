package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import android.text.SpannableString
import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverNodeMult(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation? = null,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        val heights = ArrayList<Int>()
        length += origin.children.size * op!!.name.length - 1
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node))
            elem.setNodesFromExpression()
            heights.add(elem.height)
            children.add(elem)
            length += elem.length
        }
        height = heights.max()!!
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        for (child in children) {
            child.setCoordinates(Point(currLen, leftTop.y + (height - child.height) / 2))
            currLen += child.length + op!!.name.length
        }
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
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            if (ind != 0) {
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, op!!.name)
                curInd += op!!.name.length
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}