package spbpu.hsamcp.mathgame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.TextView
import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode

class RuleMathView: TextView {
    private val TAG = "RuleMathView"
    private var rule: ExpressionNode? = null

    /** INITIALIZATION **/
    constructor(context: Context): super(context) {
        setDefaults()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setDefaults()
    }

    private fun setDefaults() {
        textSize = convertDpToPx(14)
        setTextColor(Color.BLACK)
        typeface = Typeface.MONOSPACE
        setPadding(20, 10, 20, 20)
        if (text != null) {
            rule = stringToExpression(text.toString())
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        rule = stringToExpression(text.toString())
    }

    private fun convertDpToPx(dp: Int): Float {
        return Math.round(dp * context.resources.displayMetrics.xdpi /
            DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }
}