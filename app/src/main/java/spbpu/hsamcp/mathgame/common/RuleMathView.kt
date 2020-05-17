package spbpu.hsamcp.mathgame.common

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
import spbpu.hsamcp.mathgame.LevelScene
import spbpu.hsamcp.mathgame.PlayScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.level.Type
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.mathResolver.TaskType
import spbpu.hsamcp.mathgame.mathResolver.VariableStyle

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
        setPadding(
            Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)
        if (subst != null) {
            setSubst(subst!!, Type.OTHER)
        }
    }

    fun setSubst(subst: ExpressionSubstitution, type: Type) {
        this.subst = subst
        val from = when (type) {
            Type.SET -> MathResolver.resolveToPlain(subst.left, VariableStyle.GREEK, TaskType.SET)
            else -> MathResolver.resolveToPlain(subst.left, VariableStyle.GREEK)
        }
        val to = when (type) {
            Type.SET -> MathResolver.resolveToPlain(subst.right, VariableStyle.GREEK, TaskType.SET)
            else -> MathResolver.resolveToPlain(subst.right, VariableStyle.GREEK)
        }
        val textStr = MathResolver.getRule(from, to)
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
                    PlayScene.shared.currentRuleView = this
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