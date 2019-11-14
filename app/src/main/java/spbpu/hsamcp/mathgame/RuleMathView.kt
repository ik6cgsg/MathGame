package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Vibrator
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView

class RuleMathView: TextView, View.OnLongClickListener {
    private val TAG = "RuleMathView"
    var substFrom: String? = null
        private set
    var substTo: String? = null
        private set

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
        textSize = convertDpToPx(8)
        setTextColor(Color.BLACK)
        typeface = Typeface.MONOSPACE
        setPadding(10, 10, 10, 10)
        if (substFrom != null && substTo != null) {
            val textStr = "$substFrom->$substTo"
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
    private fun convertDpToPx(dp: Int): Float {
        return Math.round(dp * context.resources.displayMetrics.xdpi /
            DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }
}