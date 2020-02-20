package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import android.view.MotionEvent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.twf.api.*
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.mathResolver.MathResolverPair

class GlobalMathView: TextView {
    private val TAG = "GlobalMathView"
    var formula: ExpressionNode? = null
        private set
    var currentAtom: ExpressionNode? = null
        private set
    private var mathPair: MathResolverPair? = null
    val defaultSize = 26f
    val maxSize = 34f
    private val defaultPadding: Int = 10

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
        setTextColor(Color.LTGRAY)
        typeface = Typeface.MONOSPACE
        textSize = defaultSize
        setLineSpacing(0f, 0.5f)
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
    }

    fun setFormula(formulaStr: String) {
        Log.d(TAG, "setFormula from str")
        if (formulaStr.isNotEmpty()) {
            formula = stringToExpression(formulaStr)
            setTextFromFormula()
        }
    }

    fun setFormula(formulaNode: ExpressionNode) {
        Log.d(TAG, "setFormula from node")
        formula = formulaNode
        currentAtom = null
        setTextFromFormula()
    }

    /** Scene interaction **/
    fun performSubstitution(subst: ExpressionSubstitution): ExpressionNode? {
        Log.d(TAG, "performSubstitution")
        var res: ExpressionNode? = null
        if (formula == null || currentAtom == null) {
            Toast.makeText(context, "Error: no atom!", Toast.LENGTH_SHORT).show()
        } else {
            val substitutionPlaces = findSubstitutionPlacesInExpression(formula!!, subst)
            if (substitutionPlaces.isNotEmpty()) {
                val substPlace = substitutionPlaces.find {
                    expressionToString(it.originalValue) == expressionToString(currentAtom!!)
                }
                if (substPlace != null) {
                    applySubstitution(formula!!, subst, listOf(substPlace))
                    setTextFromFormula()
                    res = formula!!.clone()
                    currentAtom = null
                }
            }
        }
        return res
    }

    fun recolorCurrentAtom(color: Int) {
        val newText = SpannableString(text)
        val colorSpans = (text as SpannedString).getSpans(0, text.length, ForegroundColorSpan::class.java)
        for (cs in colorSpans) {
            val start = (text as SpannedString).getSpanStart(cs)
            val end = (text as SpannedString).getSpanEnd(cs)
            newText.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        text = newText
    }

    fun clearFormula() {
        currentAtom = null
        text = text.toString()
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        super.onTouchEvent(event)
        if (event.pointerCount == 2) {
            return false
        }
        if (formula != null) {
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.x >= left && event.x <= right && event.y >= top && event.y <= bottom) {
                    selectCurrentAtom(event)
                }
            }
        }
        return true
    }

    /** View OVERRIDES **/

    /** UTILS **/
    private fun selectCurrentAtom(event: MotionEvent) {
        Log.d(TAG, "selectCurrentAtom")
        val x = event.x - textSize / 4
        val y = event.y - textSize / 4
        if (layout != null) {
            val offset = getOffsetForPosition(x, y)
            val atom = mathPair!!.getColoredAtom(offset, Color.CYAN)
            if (atom != null) {
                val atomStr = expressionToString(atom)
                var curAtomStr = ""
                if (currentAtom != null) {
                    curAtomStr = expressionToString(currentAtom!!)
                }
                if (atomStr != curAtomStr) {
                    currentAtom = atom
                    text = mathPair!!.matrix
                    MathScene.onFormulaClicked()
                }
            }
        }
    }

    private fun setTextFromFormula() {
        Log.d(TAG, "setTextFromFormula")
        mathPair = MathResolver.resolveToPlain(formula!!)
        text = mathPair!!.matrix
        setLines(text.length / mathPair!!.tree!!.length)
    }
}