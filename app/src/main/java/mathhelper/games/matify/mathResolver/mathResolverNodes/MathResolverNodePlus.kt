package mathhelper.games.matify.mathResolver.mathResolverNodes

import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType
import mathhelper.games.matify.mathResolver.*

class MathResolverNodePlus(
    origin: ExpressionNode,
    needBrackets: Boolean = false,
    op: Operation,
    length: Int = 0, height: Int = 0
) : MathResolverNodeBase(origin, needBrackets, op, length, height) {
    private var operators: ArrayList<String> = ArrayList()

    override fun setNodesFromExpression()  {
        super.setNodesFromExpression()
        var maxH = 0
        length += origin.children.size * op!!.name.length - 1
        origin.children.forEachIndexed { i, node ->
            lateinit var elem: MathResolverNodeBase
            if (node.nodeType == NodeType.FUNCTION &&
                    Operation(node.value).type == OperationType.MINUS && i != 0) {
                operators.add(node.value)
                var brackets = false
                if (node.children[0].nodeType == NodeType.FUNCTION &&
                    Operation(node.children[0].value).type == OperationType.PLUS) {
                    brackets = true
                }
                elem = createNode(node.children[0], brackets, style, taskType)
            } else {
                if (i != 0) {
                    operators.add(op!!.name)
                }
                elem = createNode(node, getNeedBrackets(node), style, taskType)
            }
            elem.setNodesFromExpression()
            if (elem is MathResolverNodeMinus && node != origin.children[0]) {
                elem.length -= elem.op!!.name.length
            }
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
        if (baseLineOffset == 0) {
            baseLineOffset = height - 1
        }
    }

    override fun setCoordinates(leftTop: Point) {
        super.setCoordinates(leftTop)
        var currLen = if (!needBrackets) leftTop.x else leftTop.x + 1
        for (child in children) {
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
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(curInd, operators[0])
                    curInd += operators[0].length
                    operators.removeAt(0)
            }
            child.getPlainNode(stringMatrix, spannableArray)
            curInd += child.length
        }
    }
}