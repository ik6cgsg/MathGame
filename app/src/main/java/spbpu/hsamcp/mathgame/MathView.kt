package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.TextView
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.twf.api.expressionToString
import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode

class MathView: TextView {
    private val TAG = "MathView"
    private var formula: ExpressionNode? = null
    private val defaultFontSizeDp: Int = 18
    private val defaultPadding: Int = 10

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setDefaults()
    }

    private fun setDefaults() {
        textSize = convertDpToPx(defaultFontSizeDp)
        setTextColor(Color.BLACK)
        typeface = Typeface.MONOSPACE
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        if (text != null) {
            formula = stringToExpression(text.toString())
        }
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var res = false
        if (formula != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "MotionEvent.ACTION_DOWN")
                    res = true
                    /*
                    val spans = formula!!.getSpans<ForegroundColorSpan>(0, formula!!.length)
                    if (spans.isEmpty()) {
                        formula = SpannableString(formula.toString())
                        formula!!.setSpan(ForegroundColorSpan(Color.RED), 0, formula!!.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        text = formula
                        res = true
                    }
                    */
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.d(TAG, "MotionEvent.ACTION_MOVE")
                    /** TODO:
                     * [] get offset from coords
                     * [] get indices of current atom from twf
                     * [] add ForegroudSpan on theese indices
                     **/
                    val x = event.x
                    val y = event.y.toInt()
                    if (layout == null) {
                        text = expressionToString(formula!!)
                        text = text.drop(1)
                        text = text.dropLast(1)
                    } else {
                        val line = layout.getLineForVertical(y)
                        val offset = layout.getOffsetForHorizontal(line, x)
                        val expression = stringToExpression(text.toString())
                        val atom = searchNodeByOffset(expression, offset)
                        val newText = SpannableString(text.toString())
                        if (atom != null) {
                            newText.setSpan(ForegroundColorSpan(Color.CYAN), atom.startPosition,
                                atom.endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        } else {
                            newText.setSpan(ForegroundColorSpan(Color.RED), 0, newText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        text = newText
                    }
                    res = true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "MotionEvent.ACTION_UP")
                    // reset all styles
                    // TODO: if inside make substitution
                    text = text.toString()
                    res = true
                }
            }
        }
        return res
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        formula = stringToExpression(text.toString())
    }

    /** View OVERRIDES **/

    /** UTILS **/
    private fun searchNodeByOffset(expression: ExpressionNode, offset: Int): ExpressionNode? {
        var resNode: ExpressionNode? = null
        for (node in expression.children) {
            if (node.startPosition <= offset && node.endPosition >= offset) {
                if (node.getCountOfNodes() == 0) {
                    resNode = node
                    break
                }
                resNode = searchNodeByOffset(node, offset) ?: node
            }
        }
        return resNode
    }

    private fun convertDpToPx(dp: Int): Float {
        return Math.round(dp * context.resources.displayMetrics.xdpi /
            DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }
}