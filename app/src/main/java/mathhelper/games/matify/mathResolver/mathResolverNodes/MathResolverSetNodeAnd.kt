package mathhelper.games.matify.mathResolver.mathResolverNodes

import android.text.style.ScaleXSpan
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverSetNodeAnd(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {
    private val symbol = MatifySymbols.setAnd

    override fun setNodesFromExpression() {
        super.setNodesFromExpression()
        var maxH = 0
        length += (origin.children.size - 1) * symbol.length
        for (node in origin.children) {
            val elem = createNode(node, getNeedBrackets(node), style, taskType)
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
            curInd++
            BracketHandler.setBrackets(stringMatrix, spannableArray, leftTop, rightBottom)
        }
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            if (ind != 0) {
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, symbol)
                val off = (stringMatrix[0].length + 1) * curStr
                //MatifySpan.setSizeMultifyer(off + curInd, off + curInd + symbol.length, 0.8f)
                curInd += symbol.length
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}