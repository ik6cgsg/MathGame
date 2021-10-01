package mathhelper.games.matify.mathResolver.mathResolverNodes

import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.mathResolver.*

class MathResolverNodeLog(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        length += op!!.name.length + 2
        for (node in origin.children) {
            var mult = multiplier
            if (node == origin.children[1]) mult *= multiplierDif
            val elem = createNode(node, getNeedBrackets(node), style, taskType, mult )
            elem.setNodesFromExpression()
            children.add(elem)
            length += elem.length
        }
        baseLineOffset = children[0].baseLineOffset
        height = if (children[0].height >= children[1].height + baseLineOffset + 1) {
            children[0].height
        }
        else {
            children[1].height + children[0].baseLineOffset + 1
        }
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        val currLen = leftTop.x + op!!.name.length
        children[1].setCoordinates(Point(currLen, leftTop.y + 1 + baseLineOffset))
        children[0].setCoordinates(Point(currLen + children[1].length + 1, leftTop.y))
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        val curStr = leftTop.y + baseLineOffset
        stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(leftTop.x, op!!.name)
        BracketHandler.setBrackets(stringMatrix, spannableArray,
            Point(children[0].leftTop.x - 1, children[0].leftTop.y),
            Point(children[0].rightBottom.x + 1, children[0].rightBottom.y))
        if (multiplier < 1f) {
            spannableArray.add(SpanInfo(MatifyMultiplierSpan(multiplier), leftTop, rightBottom))
        }
        children[1].getPlainNode(stringMatrix, spannableArray)
        children[0].getPlainNode(stringMatrix, spannableArray)
    }
}