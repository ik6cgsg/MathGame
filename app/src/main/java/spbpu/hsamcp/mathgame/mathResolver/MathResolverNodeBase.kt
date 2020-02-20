package spbpu.hsamcp.mathgame.mathResolver

import android.graphics.Path
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType
import spbpu.hsamcp.mathgame.mathResolver.mathResolverNodes.*

open class MathResolverNodeBase(
    var origin: ExpressionNode,
    var needBrackets: Boolean = false,
    var op: Operation? = null,
    var length: Int = 0, var height: Int = 0
) {
    var children: ArrayList<MathResolverNodeBase> = ArrayList()
    lateinit var leftTop: Point
    lateinit var rightBottom: Point
    var baseLineOffset: Int = 0

    companion object {
        fun createNode(expression: ExpressionNode, needBrackets: Boolean): MathResolverNodeBase {
            return if (expression.nodeType == NodeType.VARIABLE) {
                MathResolverNodeBase(expression, false, null, expression.value.length, 1)
            } else {
                val operation = Operation(expression.value)
                when (operation.type) {
                    OperationType.DIV -> MathResolverNodeDiv(expression, needBrackets, operation)
                    OperationType.POW -> MathResolverNodePow(expression, needBrackets, operation)
                    OperationType.PLUS -> MathResolverNodePlus(expression, needBrackets, operation)
                    OperationType.MULT -> MathResolverNodeMult(expression, needBrackets, operation)
                    OperationType.FUNCTION -> MathResolverNodeFunction(expression, needBrackets, operation)
                    OperationType.MINUS -> MathResolverNodeMinus(expression, needBrackets, operation)
                    OperationType.SET_AND -> MathResolverSetNodeAnd(expression, needBrackets, operation)
                }
            }
        }

        fun getTree(expression: ExpressionNode): MathResolverNodeBase {
            val root = createNode(expression.children[0], false)
            root.setNodesFromExpression()
            root.setCoordinates(Point(0, 0))
            return root
        }
    }

    open fun setNodesFromExpression() {
        if (needBrackets) {
            length = 2
        }
    }

    open fun setCoordinates(leftTop: Point) {
        this.leftTop = leftTop
        rightBottom = Point(leftTop.x + length - 1, leftTop.y + height - 1)
    }

    open fun getPlainNode(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>) {
        stringMatrix[leftTop.y] = stringMatrix[leftTop.y].replaceByIndex(leftTop.x, origin.value)
    }

    fun getNeedBrackets(node: ExpressionNode): Boolean {
        return if (node.nodeType == NodeType.FUNCTION) {
            val nextPriority = Operation.getPriority(node.value)
            nextPriority != -1 && nextPriority < op!!.priority
        } else {
            false
        }
    }
}