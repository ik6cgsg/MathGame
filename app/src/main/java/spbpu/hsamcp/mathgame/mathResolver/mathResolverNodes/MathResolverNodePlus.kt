package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverNodePlus(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation? = null,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        var maxH = 0
        length += origin.children.size * op!!.name.length - 1
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node))
            elem.setNodesFromExpression()
            if (elem is MathResolverNodeMinus && node != origin.children[0]) {
                elem.length -= elem.op!!.name.length
            }
            children.add(elem)
            length += elem.length
            if (elem.height > maxH) {
                maxH = elem.height
                if (elem.op != null && elem.op!!.type != OperationType.POW) {
                    baseLineOffset = elem.baseLineOffset
                }
            }
        }
        height = maxH
        if (baseLineOffset == 0) {
            baseLineOffset = height - 1
        }
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        for (child in children) {
            if (child is MathResolverNodeMinus && child != children[0]) {
                currLen -= op!!.name.length
            }
            child.setCoordinates(Point(currLen, leftTop.y + baseLineOffset - child.baseLineOffset))
            currLen += child.length + op!!.name.length
        }
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
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            if (ind != 0) {
                if (child is MathResolverNodeMinus) {
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, child.op!!.name)
                    curInd += child.op!!.name.length
                } else {
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, op!!.name)
                    curInd += op!!.name.length
                }
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}