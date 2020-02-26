package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import com.twf.api.expressionSubstitutionFromStrings
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.mathResolver.MathResolver

class RuleMathView: TextView {
    private val TAG = "RuleMathView"
    private val moveTreshold = 10
    var subst: ExpressionSubstitution? = null
        private set
    private var needClick = false
    private var moveCnt = 0

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
        textSize = Constants.ruleDefaultSize
        setHorizontallyScrolling(true)
        isHorizontalScrollBarEnabled = true
        isScrollbarFadingEnabled = true
        movementMethod = ScrollingMovementMethod()
        setTextColor(Color.LTGRAY)
        typeface = Typeface.MONOSPACE
        setLineSpacing(0f, Constants.mathLineSpacing)
        setPadding(Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)
        if (subst != null) {
            setSubst(subst!!)
        }
    }

    fun setSubst(subst: ExpressionSubstitution) {
        this.subst = subst
        val from = MathResolver.resolveToPlain(subst.left)
        val to = MathResolver.resolveToPlain(subst.right)
        val textStr = MathResolver.getRule(from, to)
        //maxLines = 1 + textStr.count { it == '\n' }
        text = textStr
    }

    /** TextView OVERRIDES **/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        super.onTouchEvent(event)
        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "ACTION_DOWN")
                needClick = true
                moveCnt = 0
                setBackgroundColor(Constants.lightGrey)
            }
            event.action == MotionEvent.ACTION_UP -> {
                Log.d(TAG, "ACTION_UP")
                if (needClick && AndroidUtil.touchUpInsideView(this, event)) {
                    MathScene.currentRuleView = this
                    MathScene.onRuleClicked()
                    needClick = false
                } else {
                    setBackgroundColor(Color.TRANSPARENT)
                }
            }
            event.action == MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "ACTION_MOVE")
                if (needClick) {
                    moveCnt++
                    if (moveCnt > moveTreshold) {
                        needClick = false
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                } else {
                    setBackgroundColor(Color.TRANSPARENT)
                }
            }
            event.action == MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "ACTION_CANCEL")
                needClick = false
                setBackgroundColor(Color.TRANSPARENT)
            }
        }
        return true
    }

    /** UTILS **/
}