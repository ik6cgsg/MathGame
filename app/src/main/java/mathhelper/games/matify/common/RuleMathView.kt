package mathhelper.games.matify.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import api.expressionSubstitutionFromStructureStrings
import expressiontree.ExpressionSubstitution
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.mathResolver.MathResolver
import mathhelper.games.matify.mathResolver.TaskType
import mathhelper.games.matify.mathResolver.VariableStyle
import java.lang.Exception

class RuleMathView: HorizontalScrollView {//androidx.appcompat.widget.AppCompatTextView {
    private val TAG = "RuleMathView"
    private val moveTreshold = 10
    var subst: ExpressionSubstitution? = null
        private set
    lateinit var ruleView: TextView
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
        scrollBarSize = 20
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            horizontalScrollbarThumbDrawable = context.getDrawable(R.drawable.alert_shape)
        }
        isScrollbarFadingEnabled = false
        isFillViewport = true
        ruleView = TextView(context)
        ruleView.textSize = Constants.ruleDefaultSize
        ruleView.setTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
        ruleView.typeface = Typeface.MONOSPACE
        ruleView.background = context.getDrawable(R.drawable.row_clickable)
        ruleView.isClickable = true
        ruleView.isFocusable = true
        ruleView.setOnClickListener {
            PlayScene.shared.setCurrentRuleView(context, this)
        }
        ruleView.setLineSpacing(0f, Constants.mathLineSpacing)
        ruleView.setPadding(
            Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)
        ruleView.includeFontPadding = false
        /*setPadding(
            Constants.defaultPadding, Constants.defaultPadding,
            Constants.defaultPadding, Constants.defaultPadding)*/
        addView(ruleView)
        if (subst != null) {
            setSubst(subst!!, "")
        }
    }

    fun setSubst(subst: ExpressionSubstitution, type: String?) {
        this.subst = subst
        val style = if (subst.basedOnTaskContext) VariableStyle.DEFAULT else VariableStyle.GREEK
        try {
            val ruleStr = MathResolver.getRule(subst.left, subst.right, style, type)
            ruleView.text = ruleStr
            //ruleView.maxLines = ruleStr.lines().size+1
        } catch (e: Exception) {
            ruleView.text = context.getString(R.string.parsing_error)
            Log.e(TAG, "Error during substitution render")
        }
    }

    /** TextView OVERRIDES **/
    /*override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        super.onTouchEvent(event)
        /*when {
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
        }*/
        return true
    }*/

    /** UTILS **/
}