package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Vibrator
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import spbpu.hsamcp.mathgame.mathResolver.MathResolver

class RuleMathView: TextView, View.OnLongClickListener {
    private val TAG = "RuleMathView"
    var substFrom: String? = null
        private set
    var substTo: String? = null
        private set
    private var defaultSize = 20f
    private val defaultPadding: Int = 10

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val params = context.obtainStyledAttributes(attrs, R.styleable.RuleMathView)
        substFrom = params.getString(R.styleable.RuleMathView_substFrom)
        substTo = params.getString(R.styleable.RuleMathView_substTo)
        setDefaults()
        setOnLongClickListener(this)
    }

    private fun setDefaults() {
        textSize = defaultSize
        setTextColor(Color.BLACK)
        typeface = Typeface.MONOSPACE
        setLineSpacing(0f, 0.5f)
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        if (substFrom != null && substTo != null) {
            val from = MathResolver.resolveToPlain(substFrom!!).toString()
            val to = MathResolver.resolveToPlain(substTo!!).toString()
            val textStr = MathResolver.getRule(from, to, " -> ")
            text = textStr
        }
    }

    override fun onLongClick(v: View?): Boolean {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)
        MathScene.currentRule = this
        MathScene.onRuleClicked()
        return true
    }

    /** TextView OVERRIDES **/

    /** UTILS **/
}