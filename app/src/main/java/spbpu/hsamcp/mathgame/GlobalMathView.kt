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
import android.text.style.SuperscriptSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.twf.api.*
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.SubstitutionPlace

class GlobalMathView: TextView {
    private val TAG = "GlobalMathView"
    private var formula: ExpressionNode? = null
    private var currentAtom: ExpressionNode? = null
    private val defaultFontSizeDp: Int = 12
    private val defaultPadding: Int = 10

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setDefaults()
    }

    private fun setDefaults() {
        if (textSize.compareTo(0) != 0) {
            textSize = convertDpToPx(defaultFontSizeDp)
        }
        setTextColor(Color.BLACK)
        typeface = Typeface.MONOSPACE
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        if (text != null) {
            formula = stringToExpression(text.toString())
        }
    }

    /** Scene interaction **/
    fun performSubstitution(substFrom: String, substTo: String) {
        if (formula == null) {
            Toast.makeText(context, "Error: no formula!", Toast.LENGTH_SHORT).show()
        } else {
            val substitution = expressionSubstitutionFromStrings(substFrom, substTo)
            val substitutionPlaces = findSubstitutionPlacesInExpression(formula!!, substitution)
            if (substitutionPlaces.isEmpty()) {
                Toast.makeText(context, "Error: cant do this substitution!", Toast.LENGTH_SHORT).show()
            } else if (currentAtom == null) {
                val applicationResult = applySubstitution(formula!!, substitution, substitutionPlaces)
                setTextFromFormula()
            } else {
                val substPlace = substitutionPlaces.find { it.originalValue == currentAtom!! }
                if (substPlace == null) {
                    Toast.makeText(context, "Error: cant do this substitution on selected atom", Toast.LENGTH_SHORT).show()
                } else {
                    val applicationResult = applySubstitution(formula!!, substitution, listOf(substPlace))
                    setTextFromFormula()
                }
            }
        }
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        var res = false
        if (formula != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "MotionEvent.ACTION_DOWN")
                    selectCurrentAtom(event)
                    res = true
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.d(TAG, "MotionEvent.ACTION_MOVE")
                    //selectCurrentAtom(event)
                    res = true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "MotionEvent.ACTION_UP")
                    // reset all styles
                    // TODO: if inside make substitution
                    //text = text.toString()
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
    private fun selectCurrentAtom(event: MotionEvent) {
        val x = event.x
        val y = event.y.toInt()
        if (layout == null) {
            setTextFromFormula()
        } else {
            val line = layout.getLineForVertical(y)
            val offset = layout.getOffsetForHorizontal(line, x)
            val expression = stringToExpression(text.toString())
            val atom = searchNodeByOffset(expression, offset)
            if (atom != null && atom != currentAtom) {
                currentAtom = atom
                val newText = SpannableString(text.toString())
                newText.setSpan(ForegroundColorSpan(Color.CYAN), currentAtom!!.startPosition,
                    currentAtom!!.endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                // TODO: clear & set span instead of creating new text
                text = newText
            }
        }
    }

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

    private fun setTextFromFormula() {
        val str = expressionToString(formula!!).drop(1).dropLast(1)
        text = str
    }

    private fun convertDpToPx(dp: Int): Float {
        return Math.round(dp * context.resources.displayMetrics.xdpi /
            DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }
}