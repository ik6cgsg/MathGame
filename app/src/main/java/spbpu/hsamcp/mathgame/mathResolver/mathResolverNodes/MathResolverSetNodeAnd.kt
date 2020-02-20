package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverSetNodeAnd(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {
    private val symbol = "∧"

    override fun setNodesFromExpression() {
        super.setNodesFromExpression()
        length += origin.children.size * symbol.length - 1
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node))
            elem.setNodesFromExpression()
            children.add(elem)
            length += elem.length
        }
        height = 1
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        for (child in children) {
            child.setCoordinates(Point(currLen, 0))
            currLen += child.length + symbol.length
        }
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        var curInd = leftTop.x
        if (needBrackets) {
            stringMatrix[0] =
                stringMatrix[0].replaceByIndex(curInd, "(")
            curInd++
            stringMatrix[0] =
                stringMatrix[0].replaceByIndex(rightBottom.x, ")")
        }
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            if (ind != 0) {
                stringMatrix[0] = stringMatrix[0].replaceByIndex(curInd, symbol)
                curInd += symbol.length
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}