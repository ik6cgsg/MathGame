package mathhelper.games.matify.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import com.twf.api.expressionSubstitutionFromStructureStrings
import com.twf.expressiontree.ExpressionSubstitution
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.level.Type
import mathhelper.games.matify.mathResolver.MathResolver
import mathhelper.games.matify.mathResolver.TaskType
import mathhelper.games.matify.mathResolver.VariableStyle
import java.lang.Exception

class RuleMathView: androidx.appcompat.widget.AppCompatTextView {
    private val TAG = "RuleMathView"
    private val moveTreshold = 10
    var subst: ExpressionSubstitution? = null
        private set
    private var needClick = false
    private var moveCnt = 0

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        setDefaults(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val params = context.obtainStyledAttributes(attrs, R.styleable.RuleMathView)
        val substFrom = params.getString(R.styleable.RuleMathView_substFrom)
        val substTo = params.getString(R.styleable.RuleMathView_substTo)
        subst = expressionSubstitutionFromStructureStrings(substFrom!!, substTo!!)
        setDefaults(context)
    }

    private fun setDefaults(context: Context) {
        textSize = Constants.ruleDefaultSize
        setHorizontallyScrolling(true)
        isHorizontalScrollBarEnabled = true
        isScrollbarFadingEnabled = true
        movementMethod = ScrollingMovementMethod()
        val themeName = Storage.shared.theme(context)
        setTextColor(ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_COLOR))
        //setTextColor(Color.LTGRAY)
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
        val style = if (subst.basedOnTaskContext) VariableStyle.DEFAULT else VariableStyle.GREEK
        val from = when (type) {
            Type.SET -> MathResolver.resolveToPlain(subst.left, style, TaskType.SET)
            else -> MathResolver.resolveToPlain(subst.left, style)
        }
        val to = when (type) {
            Type.SET -> MathResolver.resolveToPlain(subst.right, style, TaskType.SET)
            else -> MathResolver.resolveToPlain(subst.right, style)
        }
        if (from.tree == null || to.tree == null) {
            text = context.getString(R.string.parsing_error)
            return
        }
        try {
            text = MathResolver.getRule(from, to)
        } catch (e: Exception) {
            text = context.getString(R.string.parsing_error)
            Log.e(TAG, "Error during substitution render")
        }
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
                val themeName = Storage.shared.theme(context)
                setBackgroundColor(ThemeController.shared.getColorByTheme(themeName, ColorName.ON_TOUCH_BACKGROUND_COLOR))
            }
            event.action == MotionEvent.ACTION_UP -> {
                Log.d(TAG, "ACTION_UP")
                if (needClick && AndroidUtil.touchUpInsideView(this, event)) {
                    PlayScene.shared.setCurrentRuleView(context, this)
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