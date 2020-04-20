package spbpu.hsamcp.mathgame.common

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import android.view.MotionEvent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.core.text.getSpans
import com.twf.api.*
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.MathScene
import spbpu.hsamcp.mathgame.level.Type
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.mathResolver.MathResolverPair

class GlobalMathView: TextView {
    private val TAG = "GlobalMathView"
    var formula: ExpressionNode? = null
        private set
    var currentAtom: ExpressionNode? = null
        private set
    private var mathPair: MathResolverPair? = null

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
        textSize = Constants.centralFormulaDefaultSize
        setLineSpacing(0f, Constants.mathLineSpacing)
        setPadding(
            Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)
    }

    fun setFormula(formulaStr: String, type: Type) {
        Log.d(TAG, "setFormula from str")
        if (formulaStr.isNotEmpty()) {
            formula = when (type) {
                Type.SET -> stringToExpression(formulaStr, type.str)
                else -> stringToExpression(formulaStr)
            }
            textSize = Constants.centralFormulaDefaultSize
            currentAtom = null
            setTextFromFormula()
        }
    }

    fun setFormula(formulaNode: ExpressionNode, resetSize: Boolean = true) {
        Log.d(TAG, "setFormula from node")
        formula = formulaNode
        if (resetSize) {
            textSize = Constants.centralFormulaDefaultSize
        }
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
                    it.originalValue.nodeId == currentAtom!!.nodeId
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
        val colorSpans = newText.getSpans<ForegroundColorSpan>(0, text.length)
        for (cs in colorSpans) {
            val start = newText.getSpanStart(cs)
            val end = newText.getSpanEnd(cs)
            newText.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        text = newText
    }

    fun clearFormula() {
        currentAtom = null
        val newText = SpannableString(text)
        val colorSpans = newText.getSpans<ForegroundColorSpan>(0, text.length)
        for (cs in colorSpans) {
            newText.removeSpan(cs)
        }
        val boldSpans = newText.getSpans<StyleSpan>(0, text.length)
        for (bs in boldSpans) {
            newText.removeSpan(bs)
        }
        text = newText
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        super.onTouchEvent(event)
        if (event.pointerCount == 2) {
            return false
        }
        if (formula != null && AndroidUtil.touchUpInsideView(this, event)) {
            selectCurrentAtom(event)
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
                if (currentAtom == null || currentAtom!!.nodeId != atom.nodeId) {
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
    }
}