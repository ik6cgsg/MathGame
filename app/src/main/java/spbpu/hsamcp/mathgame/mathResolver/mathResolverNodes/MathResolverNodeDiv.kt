package spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.*
import kotlin.math.ceil

class MathResolverNodeDiv(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation? = null,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {
    private var divSymbol = "\u2014"

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        var maxLen = 0
        for (node in origin.children) {
            val elem = createNode(node, false)
            elem.setNodesFromExpression()
            children.add(elem)
            height += elem.height + 1
            if (elem.length > maxLen) {
                maxLen = elem.length
                baseLineOffset = if (node != origin.children[origin.children.size - 1]) {
                    height - 1
                } else {
                    height - 2 - elem.height
                }
            }
        }
        height--
        length += maxLen
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currH = leftTop.y
        for (child in children) {
            var currLen = leftTop.x + ceil((length - child.length) / 2f).toInt()
            child.setCoordinates(Point(currLen, currH))
            currH += child.height + 1
        }
    }

    override fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        var curStr = leftTop.y
        var curInd = leftTop.x
        if (needBrackets) {
            val brStr = (rightBottom.y + curStr) / 2
            stringMatrix[brStr] = stringMatrix[brStr].replaceByIndex(curInd, "(")
            //spannableArray.add(SpanInfo(RelativeSizeSpan(1.5f), brStr, curInd, curInd + 1))
            curInd++
            stringMatrix[brStr] = stringMatrix[brStr].replaceByIndex(rightBottom.x, ")")
            //spannableArray.add(SpanInfo(RelativeSizeSpan(1.5f), brStr, rightBottom.x, rightBottom.x + 1))
        }
        children.forEachIndexed { ind: Int, child: MathResolverNodeBase ->
            child.getPlainNode(stringMatrix, spannableArray)
            curStr += child.height
            if (ind != children.size - 1) {
                val len = if (needBrackets) length - 2 else length
                val replacement = divSymbol.repeat(len)
                stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, replacement)
                curStr++
            }
        }
    }
}