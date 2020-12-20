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
import mathhelper.games.matify.level.Type
import mathhelper.games.matify.mathResolver.MathResolver
import mathhelper.games.matify.mathResolver.MathResolverPair
import mathhelper.games.matify.mathResolver.TaskType

class GlobalMathView: TextView {
    private val TAG = "GlobalMathView"
    var expression: ExpressionNode? = null
        private set
    var currentAtoms: MutableList<ExpressionNode> = mutableListOf()
        private set
    var multiselectionMode = false
    var currentRulesToResult : Map<ExpressionSubstitution, ExpressionNode>? = null
    private var mathPair: MathResolverPair? = null
    private var type: Type = Type.OTHER

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        Log.d(TAG, "constructor from context")
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        Log.d(TAG, "constructor from attrs")
        setDefaults()
    }

    private fun setDefaults() {
        Log.d(TAG, "setDefaults")
        val themeName = Storage.shared.theme(context)
        setTextColor(ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_COLOR))
        typeface = Typeface.MONOSPACE
        textSize = Constants.centralExpressionDefaultSize
        setLineSpacing(0f, Constants.mathLineSpacing)
        setPadding(
            Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)
    }

    fun setExpression(expressionStr: String, type: Type) {
        Log.d(TAG, "setExpression from str")
        this.type = type
        if (expressionStr.isNotEmpty()) {
            expression = structureStringToExpression(expressionStr)
            textSize = Constants.centralExpressionDefaultSize
            currentAtoms = arrayListOf()
            setTextFromExpression()
        }
    }

    fun setExpression(expressionNode: ExpressionNode, type: Type, resetSize: Boolean = true) {
        Log.d(TAG, "setExpression from node")
        this.type = type
        expression = expressionNode
        if (resetSize) {
            textSize = Constants.centralExpressionDefaultSize
        }
        currentAtoms = arrayListOf()
        setTextFromExpression()
    }

    /** Scene interaction **/

    fun performSubstitutionForMultiselect(subst: ExpressionSubstitution): ExpressionNode? {
        Log.d(TAG, "performSubstitution")
        var res: ExpressionNode? = null
        if (expression == null || currentRulesToResult == null) {
            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
        } else {
            res = currentRulesToResult!![subst]
            expression = res!!.clone()
            setTextFromExpression()
            currentRulesToResult = null
            currentAtoms = arrayListOf()
        }
        return res
    }

    fun recolorCurrentAtom(color: Int) {
        val newText = SpannableString(text)
        val colorSpans = newText.getSpans<ForegroundColorSpan>(0, text.length)
        for (cs in colorSpans) {
            val start = newText.getSpanStart(cs)
            val end = newText.getSpanEnd(cs)
            newText.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        text = newText
    }

    fun clearExpression() {
        currentAtoms = arrayListOf()
        currentRulesToResult = null
        val colorSpans = mathPair!!.matrix.getSpans<ForegroundColorSpan>(0, mathPair!!.matrix.length)
        for (cs in colorSpans) {
            mathPair!!.matrix.removeSpan(cs)
        }
        val boldSpans = mathPair!!.matrix.getSpans<StyleSpan>(0, mathPair!!.matrix.length)
        for (bs in boldSpans) {
            mathPair!!.matrix.removeSpan(bs)
        }
        text = mathPair!!.matrix
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        super.onTouchEvent(event)
        if (event.pointerCount == 2) {
            return false
        }
        if (expression != null && AndroidUtil.touchUpInsideView(this, event)) {
            selectCurrentAtom(event)
        }
        return true
    }

    /** View OVERRIDES **/

    private fun selectCurrentAtom(event: MotionEvent) {
        Log.d(TAG, "selectCurrentAtom")
        val x = event.x - textSize / 4
        val y = event.y - textSize / 4
        if (layout != null) {
            val offset = getOffsetForPosition(x, y)

            val themeName = Storage.shared.theme(context)
            val atomColor = ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_HIGHLIGHT_COLOR)
            val atomSecondColor = ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_SECOND_HIGHLIGHT_COLOR)

            val atom = mathPair!!.getColoredAtom(offset, multiselectionMode, atomColor)
            if (atom != null) {
                if (multiselectionMode) {
                    if (currentAtoms.any { it.nodeId == atom.nodeId }) {
                        currentAtoms = currentAtoms.filter { it.nodeId != atom.nodeId }.toMutableList()
                    } else {
                        currentAtoms.add(atom)
                    }
                    val topNode = findLowestSubtreeTopOfSelectedNodesInExpression(expression!!, currentAtoms)
                    mathPair!!.recolorExpressionInMultiSelectionMode(currentAtoms, topNode, atomColor, atomSecondColor)
                } else {
                    currentAtoms.clear()
                    currentAtoms.add(atom)
                }
                text = mathPair!!.matrix
                PlayScene.shared.onAtomClicked()
            }
        }
    }

    fun deleteLastSelect() {
        val atom = currentAtoms.last()
        currentAtoms.remove(atom)
        mathPair!!.deleteSpanForAtom(atom)
        text = mathPair!!.matrix
        PlayScene.shared.onAtomClicked()
    }

    private fun setTextFromExpression() {
        Log.d(TAG, "setTextFromExpression")
        mathPair = when(type) {
            Type.SET -> MathResolver.resolveToPlain(expression!!, taskType = TaskType.SET)
            else -> MathResolver.resolveToPlain(expression!!)
        }
        text = mathPair!!.matrix
    }
}