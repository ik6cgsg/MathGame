package spbpu.hsamcp.mathgame.mathResolver

import android.graphics.Paint
import android.graphics.Typeface
import android.text.style.ScaleXSpan
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.NodeType
import spbpu.hsamcp.mathgame.common.Constants
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
    lateinit var style: VariableStyle
    lateinit var taskType: TaskType
    private var customized = false
    private lateinit var outputValue: String

    companion object {
        var checkSymbol = "A"
        var fontPaint: Paint = {
            val fp = Paint(Paint.ANTI_ALIAS_FLAG)
            fp.textSize = Constants.centralExpressionDefaultSize
            fp.typeface = Typeface.MONOSPACE
            fp.style = Paint.Style.STROKE
            fp
        }()

        fun createNode(expression: ExpressionNode, needBrackets: Boolean,
                       style: VariableStyle, taskType: TaskType): MathResolverNodeBase {
            val node = if (expression.nodeType == NodeType.VARIABLE) {
                val (value, customized) = CustomSymbolsHandler.getPrettyValue(expression, style, taskType)
                val variable = MathResolverNodeBase(expression, false, null, value.length, 1)
                variable.customized = customized
                variable.outputValue =  if (customized) {
                    value
                } else {
                    expression.value
                }
                variable
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
                    OperationType.SET_OR -> MathResolverSetNodeOr(expression, needBrackets, operation)
                    OperationType.SET_MINUS -> MathResolverSetNodeMinus(expression, needBrackets, operation)
                    OperationType.SET_NOT -> MathResolverSetNodeNot(expression, needBrackets, operation)
                    OperationType.SET_IMPLIC -> MathResolverSetNodeImplic(expression, needBrackets, operation)
                }
            }
            node.style = style
            node.taskType = taskType
            return node
        }

        fun getTree(expression: ExpressionNode, style: VariableStyle, taskType: TaskType): MathResolverNodeBase {
            val root = createNode(expression.children[0], false, style, taskType)
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
        stringMatrix[leftTop.y] = stringMatrix[leftTop.y].replaceByIndex(leftTop.x, outputValue)
        if (customized) {
            val checkStr = checkSymbol.repeat(outputValue.length)
            val numberMult = fontPaint.measureText(checkStr) / fontPaint.measureText(outputValue)
            spannableArray.add(SpanInfo(ScaleXSpan(numberMult), leftTop.y, leftTop.x, leftTop.x + outputValue.length))
        }
    }

    fun getNeedBrackets(node: ExpressionNode): Boolean {
        return if (node.nodeType == NodeType.FUNCTION) {
            val nextPriority = Operation.getPriority(node.value)
            nextPriority != -1 && nextPriority <= op!!.priority
        } else {
            false
        }
    }
}