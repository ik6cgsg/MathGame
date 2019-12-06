package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import android.text.SpannableString
import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverNodeFunction(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation? = null,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        val heights = ArrayList<Int>()
        length += op!!.name.length + 2
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node))
            elem.setNodesFromExpression()
            children.add(elem)
            heights.add(elem.height)
            length += elem.length
        }
        height = heights.max()!!
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = leftTop.x + op!!.name.length + 1
        for (child in children) {
            child.setCoordinates(Point(currLen, leftTop.y + (height - child.height) / 2))
            currLen += child.length
        }
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        val curStr = (leftTop.y + rightBottom.y) / 2
        var curInd = leftTop.x
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            if (ind == 0) {
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, op!!.name + "(")
                curInd += op!!.name.length + 1
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
            if (ind == children.size - 1) {
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, ")")
            }
        }
    }
}