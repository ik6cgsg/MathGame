package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.twf.api.expressionSubstitutionFromStrings
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.mathResolver.MathResolver

class RuleMathView: TextView {
    private val TAG = "RuleMathView"
    var subst: ExpressionSubstitution? = null
        private set
    private var defaultSize = 22f
    private val defaultPadding: Int = 15

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val params = context.obtainStyledAttributes(attrs, R.styleable.RuleMathView)
        val substFrom = params.getString(R.styleable.RuleMathView_substFrom)
        val substTo = params.getString(R.styleable.RuleMathView_substTo)
        subst = expressionSubstitutionFromStrings(substFrom!!, substTo!!)
        setDefaults()
    }

    private fun setDefaults() {
        textSize = defaultSize
        setTextColor(Color.LTGRAY)
        typeface = Typeface.MONOSPACE
        setLineSpacing(0f, 0.5f)
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        if (subst != null) {
            setSubst(subst!!)
        }
    }

    fun setSubst(subst: ExpressionSubstitution) {
        this.subst = subst
        val from = MathResolver.resolveToPlain(subst.left)
        val to = MathResolver.resolveToPlain(subst.right)
        val textStr = MathResolver.getRule(from, to)
        text = textStr
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        super.onTouchEvent(event)
        // TODO: color highlight
        if (event.action == MotionEvent.ACTION_UP) {
            MathScene.currentRuleView = this
            MathScene.onRuleClicked()
        }
        return true
    }

    /** UTILS **/
}