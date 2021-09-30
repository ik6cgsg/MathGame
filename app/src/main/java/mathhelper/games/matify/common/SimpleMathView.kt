package mathhelper.games.matify.common

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.TypedArrayUtils.getText
import androidx.core.text.getSpans
import api.findLowestSubtreeTopOfSelectedNodesInExpression
import api.structureStringToExpression
import expressiontree.ExpressionNode
import expressiontree.ExpressionSubstitution
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.mathResolver.ExpressionType
import mathhelper.games.matify.mathResolver.MathResolver
import mathhelper.games.matify.mathResolver.MathResolverPair
import mathhelper.games.matify.mathResolver.TaskType

class SimpleMathView: androidx.appcompat.widget.AppCompatTextView {
    private val TAG = "SimpleMathView"
    var expression: ExpressionNode? = null
        private set
    private var mathPair: MathResolverPair? = null
    private var type: String? = ""

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        Logger.d(TAG, "constructor from context")
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        Logger.d(TAG, "constructor from attrs")
        setDefaults()
    }

    private fun setDefaults() {
        Logger.d(TAG, "setDefaults")
        val themeName = Storage.shared.theme()
        setTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
        typeface = Typeface.MONOSPACE
        textSize = Constants.simpleMathViewSize
        setLineSpacing(0f, Constants.mathLineSpacing)
        setPadding(
            Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)
    }

    fun setExpression(expressionStr: String, type: String?) {
        Logger.d(TAG, "setExpression from str")
        this.type = type
        if (expressionStr.isNotEmpty()) {
            expression = structureStringToExpression(expressionStr)
            textSize = Constants.simpleMathViewSize
            setTextFromExpression()
        } else {
            text = ""
        }
    }

    fun setExpression(expressionNode: ExpressionNode, type: String?, resetSize: Boolean = true) {
        Logger.d(TAG, "setExpression from node")
        this.type = type
        expression = expressionNode
        if (resetSize) {
            textSize = Constants.simpleMathViewSize
        }
        setTextFromExpression()
    }

    private fun setTextFromExpression() {
        Logger.d(TAG, "setTextFromExpression")
        mathPair = when(type) {
            TaskType.SET.str -> MathResolver.resolveToPlain(expression!!, taskType = TaskType.SET, exprType = ExpressionType.SIMPLE)
            else -> MathResolver.resolveToPlain(expression!!, exprType = ExpressionType.SIMPLE)
        }
        text = mathPair!!.matrix
    }
}