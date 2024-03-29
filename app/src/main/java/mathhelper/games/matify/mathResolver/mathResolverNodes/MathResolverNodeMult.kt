package mathhelper.games.matify.mathResolver.mathResolverNodes

import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverNodeMult(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {
    private val symbol = MatifySymbols.mult

    override fun setNodesFromExpression()  {
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
                if (elem.op != null) {
                    baseLineOffset = elem.baseLineOffset
                }
            }
        }
        height = maxH
        if (baseLineOffset < 0) {
            baseLineOffset = height - 1
        }
        super.fixBaseline()
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
        if (multiplier < 1f) {
            spannableArray.add(SpanInfo(MatifyMultiplierSpan(multiplier), leftTop, rightBottom))
        }
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            if (ind != 0) {
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, symbol)
                curInd += symbol.length
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}