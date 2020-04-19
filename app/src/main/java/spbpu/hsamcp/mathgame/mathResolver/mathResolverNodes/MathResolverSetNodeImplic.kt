package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import android.text.style.ScaleXSpan
import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*

class MathResolverSetNodeImplic(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    companion object {
        private var symbol = "→"
        private val mult: Float =
            fontPaint.measureText(checkSymbol) / fontPaint.measureText(symbol)
    }

    override fun setNodesFromExpression() {
        super.setNodesFromExpression()
        var maxH = 0
        length += origin.children.size * symbol.length - 1
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node), style)
            elem.setNodesFromExpression()
            children.add(elem)
            length += elem.length
            if (elem.height > maxH) {
                maxH = elem.height
            }
        }
        height = maxH
        baseLineOffset = height - 1
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        for (child in children) {
            child.setCoordinates(Point(currLen, leftTop.y + baseLineOffset - child.baseLineOffset))
            currLen += child.length + symbol.length
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
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, symbol)
                spannableArray.add(SpanInfo(ScaleXSpan(mult), curStr, curInd, curInd + symbol.length))
                curInd += symbol.length
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}