package mathhelper.games.matify.mathResolver.mathResolverNodes

import expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverNodePow(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        for (node in origin.children) {
            var mult = multiplier
            if (node != origin.children[0]) mult *= multiplierDif
            val elem = createNode(node, getNeedBrackets(node), style, taskType, mult)
            elem.setNodesFromExpression()
            children.add(elem)
            height += elem.height
            length += elem.length
        }
        baseLineOffset = height - (children[0].height - children[0].baseLineOffset)
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        var currH = rightBottom.y + 1
        for (child in children) {
            currH -= child.height
            child.setCoordinates(Point(currLen, currH))
            currLen += child.length
        }
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        if (needBrackets) {
            BracketHandler.setBrackets(stringMatrix, spannableArray, leftTop, rightBottom)
        }
        if (multiplier < 1f) {
            spannableArray.add(SpanInfo(MatifyMultiplierSpan(multiplier), leftTop, rightBottom))
        }
        children.forEach { it.getPlainNode(stringMatrix, spannableArray) }
    }
}